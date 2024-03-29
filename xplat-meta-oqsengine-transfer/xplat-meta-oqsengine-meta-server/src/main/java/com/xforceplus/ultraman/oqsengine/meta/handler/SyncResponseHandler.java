package com.xforceplus.ultraman.oqsengine.meta.handler;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.DATA_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.REGISTER;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.REGISTER_OK;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.RESET;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_OK;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Notice;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.APP_UPDATE_PULL_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ServerConstant.SERVER_TASK_COUNT;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.SyncCode;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * sync response handler, implement.
 *
 * @author xujia
 * @since 1.8
 */
public class SyncResponseHandler implements IResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(SyncResponseHandler.class);

    @Resource
    private IResponseWatchExecutor responseWatchExecutor;

    @Resource
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;

    @Resource
    private EntityClassGenerator entityClassGenerator;

    @Resource(name = "grpcTaskExecutor")
    private ExecutorService taskExecutor;

    @Resource
    private GRpcParams grpcParams;

    @Resource
    private MetricsRecorder metricsRecorder;

    private final List<Thread> longRunTasks = new ArrayList<>(SERVER_TASK_COUNT);

    private volatile boolean isShutdown = false;

    @Override
    public void start() {

        //  设置服务状态为启用
        isShutdown = false;

        //  启动retryExecutor
        retryExecutor.start();

        //  启动responseWatchExecutor
        responseWatchExecutor.start();

        //  添加watchElementCheck任务线程
        longRunTasks.add(ThreadUtils.create(this::watchElementCheck));

        //  添加keepAlive任务线程
        longRunTasks.add(ThreadUtils.create(this::keepAlive));

        //  启动当前Task线程
        longRunTasks.forEach(Thread::start);

        logger.debug("syncResponseHandler start.");
    }

    /**
     * 关闭.
     */
    @Override
    public void stop() {
        //  设置服务状态为禁用
        isShutdown = true;
        //  关闭retryExecutor
        retryExecutor.stop();
        //  关闭responseWatchExecutor
        responseWatchExecutor.stop();

        longRunTasks.forEach(
            runTask -> {
                ThreadUtils.shutdown(null, SHUT_DOWN_WAIT_TIME_OUT);
            }
        );
        longRunTasks.clear();

        logger.debug("syncResponseHandler stop.");
    }

    /**
     * 执行对于request的处理.
     */
    @Override
    public void invoke(EntityClassSyncRequest entityClassSyncRequest,
                       StreamObserver<EntityClassSyncResponse> responseStreamObserver) {

        logger.info("grpc-request in : clientId : {}， appId : {}, status : {}", entityClassSyncRequest.getClientId(), entityClassSyncRequest.getAppId(),
            RequestStatus.getInstance(entityClassSyncRequest.getStatus()));

        //  处理心跳
        String uid = entityClassSyncRequest.getUid();
        if (!uid.isEmpty()) {
            confirmHeartBeat(uid, responseStreamObserver);
        }

        if (entityClassSyncRequest.getStatus() == REGISTER.ordinal() || entityClassSyncRequest.getStatus() ==
            RESET.ordinal()) {
            //  处理注册
            WatchElement w =
                    new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                            entityClassSyncRequest.getVersion(), Register);

            boolean isForce = entityClassSyncRequest.getForce();
            if (entityClassSyncRequest.getStatus() == RESET.ordinal()) {
                isForce = true;
            }

            responseWatchExecutor.add(entityClassSyncRequest.getClientId(), entityClassSyncRequest.getUid()
                , responseStreamObserver, w, isForce);

            //  强制刷新
            if (entityClassSyncRequest.getForce() || entityClassSyncRequest.getStatus() == RESET.ordinal()) {

                //  强制刷新ENV
                if (entityClassSyncRequest.getStatus() == RESET.ordinal()) {
                    confirmRegister(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                        entityClassSyncRequest.getVersion(), entityClassSyncRequest.getUid());
                }

                pull(entityClassSyncRequest.getClientId(), entityClassSyncRequest.getUid(), entityClassSyncRequest.getForce(), w, SYNC_OK);

            } else if (confirmRegister(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                    entityClassSyncRequest.getVersion(), entityClassSyncRequest.getUid())) {

                /*
                 * 客户端版本为-1或者
                 * 当前的服务端版本不存在
                 * 当前的服务端记录版本与 appId + env 对应的版本不一致时，
                 * 将从元数据获取一次数据、异步推出
                 */
                Integer currentVersion =
                    responseWatchExecutor.version(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv());
                if (null == currentVersion
                    || NOT_EXIST_VERSION == entityClassSyncRequest.getVersion()
                    || entityClassSyncRequest.getVersion() != currentVersion) {
                    pull(entityClassSyncRequest.getClientId(), entityClassSyncRequest.getUid(), entityClassSyncRequest.getForce(), w, SYNC_OK);
                }
            }
            logger.debug("register uid [{}], appId [{}], env [{}], version [{}] success.",
                    entityClassSyncRequest.getUid(), entityClassSyncRequest.getAppId(),
                    entityClassSyncRequest.getEnv(), entityClassSyncRequest.getVersion());
        } else if (entityClassSyncRequest.getStatus() == SYNC_OK.ordinal()) {
            //  处理返回结果成功
            WatchElement w =
                new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getEnv(),
                    entityClassSyncRequest.getVersion(), Confirmed);

            boolean ret = responseWatchExecutor.update(entityClassSyncRequest.getUid(), w);
            if (ret) {
                metricsRecorder.info(entityClassSyncRequest.getAppId(),
                    MetricsLog.linkKey(entityClassSyncRequest.getClientId(), SyncCode.SYNC_DATA_OK.name()),
                    String.format("sync data success, clientId : %s, env : %s, version : %d.",
                        entityClassSyncRequest.getClientId(), entityClassSyncRequest.getEnv(), entityClassSyncRequest.getVersion()));
            }
        } else if (entityClassSyncRequest.getStatus() == SYNC_FAIL.ordinal()) {
            metricsRecorder.error(entityClassSyncRequest.getAppId(),
                MetricsLog.linkKey(entityClassSyncRequest.getClientId(), SyncCode.SYNC_DATA_ERROR.name()),
                String.format("sync data failed, clientId : %s, env : %s, version : %d.",
                    entityClassSyncRequest.getClientId(), entityClassSyncRequest.getEnv(), entityClassSyncRequest.getVersion()));
        } else if (entityClassSyncRequest.getStatus() == DATA_ERROR.ordinal()) {
            metricsRecorder.error(entityClassSyncRequest.getAppId(),
                MetricsLog.linkKey(entityClassSyncRequest.getClientId(), SyncCode.SYNC_DATA_ERROR.name()),
                String.format("sync data format error, clientId : %s, env : %s, version : %d.",
                    entityClassSyncRequest.getClientId(), entityClassSyncRequest.getEnv(), entityClassSyncRequest.getVersion()));
        }
    }

    @Override
    public boolean isShutDown() {
        return isShutdown;
    }

    /**
     * 主动拉取EntityClass(用于推送失败或超时).
     */
    @Override
    public void pull(String clientId, String uid, boolean force, WatchElement watchElement, RequestStatus requestStatus) {

        taskExecutor.submit(() -> {
            try {
                internalPull(uid, force, watchElement, requestStatus);
            } catch (Exception e) {
                //  当元数据告知失败将在一分钟后进行重试
                if (e instanceof MetaSyncServerException
                    && e.getMessage().equalsIgnoreCase(APP_UPDATE_PULL_ERROR.name())) {
                    retryExecutor.offer(
                        new RetryExecutor.DelayTask(grpcParams.getDefaultDelayTaskDuration(),
                            new RetryExecutor.Element(
                                new WatchElement(watchElement.getAppId(), watchElement.getEnv(),
                                    watchElement.getVersion(), watchElement.getStatus()),
                                uid, clientId)));
                }

                metricsRecorder.info(watchElement.getAppId(),
                    MetricsLog.linkKey(clientId, SyncCode.PULL_DATA_FAILED.name()),
                    String.format("pull data failed, clientId : %s, env : %s, version : %d, message : %s",
                        clientId, watchElement.getEnv(), watchElement.getVersion(), e.getMessage()));
            }
        });
    }


    /**
     * 接收外部推送.
     */
    @Override
    public boolean push(ServerSyncEvent event) {

        //  更新版本成功、则推送给外部
        if (responseWatchExecutor.addVersion(event.appId(), event.env(), event.version())) {
            List<ResponseWatcher> needList = null;
            try {
                needList = responseWatchExecutor.need(new WatchElement(event.appId(), event.env(), event.version(), Notice));
            } catch (Exception e) {
                metricsRecorder.error(event.appId(),
                    SyncCode.PUSH_DATA_CHECK_FAILED.name(),
                    String.format("check needList failed, env : %s, version : %d, message : %s",
                        event.env(), event.version(), e.getMessage()));

                return false;
            }

            if (null != needList && !needList.isEmpty()) {
                for (ResponseWatcher r : needList) {
                    taskExecutor.submit(() -> {
                        try {
                            EntityClassSyncResponse response = generateResponse(r.uid(), event.appId(), event.env(), event.version(),
                                    RequestStatus.SYNC, event.entityClassSyncRspProto(), false);
                            responseByWatch(event.appId(), event.env(), event.version(), response, r, false);
                        } catch (Exception e) {
                            metricsRecorder.error(event.appId(),
                                MetricsLog.linkKey(r.clientId(), SyncCode.SEND_PUSH_DATA_FAILED.name()),
                                String.format("send push data failed, env : %s, version : %d, message : %s",
                                    event.env(), event.version(), e.getMessage()));
                        }
                    });
                }
            } else {
                logger.warn("need list is empty, no watch on event [{}-{}-{}]", event.appId(), event.env(), event.version());
            }
        } else {
            //  元数据推送的AppId版本小于当前关注版本，忽略...
            logger.warn("appId [{}], env [{}], push version [{}] is less than watcher version [{}], ignore...",
                event.appId(), event.env(), event.version(),
                responseWatchExecutor.version(event.appId(), event.env()));
        }
        return true;
    }

    private boolean internalPull(String uid, boolean force, WatchElement watchElement, RequestStatus requestStatus) {
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        //  判断watcher的可用性
        if (null != watcher && watcher.isActive()) {
            try {
                ServerSyncEvent appUpdateEvent =
                    entityClassGenerator.pull(watchElement.getAppId(), watchElement.getEnv());
                if (null == appUpdateEvent) {
                    logger.warn("pull data fail, appUpdateEvent is null, app {}, env {}.", watchElement.getAppId(), watchElement.getEnv());
                    return false;
                }
                if (isNeedEvent(watchElement, appUpdateEvent, requestStatus, force)) {
                    //  主动拉取不会更新当前的appVersion
                    EntityClassSyncResponse response =
                        generateResponse(uid, appUpdateEvent.appId(), appUpdateEvent.env(),
                            appUpdateEvent.version(),
                            RequestStatus.SYNC, appUpdateEvent.entityClassSyncRspProto(), force);
                    return responseByWatch(appUpdateEvent.appId(), appUpdateEvent.env(),
                        appUpdateEvent.version(), response, watcher, force);
                }
                logger.info("current notice version [{}] is large than updateVersion [{}], event will be ignore...",
                    watchElement.getVersion(), appUpdateEvent.version());
                return true;
            } catch (Exception e) {
                throw e;
            }
        } else {
            logger.warn("not exist watcher to handle data sync response, appId [{}], version [{}], uid [{}]...",
                watchElement.getAppId(), watchElement.getVersion(), uid);
        }
        return false;
    }

    /**
     * 实现SyncHandler中的registerConfirm功能.
     */
    private boolean confirmRegister(String appId, String env, int version, String uid) {
        return confirmResponse(appId, env, version, uid, REGISTER_OK);
    }

    /**
     * 实现SyncHandler中的heartBeatConfirm功能.
     */
    private void confirmHeartBeat(String uid, StreamObserver<EntityClassSyncResponse> responseStreamObserver) {
        ResponseWatcher responseWatcher = responseWatchExecutor.watcher(uid);
        if (null != responseWatcher) {
            if (responseWatcher.isActive()) {
                responseWatchExecutor.resetHeartBeat(uid);
            }
        }

        //  直接返回心跳
        responseStreamObserver.onNext(
                EntityClassSyncResponse.newBuilder()
                        .setUid(uid)
                        .setStatus(RequestStatus.HEARTBEAT.ordinal())
                    .build());
    }


    /**
     * 比较期望版本和元数据返回版本
     * 如果是requestStatus是sync_ok，表示只关注大于当前expected的版本
     * 如果是requestStatus是sync_Failed，表示只关注大于或等于当前expected的版本.
     */
    private boolean isNeedEvent(WatchElement expected, ServerSyncEvent actual, RequestStatus requestStatus, boolean force) {
        if (!expected.getAppId().equals(actual.appId())) {
            logger.warn("pull data fail, expected appId {} not equals to event-returns {}", expected.getAppId(), actual.appId());
            return false;
        }

        if (!expected.getEnv().equals(actual.env())) {
            logger.warn("pull data fail, appId {}, expected env {} not equals to event-returns {}",
                expected.getAppId(), expected.getEnv(), actual.env());
            return false;
        }
        if (!force) {
            switch (requestStatus) {
                case SYNC_OK:
                    return expected.getVersion() < actual.version();
                case SYNC_FAIL:
                    return expected.getVersion() <= actual.version();
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * confirmResponse(发送服务器注册确认、心跳的回包).
     */
    private boolean confirmResponse(String appId, String env, int version, String uid, RequestStatus requestStatus) {
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        if (null != watcher) {
            EntityClassSyncResponse.Builder builder =
                    EntityClassSyncResponse.newBuilder()
                            .setUid(uid)
                            .setStatus(requestStatus.ordinal());

            if (requestStatus.equals(REGISTER_OK)) {
                builder.setAppId(appId).setVersion(version).setEnv(env);
            }

            return responseByWatch(appId, env, version, builder.build(), watcher, true);
        } else {
            logger.warn("watch not exist to handle confirm : {} response, appId: {}, version : {}, uid :{}...",
                requestStatus.name(), appId, version, uid);
        }
        return false;
    }

    /**
     * 发送Response、如果当前不是注册或心跳，且发送成功，则加入重试观察队列.
     */
    private boolean responseByWatch(String appId, String env, int version, EntityClassSyncResponse response,
                                    ResponseWatcher watcher, boolean registerOrHeartBeat) {

        //  发送.
        boolean ret = observerOnNext(response, watcher);

        //  成功且不是注册确认，则加入到DelayTaskQueue中进行监听.
        if (ret && !registerOrHeartBeat) {
            logger.info("send app-pack ok, response [{}, {}, {}, {}]",
                "RSP APP_ID:" + response.getAppId(), "ENV:" + response.getEnv(), "VER:" + response.getVersion(),
                "UID:" + response.getUid());
            retryExecutor.offer(
                new RetryExecutor.DelayTask(grpcParams.getDefaultDelayTaskDuration(),
                    new RetryExecutor.Element(new WatchElement(appId, env, version, Notice), watcher.uid(), watcher.clientId())));
        }
        return ret;
    }

    /**
     * 发送Response,如果发送失败，会直接release当前的watcher，并返回false.
     */
    private boolean observerOnNext(EntityClassSyncResponse response, ResponseWatcher watcher) {
        if (null != watcher) {
            return watcher.runWithCheck(observer -> {
                try {
                    observer.onNext(response);
                    return true;
                } catch (Exception e) {
                    logger.warn("response to observer[{}] failed.", watcher.uid());
                    //  抛出异常将等待1ms后进行清理.
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
     * 生成Response.
     */
    private EntityClassSyncResponse generateResponse(String uid, String appId, String env, int version,
                                                     RequestStatus requestStatus, EntityClassSyncRspProto result, boolean force) {
        return EntityClassSyncResponse.newBuilder()
            .setMd5(MD5Utils.getMD5(result.toByteArray()))
            .setUid(uid)
            .setAppId(appId)
            .setEnv(env)
            .setVersion(version)
            .setStatus(requestStatus.ordinal())
            .setEntityClassSyncRspProto(result)
            .setForce(force)
            .build();
    }

    /**
     * 检查下发版本是否更新成功.
     */
    private boolean watchElementCheck() {
        while (!isShutdown) {
            RetryExecutor.DelayTask task = retryExecutor.take();
            if (null == task || null == task.element()) {
                if (!isShutdown) {
                    TimeWaitUtils.wakeupAfter(POLL_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                }
                continue;
            }

            ResponseWatcher watcher = responseWatchExecutor.watcher(task.element().getUid());
            if (null != watcher) {
                WatchElement w = task.element().getElement();
                if (watcher.isActive() && watcher.onWatch(w)) {
                    //  直接拉取
                    pull(null, task.element().getUid(), false, w, SYNC_FAIL);
                    logger.debug("delay task re-pull success, uid [{}], appId [{}], env [{}], version [{}] success.",
                        watcher.uid(), w.getAppId(), w.getEnv(), w.getVersion());
                }
            }
        }

        logger.info("delayTask has quited due to server shutdown...");
        return true;
    }

    /**
     * 检查当前的存活状况.
     */
    private boolean keepAlive() {
        while (!isShutdown) {
            responseWatchExecutor.keepAliveCheck(grpcParams.getDefaultHeartbeatTimeout());
            //  等待一秒进入下一次循环
            TimeWaitUtils.wakeupAfter(grpcParams.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }

        logger.info("keepAlive check has quited due to server shutdown...");
        return true;
    }
}
