package com.xforceplus.ultraman.oqsengine.meta.handler;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.HEARTBEAT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_OK;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Init;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.CLIENT_TASK_COUNT;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.SyncCode;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SyncRequestHandler.
 */
public class SyncRequestHandler implements IRequestHandler {

    private Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

    @Resource(name = "grpcSyncExecutor")
    private SyncExecutor syncExecutor;

    @Resource
    private IRequestWatchExecutor requestWatchExecutor;

    @Resource
    private GRpcParams grpcParams;

    @Resource(name = "grpcTaskExecutor")
    private ExecutorService executorService;

    @Resource
    private MetricsRecorder metricsRecorder;

    private Queue<WatchElement> forgotQueue = new ConcurrentLinkedDeque<>();
    private List<Thread> longRunTasks = new ArrayList<>(CLIENT_TASK_COUNT);

    private volatile boolean isShutdown = false;
    private static final int PRINT_CHECK_DURATION = 10;

    @Override
    public void start() {
        isShutdown = false;

        //  1.添加watchElementCheck任务线程
        longRunTasks.add(ThreadUtils.create(this::keepAlive));


        //  2.添加watchElementCheck任务线程
        longRunTasks.add(ThreadUtils.create(this::watchElementCheck));

        //  启动所有任务线程
        longRunTasks.forEach(Thread::start);

        logger.info("requestWatchExecutor start.");
    }


    @Override
    public void stop() {
        isShutdown = true;
        requestWatchExecutor.stop();
    }

    @Override
    public synchronized boolean register(WatchElement watchElement) {
        RequestWatcher watcher = requestWatchExecutor.watcher();

        //  这里只判断是否watcher为空，如果服务watcher不为空
        if (null == watcher) {
            logger.warn("current gRpc-client is not init, can't offer appIds:{}.", watchElement);
            addToForgotQueue(watchElement);
            return false;
        }

        WatchElement w = watcher.watches().get(watchElement.getAppId());
        if (null != w) {
            if (!w.getEnv().equals(watchElement.getEnv())) {
                metricsRecorder.error(
                    w.getAppId(), SyncCode.REGISTER_ERROR.name(), String.format(
                        "can't register same appId [%s] with another env [%s], env [%s] already registered.",
                        w.getAppId(), watchElement.getEnv(), w.getEnv())
                );
                return false;
            }

            if (watchElement.getVersion() == NOT_EXIST_VERSION || watchElement.getVersion() > w.getVersion()) {
                if (!send(watcher.clientId(), watcher.uid(), true, true, RequestStatus.REGISTER, w)) {
                    metricsRecorder.error(
                        w.getAppId(), SyncCode.REGISTER_ERROR.name(),
                        String.format("send register failed, env %s", w.getEnv())
                    );

                    return false;
                }
            } else {
                //  ignore register
                if (logger.isDebugEnabled()) {
                    logger.debug("current watchList has this watchElement, appId [{}], ignore register...",
                        watchElement.getAppId());
                }
            }
        } else {
            //  watchExecutor not ready
            if (!requestWatchExecutor.isAlive(watcher.uid())) {
                addToForgotQueue(watchElement);
            } else {
                if (!send(
                    watcher.clientId(),
                    watcher.uid(),
                    false,
                    true,
                    RequestStatus.REGISTER,
                    watchElement)) {

                    metricsRecorder.error(
                        watchElement.getAppId(), SyncCode.REGISTER_ERROR.name(),
                        String.format("send register failed, env %s", watchElement.getEnv())
                    );

                    return false;
                }
            }
        }

        return true;
    }

    //  当发生断线时，需要重新连接，每次重新连接后，需要将当前OQS的WatchList重新注册到元数据中
    @Override
    public boolean reRegister() {
        //  当开启reRegister操作时，所有的register操作将被中断
        RequestWatcher requestWatcher = requestWatchExecutor.watcher();

        if (null != requestWatcher && requestWatcher.watches().size() > 0) {
            for (Map.Entry<String, WatchElement> e : requestWatcher.watches().entrySet()) {
                if (!send(requestWatcher.clientId(), requestWatcher.uid(),
                    false, false, RequestStatus.REGISTER, e.getValue())) {

                    String error = String.format("reRegister failed, env : %s", e.getValue().getEnv());
                    metricsRecorder.error(e.getValue().getAppId(), SyncCode.REGISTER_ERROR.name(), error);

                    requestWatcher.observer().onError(new Throwable(error));
                    return false;
                }

                //  刷新注册时间
                e.getValue().setRegisterTime(System.currentTimeMillis());
                logger.info("reRegister success uid [{}], appId [{}], env [{}], version [{}].",
                    requestWatcher.uid(), e.getKey(), e.getValue().getEnv(), e.getValue().getVersion());
            }
        }
        return true;
    }

