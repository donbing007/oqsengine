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
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.AppStatus.Confirmed;
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

    @Override
    public void onNext(EntityClassSyncRequest entityClassSyncRequest,
                       StreamObserver<EntityClassSyncResponse> responseStreamObserver) {
        if (entityClassSyncRequest.getStatus() == HEARTBEAT.ordinal()) {
            /**
             * 处理心跳
             */
            String uid = entityClassSyncRequest.getUid();
            if (null != uid) {
                confirmHeartBeat(uid);
            }
        } else if (entityClassSyncRequest.getStatus() == REGISTER.ordinal()) {
            /**
             * 处理注册
             */
            WatchElement w =
                    new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                            entityClassSyncRequest.getVersion(), WatchElement.AppStatus.Register);

            responseWatchExecutor.add(entityClassSyncRequest.getUid(), responseStreamObserver, w);

            /**
             * 确认注册信息
             */
            if (confirmRegister(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                    entityClassSyncRequest.getVersion(), entityClassSyncRequest.getUid())) {

                /**
                 * 当前的版本小于appId + env 对应的版本时，将从元数据获取一次数据、异步推出
                 */
                Integer currentVersion = responseWatchExecutor.version(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv());
                if (null == currentVersion || currentVersion < entityClassSyncRequest.getVersion()) {
                    pull(entityClassSyncRequest.getUid(), w);
                }
            }
        } else if (entityClassSyncRequest.getStatus() == SYNC_OK.ordinal()) {
            /**
             * 处理返回结果成功
             */
            WatchElement w =
                    new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                            entityClassSyncRequest.getVersion(), Confirmed);

            responseWatchExecutor.update(entityClassSyncRequest.getUid(), w);

        } else if (entityClassSyncRequest.getStatus() == SYNC_FAIL.ordinal()) {
            /**
             * 处理返回结果失败
             */
            WatchElement w =
                    new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                            entityClassSyncRequest.getVersion(), Notice);
            /**
             * 当客户端告知更新失败时，直接进行重试
             */
            pull(entityClassSyncRequest.getUid(), w);
        }
    }

    /**
     * 实现SyncHandler中的registerConfirm功能
     *
     * @param appId
     * @param version
     * @param uid
     */
    private boolean confirmRegister(String appId, String env, int version, String uid) {
        return confirmResponse(appId, env, version, uid, CONFIRM_REGISTER);
    }

    /**
     * 实现SyncHandler中的heartBeatConfirm功能
     *
     * @param uid
     */
    private void confirmHeartBeat(String uid) {
        responseWatchExecutor.resetHeartBeat(uid);

        confirmResponse(null, null, NOT_EXIST_VERSION, uid, CONFIRM_HEARTBEAT);
    }

    /**
     * 主动拉取EntityClass(用于推送失败或超时)
     * <p>
     * String uid
     * WatchElement watchElement
     *
     * @return
     */
    @Override
    public void pull(String uid, WatchElement watchElement) {
        /**
         * 这里异步执行
         */
        executor.submit(() -> {
            try {
                ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

                if (null != watcher) {
                    AppUpdateEvent appUpdateEvent = entityClassGenerator.pull(watchElement.getAppId(), watchElement.getEnv());
                    if (null != appUpdateEvent) {

                        /**
                        * 强制推送到外部(不更新当前版本)
                        */
                        EntityClassSyncResponse response =
                                generateResponse(appUpdateEvent.getAppId(), appUpdateEvent.getEnv(), appUpdateEvent.getVersion(),
                                                                                            appUpdateEvent.getEntityClassSyncRspProto());

                        return responseByWatch(appUpdateEvent.getAppId(), appUpdateEvent.getEnv(),
                                                appUpdateEvent.getVersion(), response, watcher, false);
                    }
                } else {
                    logger.warn("not exist watcher to handle data sync response, appId: {}, version : {}, uid :{}..."
                            , watchElement.getAppId(), watchElement.getVersion(), uid);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
            return false;
        });
    }

    /**
     * 接收外部推送
     *
     * @param event
     * @return
     */
    @Override
    public boolean push(AppUpdateEvent event) {

        /**
         * 更新版本成功、则推送给外部
         */
        if (responseWatchExecutor.addVersion(event.getAppId(), event.getEnv(), event.getVersion())) {
            List<ResponseWatcher> needList = responseWatchExecutor.need(new WatchElement(event.getAppId(), event.getEnv(), event.getVersion(), Notice));
            if (!needList.isEmpty()) {
                EntityClassSyncResponse response = generateResponse(event.getAppId(), event.getEnv(), event.getVersion(), event.getEntityClassSyncRspProto());

                needList.forEach(
                        nl -> {
                            responseByWatch(event.getAppId(), event.getEnv(), event.getVersion(), response, nl, false);
                        }
                );
            }
        }
        return true;
    }

    /**
     * confirmResponse(发送服务器注册确认、心跳的回包)
     *
     * @param appId
     * @param env
     * @param version
     * @param uid
     * @param requestStatus
     */
    private boolean confirmResponse(String appId, String env, int version, String uid, RequestStatus requestStatus) {
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        if (null != watcher) {
            EntityClassSyncResponse.Builder builder =
                    EntityClassSyncResponse.newBuilder()
                            .setUid(uid)
                            .setStatus(requestStatus.ordinal());

            if (requestStatus.equals(CONFIRM_REGISTER)) {
                builder.setAppId(appId).setVersion(version);
            }

            return responseByWatch(appId, env, version, builder.build(), watcher, true);
        } else {
            logger.warn("watch not exist to handle confirm : {} response, appId: {}, version : {}, uid :{}..."
                    , requestStatus.name(), appId, version, uid);
        }
        return false;
    }

    private boolean responseByWatch(String appId, String env, int version, EntityClassSyncResponse response,
                                    ResponseWatcher watcher, boolean confirmRegister) {
        /**
         * 成功且不是注册确认，则加入到DelayTaskQueue中进行监听
         */
        boolean ret = runObserverOnNext(response, watcher);
        if (ret && !confirmRegister) {
            retryExecutor.offer(
                    new RetryExecutor.DelayTask(gRpcParamsConfig.getDefaultDelayTaskDuration(),
                            new RetryExecutor.Element(new WatchElement(appId, env, version, Notice), watcher.uid())));
        }
        return ret;
    }

    /**
     * 发送Response
     * @param response
     * @param watcher
     * @return
     */
    private boolean runObserverOnNext(EntityClassSyncResponse response, ResponseWatcher watcher) {
        if (null != watcher) {
            return watcher.runWithCheck(observer -> {
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
            });
        }
        logger.warn("current watch is not exists or has been removed...");
        return false;
    }

    /**
     * 生成Response
     * @param appId
     * @param env
     * @param version
     * @param result
     * @return
     */
    private EntityClassSyncResponse generateResponse(String appId, String env, int version, EntityClassSyncRspProto result) {
        return EntityClassSyncResponse.newBuilder()
                .setMd5(MD5Utils.getMD5(result.toByteArray()))
                .setAppId(appId)
                .setEnv(env)
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
                    WatchElement w = task.element().getW();
                    if (watcher.isOnServe() && watcher.onWatch(w)) {
                        /**
                         * 直接拉取
                         */
                        pull(task.element().getUid(), w);
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
