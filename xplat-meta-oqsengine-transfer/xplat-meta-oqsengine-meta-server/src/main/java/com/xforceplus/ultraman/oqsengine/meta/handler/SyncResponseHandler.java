package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.CONFIRM_HEARTBEAT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.CONFIRM_REGISTER;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.AppStatus.Notice;


/**
 * desc :
 * name : EntityClassSyncResponseHandler
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class SyncResponseHandler implements IResponseHandler<EntityClassSyncResponse>, BasicMessageHandler {

    private Logger logger = LoggerFactory.getLogger(SyncResponseHandler.class);

    @Resource
    private IResponseWatchExecutor watchExecutor;

    @Resource
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;

    @Resource
    private EntityClassGenerator entityClassGenerator;

    @Resource(name = "gRpcTaskExecutor")
    private Executor executor;

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    private Thread thread;

    /**
     * 创建监听delayTask的线程
     */
    @Override
    public void start() {
        thread = ThreadUtils.create(() -> {
            delayTask();
            return true;
        });

        thread.start();
    }

    /**
     * 销毁delayTask的线程
     */
    @Override
    public void stop() {
        ThreadUtils.shutdown(thread, SHUT_DOWN_WAIT_TIME_OUT);
    }

    /**
     * 实现SyncHandler中的registerConfirm功能
     * @param appId
     * @param version
     * @param uid
     */
    @Override
    public void confirmRegister(String appId, int version, String uid) {
        response(appId, version, uid, CONFIRM_REGISTER);
    }
    /**
     * 实现SyncHandler中的heartBeatConfirm功能
     * @param uid
     */
    @Override
    public void confirmHeartBeat(String uid) {
        response(null, -1, uid, CONFIRM_HEARTBEAT);
    }

    /**
     * 主动拉取EntityClass(用于推送失败或超时)
     *
     * @param appId
     * @param version
     * @return
     */
    @Override
    public boolean pull(String appId, int version, String uid) {
        try {
            Optional<IWatcher<EntityClassSyncResponse>> watcherOp = watchExecutor.watcher(uid);

            if (watcherOp.isPresent()) {
                return response(appId, version, entityClassGenerator.pull(appId, version), watcherOp.get());
            } else {
                logger.warn("watch not exist to handle data sync response, appId: {}, version : {}, uid :{}...", appId, version, uid);
            }

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return false;
    }

    /**
     * 接收外部推送
     * @param event
     * @return
     */
    public boolean push(AppUpdateEvent event) {
        return response(event.getAppId(), event.getVersion(), event.getEntityClassSyncRspProto(), null);
    }

    /**
     * 发送Response(服务器注册确认、心跳的回包)
     * @param appId
     * @param version
     * @param uid
     * @param requestStatus
     */
    private void response(String appId, int version, String uid, RequestStatus requestStatus) {
        Optional<IWatcher<EntityClassSyncResponse>> watcherOp = watchExecutor.watcher(uid);

        if (watcherOp.isPresent()) {
            EntityClassSyncResponse.Builder builder = EntityClassSyncResponse.newBuilder().setUid(uid)
                    .setStatus(requestStatus.ordinal());

            if (requestStatus.equals(CONFIRM_REGISTER)) {
                builder.setAppId(appId).setVersion(version);
            }

            responseByWatch(appId, version, builder.build(), watcherOp.get(), true);
        } else {
            logger.warn("watch not exist to handle confirm : {} response, appId: {}, version : {}, uid :{}..."
                    , requestStatus.name(), appId, version, uid);
        }
    }

    /**
     * 发送Response(EntityClass推送)
     * @param appId
     * @param version
     * @param result
     * @param watcher
     * @return
     */
    private boolean response(String appId, int version, EntityClassSyncRspProto result,
                                                IWatcher<EntityClassSyncResponse> watcher) {
        if (null == appId || appId.isEmpty()) {
            logger.warn("appId is null");
            return false;
        }

        if (null == result) {
            logger.warn("entityClassSyncRspProto is null");
            return false;
        }

        if (null != watcher) {
            responseByWatch(appId, version, generateResponse(appId, version, result), watcher, false);
        } else {
            List<IWatcher<EntityClassSyncResponse>> needList = watchExecutor.need(new WatchElement(appId, version, Notice));
            if (!needList.isEmpty()) {
                EntityClassSyncResponse response = generateResponse(appId, version, result);

                needList.forEach(
                        nl -> {
                            responseByWatch(appId, version, response, nl, false);
                        }
                );
            }
        }
        return true;
    }

    private void responseByWatch(String appId, int version, EntityClassSyncResponse response,
                                                IWatcher<EntityClassSyncResponse> watcher, boolean confirm) {
        if (null != watcher) {
            if (watcher.runWithCheck(observer -> {
                try {
                    observer.onNext(response);
                    return true;
                } catch (Exception e) {
                    logger.warn("response to observer[{}] failed.", watcher.uid());
                    /**
                     *  抛出异常将等待1ms后进行清理
                     */
                    TimeWaitUtils.wakeupAfter(1, TimeUnit.MILLISECONDS);

                    watchExecutor.release(watcher.uid());
                }
                return false;
            })) {
                /**
                 * 成功且不是注册确认，则加入到DelayTaskQueue中进行监听
                 */
                if (!confirm) {
                    offerDelayTask(appId, version, watcher.uid());
                }
            }
        } else {
            logger.warn("current watch is not exists or has been removed...");
        }
    }

    private EntityClassSyncResponse generateResponse(String appId, int version, EntityClassSyncRspProto result) {
        return EntityClassSyncResponse.newBuilder()
                .setMd5(MD5Utils.getMD5(result.toByteArray()))
                .setAppId(appId)
                .setVersion(version)
                .setEntityClassSyncRspProto(result)
                .build();
    }

    private void offerDelayTask(String appId, int version, String uid) {
        try {
            retryExecutor.offer(
                    new RetryExecutor.DelayTask(gRpcParamsConfig.getDefaultDelayTaskDuration(), new RetryExecutor.Element(appId, version, uid)));
        } catch (Exception e) {
            logger.error("offer delayTask failed...., appId : {}, version : {}, uid : {}", appId, version, uid);
            e.printStackTrace();
        }
    }

    private void delayTask() {
        while (true) {
            RetryExecutor.DelayTask task = retryExecutor.take();
            if (null == task) {
                TimeWaitUtils.wakeupAfter(1, TimeUnit.MILLISECONDS);
                continue;
            }

            executor.execute(() -> {
                Optional<IWatcher<EntityClassSyncResponse>> watcherOp = watchExecutor.watcher(task.element().getUid());
                if (watcherOp.isPresent()) {
                    IWatcher<EntityClassSyncResponse> watcher = watcherOp.get();
                    if (watcher.isOnServe() &&
                            watcher.onWatch(new WatchElement(task.element().getAppId(), task.element().getVersion(), Notice))) {
                        /**
                         * 直接拉取
                         */
                        pull(task.element().getAppId(), task.element().getVersion(), task.element().getUid());
                    }
                }
            });
        }
    }
}
