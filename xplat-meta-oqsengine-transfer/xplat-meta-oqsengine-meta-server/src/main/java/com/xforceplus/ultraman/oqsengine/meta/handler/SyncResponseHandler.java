package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.AppStatus.Notice;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ServerConstant.SERVER_TASK_COUNT;


/**
 * desc :
 * name : EntityClassSyncResponseHandler
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class SyncResponseHandler implements IResponseHandler<EntityClassSyncResponse> {

    private Logger logger = LoggerFactory.getLogger(SyncResponseHandler.class);

    @Resource
    private IResponseWatchExecutor responseWatchExecutor;

    @Resource
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;

    @Resource
    private EntityClassGenerator entityClassGenerator;

    @Resource(name = "gRpcTaskExecutor")
    private ExecutorService executor;

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    private List<Thread> executors = new ArrayList<>(SERVER_TASK_COUNT);

    private static volatile boolean isShutdown = false;

    /**
     * 创建监听delayTask的线程
     */
    @Override
    public void start() {

        /**
         * 设置服务状态为启用
         */
        isShutdown = false;
        /**
         * 启动retryExecutor
         */
        retryExecutor.on();
        /**
         * 启动responseWatchExecutor
         */
        responseWatchExecutor.start();

        /**
         * 1.添加delayAppVersion check任务线程
         */
        executors.add(ThreadUtils.create(this::delayTask));
        /**
         * 2.添加keepAlive check任务线程
         */
        executors.add(ThreadUtils.create(this::keepAliveCheck));

        /**
         * 启动当前Task线程
         */
        executors.forEach(Thread::start);
    }



    /**
     * 销毁delayTask的线程
     */
    @Override
    public void stop() {
        /**
         * 设置服务状态为禁用
         */
        isShutdown = true;
        /**
         * 关闭retryExecutor
         */
        retryExecutor.off();
        /**
         * 关闭responseWatchExecutor
         */
        responseWatchExecutor.stop();
        /**
         * 停止当前活动线程
         */
        executors.forEach(t -> ThreadUtils.shutdown(t, SHUT_DOWN_WAIT_TIME_OUT));
    }

    public static boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public void onNext(EntityClassSyncRequest entityClassSyncRequest,
                       StreamObserver<EntityClassSyncResponse> responseStreamObserver) {
        executor.submit(() -> {
            if (entityClassSyncRequest.getStatus() == HEARTBEAT.ordinal()) {
                /**
                 * 处理心跳
                 */
                String uid = entityClassSyncRequest.getUid();
                if (null != uid) {
                    responseWatchExecutor.heartBeat(uid);

                    confirmHeartBeat(uid);
                }
            } else if (entityClassSyncRequest.getStatus() == REGISTER.ordinal()) {
                /**
                 * 处理注册
                 */
                WatchElement w =
                        new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(), WatchElement.AppStatus.Register);
                responseWatchExecutor.add(entityClassSyncRequest.getUid(), responseStreamObserver, w);

                /**
                 * 确认注册信息
                 */
                confirmRegister(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(),
                        entityClassSyncRequest.getUid());

            } else if (entityClassSyncRequest.getStatus() == SYNC_OK.ordinal()) {
                /**
                 * 处理返回结果成功
                 */
                responseWatchExecutor.update(entityClassSyncRequest.getUid(),
                        new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(),
                                WatchElement.AppStatus.Confirmed));

            } else if (entityClassSyncRequest.getStatus() == SYNC_FAIL.ordinal()) {
                /**
                 * 处理返回结果失败
                 */
                pull(entityClassSyncRequest.getAppId(),
                        entityClassSyncRequest.getVersion(), entityClassSyncRequest.getUid());
            }
        });
    }

    /**
     * 实现SyncHandler中的registerConfirm功能
     * @param appId
     * @param version
     * @param uid
     */
    private void confirmRegister(String appId, int version, String uid) {
        response(appId, version, uid, CONFIRM_REGISTER);
    }
    /**
     * 实现SyncHandler中的heartBeatConfirm功能
     * @param uid
     */
    private void confirmHeartBeat(String uid) {
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
            ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

            if (null != watcher) {
                return response(appId, version, entityClassGenerator.pull(appId, version), watcher);
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
    @Override
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
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        if (null != watcher) {
            EntityClassSyncResponse.Builder builder = EntityClassSyncResponse.newBuilder().setUid(uid)
                    .setStatus(requestStatus.ordinal());

            if (requestStatus.equals(CONFIRM_REGISTER)) {
                builder.setAppId(appId).setVersion(version);
            }

            responseByWatch(appId, version, builder.build(), watcher, true);
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
                                                ResponseWatcher watcher) {
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
            List<ResponseWatcher> needList = responseWatchExecutor.need(new WatchElement(appId, version, Notice));
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
                                                ResponseWatcher watcher, boolean confirm) {
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

                    responseWatchExecutor.release(watcher.uid());
                }
                return false;
            })) {
                /**
                 * 成功且不是注册确认，则加入到DelayTaskQueue中进行监听
                 */
                if (!confirm) {
                    retryExecutor.offer(
                            new RetryExecutor.DelayTask(gRpcParamsConfig.getDefaultDelayTaskDuration(),
                                    new RetryExecutor.Element(appId, version, watcher.uid())));
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

    /***
     * 检查下发版本是否更新成功
     */
    private boolean delayTask() {
        while (!isShutdown) {
            RetryExecutor.DelayTask task = retryExecutor.take();
            if (null == task) {
                TimeWaitUtils.wakeupAfter(3, TimeUnit.MILLISECONDS);
                continue;
            }

            executor.execute(() -> {
                ResponseWatcher watcher = responseWatchExecutor.watcher(task.element().getUid());
                if (null != watcher) {
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

        logger.info("delayTask has quited due to server shutdown...");
        return true;
    }

    /***
     * 检查当前的存活状况
     * @return
     */
    private boolean keepAliveCheck() {
        while (!isShutdown) {
            responseWatchExecutor.keepAliceCheck(gRpcParamsConfig.getDefaultHeartbeatTimeout());
            /**
             * 等待一秒进入下一次循环
             */
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }

        logger.info("keepAlive check has quited due to server shutdown...");
        return true;
    }
}
