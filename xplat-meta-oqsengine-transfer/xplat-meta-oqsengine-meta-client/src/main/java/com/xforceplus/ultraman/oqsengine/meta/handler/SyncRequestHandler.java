package com.xforceplus.ultraman.oqsengine.meta.handler;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.DATA_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.HEARTBEAT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_OK;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Init;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.CLIENT_TASK_COUNT;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequestWithALiveCheck;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.metrics.ConnectorMetricsDefine;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private Queue<WatchElement> forgotQueue = new ConcurrentLinkedDeque<>();
    private List<Thread> longRunTasks = new ArrayList<>(CLIENT_TASK_COUNT);

    private volatile boolean isShutdown = false;
    private static final int PRINT_CHECK_DURATION = 10;

    private AtomicInteger acceptDataHandleErrorCounter =
        Metrics.gauge(ConnectorMetricsDefine.CLIENT_ACCEPT_DATA_HANDLER_ERROR, new AtomicInteger(0));

    private AtomicInteger acceptDataFormatCounter =
        Metrics.gauge(ConnectorMetricsDefine.CLIENT_ACCEPT_DATA_FORMAT_ERROR, new AtomicInteger(0));

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
            forgotQueue.add(watchElement);

            return false;
        }

        AtomicBoolean ret = new AtomicBoolean(true);

        WatchElement w = watcher.watches().get(watchElement.getAppId());
        if (null != w) {
            if (!w.getEnv().equals(watchElement.getEnv())) {
                logger.warn("can't register same appId [{}] with another env [{}], env [{}] already registered.",
                    w.getAppId(), watchElement.getEnv(), w.getEnv());
            }

            if (watchElement.getVersion() == NOT_EXIST_VERSION) {
                return sendRegister(watcher.uid(), true, w);
            }

            logger.info("current watchList has this watchElement, appId [{}], ignore register...",
                watchElement.getAppId());
            return true;
        } else {
            if (!requestWatchExecutor.isAlive(watcher.uid())) {
                forgotQueue.add(watchElement);
            } else {
                return sendRegister(watcher.uid(), false, watchElement);
            }
        }

        return ret.get();
    }

    /**
     * 发送register.
     */
    private boolean sendRegister(String uid, boolean force, WatchElement v) {
        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

        EntityClassSyncRequest entityClassSyncRequest =
            builder.setUid(uid)
                .setAppId(v.getAppId())
                .setEnv(v.getEnv())
                .setVersion(v.getVersion())
                .setForce(force)
                .setStatus(RequestStatus.REGISTER.ordinal()).build();

        requestWatchExecutor.add(v);

        try {
            sendRequestWithALiveCheck(requestWatchExecutor.watcher(), entityClassSyncRequest);

            logger.info("register success uid [{}], appId [{}], env [{}], version [{}].",
                uid, v.getAppId(), v.getEnv(), v.getVersion());

            return true;
        } catch (Exception e) {
            v.setStatus(Init);
            return false;
        }
    }

    //  当发生断线时，需要重新连接，每次重新连接后，需要将当前OQS的WatchList重新注册到元数据中
    @Override
    public boolean reRegister() {
        //  当开启reRegister操作时，所有的register操作将被中断
        RequestWatcher requestWatcher = requestWatchExecutor.watcher();
        boolean isOperationOK = true;
        if (null != requestWatcher && requestWatcher.watches().size() > 0) {
            for (Map.Entry<String, WatchElement> e : requestWatcher.watches().entrySet()) {
                try {
                    EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                    EntityClassSyncRequest entityClassSyncRequest =
                        builder.setAppId(e.getKey())
                            .setEnv(e.getValue().getEnv())
                            .setVersion(e.getValue().getVersion())
                            .setUid(requestWatcher.uid())
                            .setForce(false)
                            .setStatus(RequestStatus.REGISTER.ordinal()).build();

                    sendRequest(requestWatcher, entityClassSyncRequest);
                    //  刷新注册时间
                    e.getValue().setRegisterTime(System.currentTimeMillis());
                    logger.info("reRegister success uid [{}], appId [{}], env [{}], version [{}].",
                        requestWatcher.uid(), e.getKey(), e.getValue().getEnv(), e.getValue().getVersion());
                } catch (Exception ex) {
                    isOperationOK = false;
                    logger.warn("reRegister watcherElement-[{}] failed, message : {}", e.getValue().toString(),
                        ex.getMessage());
                    requestWatcher.observer().onError(ex);
                    break;
                }
            }
        }
        return isOperationOK;
    }

    @Override
    public void initWatcher(String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {
        requestWatchExecutor.create(uid, streamObserver);
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
                logger.info("register success, uid [{}], appId [{}], env [{}], version [{}] success.",
                    entityClassSyncResponse.getUid(), entityClassSyncResponse.getAppId(),
                    entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion());
            }

        } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {
            //  执行返回结果
            executorService.submit(() -> {
                try {
                    accept(entityClassSyncResponse);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    if (requestWatchExecutor.watcher().isActive()) {
                        requestWatchExecutor.watcher().observer().onError(e);
                    }
                }
            });
        }
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
            logger.warn("stream [{}] has broken, reCreate new stream after ({})ms...", uid, grpcParams.getReconnectDuration());

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

    private void accept(EntityClassSyncResponse entityClassSyncResponse) {

        logger.debug("getSync response, uid [{}], appId [{}], env [{}], version [{}].",
            entityClassSyncResponse.getUid(), entityClassSyncResponse.getAppId(),
            entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion());

        //  执行OQS更新EntityClass
        EntityClassSyncRequest.Builder entityClassSyncRequestBuilder = execute(entityClassSyncResponse);
        if (entityClassSyncRequestBuilder.getStatus() != SYNC_OK.ordinal()) {
            logger.warn("execute data sync fail, [{}]", entityClassSyncRequestBuilder.build().toString());
            return;
        }

        if (!entityClassSyncResponse.getForce()) {
            //  回写处理结果, entityClassSyncRequest为空则代表传输存在问题.
            sendRequestWithALiveCheck(requestWatchExecutor.watcher(),
                entityClassSyncRequestBuilder.setUid(entityClassSyncResponse.getUid()).build());
        }

        logger.debug("sync data fin, uid [{}], appId [{}], env [{}], version [{}], status[{}].",
            entityClassSyncResponse.getUid(), entityClassSyncResponse.getAppId(),
            entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion(),
            entityClassSyncRequestBuilder.getStatus());
    }

    /**
     * 执行方法.
     */
    @SuppressWarnings("unchecked")
    private EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse) {
        RequestStatus status = SYNC_FAIL;
        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
        try {
            if (entityClassSyncResponse.getAppId().isEmpty()
                || entityClassSyncResponse.getAppId().isEmpty()
                || NOT_EXIST_VERSION == entityClassSyncResponse.getVersion()) {
                throw new MetaSyncClientException("sync appId or version could not be null...", false);
            }

            builder.setAppId(entityClassSyncResponse.getAppId())
                .setVersion(entityClassSyncResponse.getVersion())
                .setEnv(entityClassSyncResponse.getEnv());

            //  该方法返回的错误不会导致重新连接、但会通知服务端本次推送更新失败
            try {
                //  md5 check && 是否已存在版本判断.
                EntityClassSyncRspProto result = entityClassSyncResponse.getEntityClassSyncRspProto();
                if (md5Check(entityClassSyncResponse.getMd5(), result)) {
                    WatchElement w =
                        new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), Confirmed);
                    //  当前关注此版本
                    if (entityClassSyncResponse.getForce() || requestWatchExecutor.watcher().onWatch(w)) {
                        //  执行外部传入的执行器
                        try {
                            status = syncExecutor
                                .sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(),
                                    result) ? RequestStatus.SYNC_OK : SYNC_FAIL;
                        } catch (Exception e) {
                            status = DATA_ERROR;
                            logger.warn(e.getMessage());
                        }

                        if (status == RequestStatus.SYNC_OK) {
                            requestWatchExecutor.update(w);
                        }
                    } else {
                        logger.warn("current oqs-version bigger than sync-version : {}, will ignore...",
                            entityClassSyncResponse.getVersion());
                        status = RequestStatus.SYNC_OK;
                    }
                }

            } catch (Exception e) {
                logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
        } finally {
            metrics(status);
        }

        return builder.setStatus(status.ordinal());
    }

    private void metrics(RequestStatus status) {
        switch (status) {
            case SYNC_OK:
                acceptDataHandleErrorCounter.set(0);
                acceptDataFormatCounter.set(0);
                break;
            case SYNC_FAIL:
                acceptDataHandleErrorCounter.incrementAndGet();
                break;
            case DATA_ERROR:
                acceptDataFormatCounter.incrementAndGet();
                break;
            default:
                break;
        }
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
                        if (System.currentTimeMillis() - requestWatcher.heartBeat() > grpcParams.getDefaultHeartbeatTimeout()) {
                            requestWatcher.observer().onCompleted();
                            logger.warn("last heartbeat time [{}] reaches max timeout [{}]",
                                System.currentTimeMillis() - requestWatcher.heartBeat(),
                                grpcParams.getDefaultHeartbeatTimeout());
                        }
                    }

                    EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                    sendRequestWithALiveCheck(requestWatcher, request);
                } catch (Exception e) {
                    //  ignore
                    logger.warn("send keepAlive failed, message [{}], but exception will ignore due to retry...",
                        e.getMessage());
                }
                logger.debug("keepAlive ok, print next check after ({})ms...", grpcParams.getKeepAliveSendDuration());
            }
            TimeWaitUtils.wakeupAfter(grpcParams.getKeepAliveSendDuration(), TimeUnit.MILLISECONDS);
        }
        logger.debug("keepAlive task has quited due to sync-client shutdown...");
        return true;
    }


    /**
     * 检查当前APP的注册状态，处于INIT、REGISTER的APP将被重新注册到服务端.
     */
    private boolean watchElementCheck() {
        logger.debug("start appCheck task ok...");
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
                            builder.setUid(requestWatcher.uid()).setAppId(k.getAppId()).setVersion(k.getVersion())
                                .setStatus(RequestStatus.REGISTER.ordinal()).build();
                        try {
                            sendRequestWithALiveCheck(requestWatcher, entityClassSyncRequest);
                            k.setRegisterTime(System.currentTimeMillis());
                        } catch (Exception e) {
                            k.setStatus(Init);
                        }
                    }
                );
                if (counter % PRINT_CHECK_DURATION == 0) {
                    logger.debug("app check ok, print next check after ({})ms...",
                        grpcParams.getMonitorSleepDuration() * PRINT_CHECK_DURATION);
                }
                counter++;
            }
            TimeWaitUtils.wakeupAfter(grpcParams.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }
        logger.debug("appCheck task has quited due to sync-client shutdown...");

        return true;
    }

    /**
     * only test use， not interface.
     */
    public Queue<WatchElement> getForgotQueue() {
        return forgotQueue;
    }
}