    @Override
    public void initWatcher(String clientId, String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {
        requestWatchExecutor.create(clientId, uid, streamObserver);
    }

    /**
     * 执行处理response.
     */
    @Override
    public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void nil) {

        //  更新heartbeat
        requestWatchExecutor.resetHeartBeat(entityClassSyncResponse.getUid());

        //  更新状态
        if (entityClassSyncResponse.getStatus() == RequestStatus.REGISTER_OK.ordinal()) {
            boolean ret = requestWatchExecutor
                .update(new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                    entityClassSyncResponse.getVersion(), Confirmed));

            if (ret) {
                metricsRecorder.info(entityClassSyncResponse.getAppId(), SyncCode.REGISTER_OK.name(),
                    String.format("register success, uid : %s, env : %s, version : %s success.",
                        entityClassSyncResponse.getUid(), entityClassSyncResponse.getEnv(),
                        entityClassSyncResponse.getVersion()));
            }

        } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {
            //  执行返回结果
            executorService.submit(() -> {
                try {
                    accept(entityClassSyncResponse);
                } catch (Exception e) {
                    if (requestWatchExecutor.watcher().isActive()) {
                        requestWatchExecutor.watcher().observer().onError(e);
                    }
                }
            });
        }
    }

    @Override
    public MetricsRecorder metricsRecorder() {
        return metricsRecorder;
    }

    @Override
    public boolean isShutDown() {
        return isShutdown;
    }

    @Override
    public IRequestWatchExecutor watchExecutor() {
        return requestWatchExecutor;
    }

    @Override
    public void notReady() {
        requestWatchExecutor.inActive();

        //  如果是服务关闭，则直接跳出while循环
        if (isShutdown) {
            logger.warn("stream has broken due to client has been shutdown...");
        } else {
            String uid =
                (null != requestWatchExecutor.watcher()) ? requestWatchExecutor.watcher().uid() : "unKnow-stream";
            logger.warn("stream [{}] has broken, reCreate new stream after ({})ms...", uid,
                grpcParams.getReconnectDuration());

            //  设置睡眠
            TimeWaitUtils.wakeupAfter(grpcParams.getReconnectDuration(), TimeUnit.MILLISECONDS);

            //  进行资源清理
            requestWatchExecutor.release(uid);
        }
    }

    @Override
    public void ready() {
        requestWatchExecutor.active();
    }


    @Override
    public boolean reset(WatchElement watchElement) {
        WatchElement old = requestWatchExecutor.watcher().watches().get(watchElement.getAppId());
        try {
            watchElement.setStatus(WatchElement.ElementStatus.Register);

            requestWatchExecutor.add(watchElement, true);

            return send(
                requestWatchExecutor.watcher().clientId(),
                requestWatchExecutor.watcher().uid(),
                false,
                true,
                RequestStatus.RESET,
                watchElement);

        } catch (Exception e) {
            //  失败则重新将old写回
            requestWatchExecutor.add(old, true);

            metricsRecorder.error(watchElement.getAppId(), SyncCode.RESET_ENV_ERROR.name(),
                String.format("reset failed, env : %s, cause : %s", watchElement.getEnv(), e.getMessage()));
        }

        return false;
    }

    /**
     * 发送register.
     */
    private boolean send(String clientId, String uid, boolean force, boolean checkActive, RequestStatus requestStatus,
                         WatchElement v) {
        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

        EntityClassSyncRequest entityClassSyncRequest =
            builder.setUid(uid)
                .setAppId(v.getAppId())
                .setEnv(v.getEnv())
                .setVersion(v.getVersion())
                .setForce(force)
                .setStatus(requestStatus.ordinal())
                .setClientId(clientId)
                .build();

        requestWatchExecutor.add(v, false);

        try {
            SendUtils.sendRequest(requestWatchExecutor.watcher(), entityClassSyncRequest, checkActive);

            logger.info("register success uid [{}], appId [{}], env [{}], version [{}].",
                uid, v.getAppId(), v.getEnv(), v.getVersion());

            return true;
        } catch (Exception e) {
            v.setStatus(Init);
            return false;
        }
    }

    private void accept(EntityClassSyncResponse entityClassSyncResponse) {
        //  执行OQS更新EntityClass
        EntityClassSyncRequest.Builder entityClassSyncRequestBuilder;
        try {
            entityClassSyncRequestBuilder = execute(entityClassSyncResponse);
        } catch (Exception e) {
            entityClassSyncRequestBuilder = EntityClassSyncRequest.newBuilder().setStatus(SYNC_FAIL.ordinal());
            metricsRecorder.error(entityClassSyncResponse.getAppId(), SyncCode.SYNC_DATA_ERROR.name(),
                String.format("parse sync-data failed, env :%s, version : %s, cause : %s",
                    entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion(), e.getMessage()));
        }

        if (null != entityClassSyncRequestBuilder) {
            if (!entityClassSyncResponse.getForce()) {
                EntityClassSyncRequest entityClassSyncRequest = entityClassSyncRequestBuilder
                    .setClientId(requestWatchExecutor.watcher().clientId())
                    .setUid(entityClassSyncResponse.getUid())
                    .build();
                try {
                    //  回写处理结果, entityClassSyncRequest为空则代表传输存在问题.
                    SendUtils.sendRequest(requestWatchExecutor.watcher(),
                        entityClassSyncRequest,
                        true
                    );
                } catch (Exception e) {
                    metricsRecorder.error(entityClassSyncRequest.getAppId(), SyncCode.SEND_REQUEST_ERROR.name(),
                        String.format("send sync result failed, env :%s, version : %s, cause : %s",
                            entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion(), e.getMessage()));
                    throw e;
                }
            }

            if (entityClassSyncRequestBuilder.getStatus() == SYNC_OK.ordinal()) {
                metricsRecorder.info(entityClassSyncResponse.getAppId(), SyncCode.SYNC_DATA_OK.name(),
                    String.format("sync-data ok, uid : %s, env : %s, version : %s", entityClassSyncResponse.getUid(),
                        entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion()));
            }
        }
    }

    /**
     * 执行方法.
     */
    @SuppressWarnings("unchecked")
    private EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse) {

        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

        if (entityClassSyncResponse.getAppId().isEmpty()
            || entityClassSyncResponse.getAppId().isEmpty()
            || NOT_EXIST_VERSION == entityClassSyncResponse.getVersion()) {

            throw new MetaSyncClientException("appId/version/env could not be null...", false);
        }

        builder.setAppId(entityClassSyncResponse.getAppId())
            .setVersion(entityClassSyncResponse.getVersion())
            .setEnv(entityClassSyncResponse.getEnv());

        //  该方法返回的错误不会导致重新连接、但会通知服务端本次推送更新失败
        //  md5 check && 是否已存在版本判断.
        EntityClassSyncRspProto result = entityClassSyncResponse.getEntityClassSyncRspProto();

        if (md5Check(entityClassSyncResponse.getMd5(), result)) {
            WatchElement w =
                new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                    entityClassSyncResponse.getVersion(), Confirmed);

            //  当前关注此版本
            if (entityClassSyncResponse.getForce() || requestWatchExecutor.watcher().onWatch(w)) {
                boolean needReturnResult = false;
                //  执行外部传入的执行器
                try {
                    needReturnResult = syncExecutor
                        .sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion(),
                            result);
                } catch (Exception e) {
                    throw new MetaSyncClientException(e.getMessage(), false);
                }

                if (!needReturnResult) {
                    return null;
                }
                requestWatchExecutor.update(w);

            } else {
                logger.debug(String.format(
                    "sync data error, current oqs not watch this version or sync version is less than service-version, env : %s, version : %s",
                    entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion()));
            }
        } else {
            throw new MetaSyncClientException("checkMD5 failed.", false);
        }

        return builder.setStatus(SYNC_OK.ordinal());
    }

    private boolean md5Check(String md5, EntityClassSyncRspProto entityClassSyncRspProto) {
        if (null == md5 || md5.isEmpty() || null == entityClassSyncRspProto) {
            return false;
        }
        return md5.equals(getMD5(entityClassSyncRspProto.toByteArray()));
    }

    /**
     * 保持和服务端的KeepAlive.
     */
    private boolean keepAlive() {
        logger.debug("start keepAlive task ok...");
        while (!isShutDown()) {
            RequestWatcher requestWatcher = requestWatchExecutor.watcher();
            if (null != requestWatcher) {
                try {
                    if (requestWatcher.isActive()) {
                        if (System.currentTimeMillis() - requestWatcher.heartBeat()
                            > grpcParams.getDefaultHeartbeatTimeout()) {
                            requestWatcher.observer().onCompleted();
                            logger.warn("last heartbeat time [{}] reaches max timeout [{}]",
                                System.currentTimeMillis() - requestWatcher.heartBeat(),
                                grpcParams.getDefaultHeartbeatTimeout());
                        }
                    }

                    EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setClientId(requestWatcher.clientId())
                        .setUid(requestWatcher.uid())
                        .setStatus(HEARTBEAT.ordinal())
                        .build();

                    SendUtils.sendRequest(requestWatcher, request, true);
                } catch (Exception e) {
                    //  ignore
                    logger.warn("send keepAlive failed, message [{}], but exception will ignore due to retry...",
                        e.getMessage());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("keepAlive ok, print next check after ({})ms...",
                        grpcParams.getKeepAliveSendDuration());
                }
            }
            TimeWaitUtils.wakeupAfter(grpcParams.getKeepAliveSendDuration(), TimeUnit.MILLISECONDS);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("keepAlive task has quited due to sync-client shutdown...");
        }
        return true;
    }


    /**
     * 检查当前APP的注册状态，处于INIT、REGISTER的APP将被重新注册到服务端.
     */
    private boolean watchElementCheck() {
        if (logger.isDebugEnabled()) {
            logger.debug("start appCheck task ok...");
        }
        long counter = 0;
        while (!isShutDown()) {
            RequestWatcher requestWatcher = requestWatchExecutor.watcher();
            if (null != requestWatcher && requestWatcher.isActive()) {
                //  将forgetQueue中的数据添加到watch中
                int checkSize = forgotQueue.size();
                while (checkSize > 0) {
                    try {
                        WatchElement w = forgotQueue.remove();
                        if (!requestWatcher.watches().containsKey(w.getAppId())) {
                            requestWatcher.watches().put(w.getAppId(), w);
                        }
                    } catch (Exception e) {
                        //  ignore & break
                        break;
                    }
                    checkSize--;
                }

                requestWatcher.watches().values().stream().filter(s -> {
                    //  过滤出INIT、已经长时间处于REGISTER的watchElement
                    //  时间超长的判定标准为当前时间-最后注册时间 > delay时间
                    return s.getStatus().ordinal() == Init.ordinal()
                        || (s.getStatus().ordinal() < Confirmed.ordinal()
                        && System.currentTimeMillis() - s.getRegisterTime() > grpcParams.getDefaultDelayTaskDuration());
                }).forEach(
                    k -> {
                        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                        EntityClassSyncRequest entityClassSyncRequest =
                            builder.setUid(requestWatcher.uid())
                                .setClientId(requestWatcher.clientId())
                                .setAppId(k.getAppId())
                                .setEnv(k.getEnv())
                                .setVersion(k.getVersion())
                                .setStatus(RequestStatus.REGISTER.ordinal())
                                .build();
                        try {
                            SendUtils.sendRequest(requestWatcher, entityClassSyncRequest, true);
                            k.setRegisterTime(System.currentTimeMillis());
                        } catch (Exception e) {
                            k.setStatus(Init);
                        }
                    }
                );
                if (counter % PRINT_CHECK_DURATION == 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("app check ok, print next check after ({})ms...",
                            grpcParams.getMonitorSleepDuration() * PRINT_CHECK_DURATION);
                    }
                }
                counter++;
            }
            TimeWaitUtils.wakeupAfter(grpcParams.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("appCheck task has quited due to sync-client shutdown...");
        }

        return true;
    }

    /**
     * only test use， not interface.
     */
    public Queue<WatchElement> getForgotQueue() {
        return forgotQueue;
    }

    private void addToForgotQueue(WatchElement watchElement) {
        if (forgotQueue.stream().noneMatch(watchElement::logicEquals)) {
            forgotQueue.add(watchElement);
        }
    }
}
