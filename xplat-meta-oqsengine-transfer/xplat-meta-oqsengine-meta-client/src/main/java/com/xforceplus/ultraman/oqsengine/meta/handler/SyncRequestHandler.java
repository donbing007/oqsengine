package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.CLIENT_TASK_COUNT;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequestWithALiveCheck;

/**
 * desc :
 * name : EntityClassExecutorService
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public class SyncRequestHandler implements IRequestHandler {

    private Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

    @Resource
    private SyncExecutor syncExecutor;

    @Resource
    private IRequestWatchExecutor requestWatchExecutor;

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    @Resource(name = "grpcTaskExecutor")
    private ExecutorService executorService;

    private Queue<WatchElement> forgotQueue = new ConcurrentLinkedDeque<>();
    private List<Thread> longRunTasks = new ArrayList<>(CLIENT_TASK_COUNT);

    private volatile boolean isShutdown = false;

    @Override
    public void start() {
        isShutdown = false;

        /**
         * 1.添加watchElementCheck任务线程
         */
        longRunTasks.add(ThreadUtils.create(this::keepAlive));


        /**
         * 2.添加watchElementCheck任务线程
         */
        longRunTasks.add(ThreadUtils.create(this::watchElementCheck));

        /**
         * 启动所有任务线程
         */
        longRunTasks.forEach(Thread::start);

        logger.info("requestWatchExecutor start.");
    }

    @Override
    public void stop() {

        isShutdown = true;

        requestWatchExecutor.stop();

        longRunTasks.forEach(s -> {
            ThreadUtils.shutdown(s, SHUT_DOWN_WAIT_TIME_OUT);
        });
    }

    @Override
    public boolean register(WatchElement watchElement) {
        return register(Collections.singletonList(watchElement));
    }

    @Override
    public synchronized boolean register(List<WatchElement> appIdEntries) {
        RequestWatcher watcher = requestWatchExecutor.watcher();
        /**
         * 这里只判断是否watcher为空，如果服务watcher不为空
         */

        if (null == watcher) {
            logger.warn("current gRpc-client is not init, can't offer appIds:{}."
                    , appIdEntries.stream().map(WatchElement::getAppId).collect(Collectors.toList()));
            forgotQueue.addAll(appIdEntries);

            return false;
        }

        AtomicBoolean ret = new AtomicBoolean(true);
        appIdEntries.stream()
                .filter(s -> {
                    if (watcher.watches().containsKey(s.getAppId())) {
                        logger.info("appId : {} is already in watchList, will ignore...", s.getAppId());
                        return false;
                    } else {
                        logger.info("add appId : {} in watchList", s.getAppId());
                        return true;
                    }
                })
                .forEach(
                        v -> {
                            /**
                             * 当前requestWatch不可用或发生UID切换时,先加入forgot列表
                             */
                            if (!requestWatchExecutor.isAlive(watcher.uid())) {
                                forgotQueue.add(v);
                                ret.set(false);
                            } else {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setUid(watcher.uid())
                                                .setAppId(v.getAppId())
                                                .setEnv(v.getEnv())
                                                .setVersion(v.getVersion())
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                requestWatchExecutor.add(v);

                                try {
                                    sendRequestWithALiveCheck(requestWatchExecutor.watcher(), entityClassSyncRequest);

                                    logger.info("register success uid [{}], appId [{}], env [{}], version [{}]."
                                            , watcher.uid(), v.getAppId(), v.getEnv(), v.getVersion());
                                } catch (Exception e) {
                                    v.setStatus(WatchElement.AppStatus.Init);
                                    ret.set(false);
                                }
                            }
                        }
                );

        logger.info("current watchList status : {}", watcher.watches().toString());

        return ret.get();
    }

    /**
     * 当发生断线时，需要重新连接，每次重新连接后，需要将当前OQS的WatchList重新注册到元数据中
     */
    public boolean reRegister() {
        /**
         * 当开启reRegister操作时，所有的register操作将被中断
         */
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
                                    .setStatus(RequestStatus.REGISTER.ordinal()).build();

                    sendRequest(requestWatcher, entityClassSyncRequest);
                    /**
                     * 刷新注册时间
                     */
                    e.getValue().setRegisterTime(System.currentTimeMillis());
                    logger.info("reRegister success uid [{}], appId [{}], env [{}], version [{}]."
                                        , requestWatcher.uid(), e.getKey(), e.getValue().getEnv(), e.getValue().getVersion());
                } catch (Exception ex) {
                    isOperationOK = false;
                    logger.warn("reRegister watcherElement-[{}] failed, message : {}", e.getValue().toString(), ex.getMessage());
                    requestWatcher.observer().onError(ex);
                    break;
                }
            }
        }
        return isOperationOK;
    }

    @Override
    public void onNext(EntityClassSyncResponse entityClassSyncResponse, Void nil) {
        /**
         * 重启中所有的Response将被忽略
         * 不是同批次请求将被忽略
         */
        if (!requestWatchExecutor.isAlive(entityClassSyncResponse.getUid())) {
            return;
        }

        /**
         * reset heartbeat
         */
        requestWatchExecutor.resetHeartBeat(entityClassSyncResponse.getUid());

        /**
         * 更新状态
         */
        if (entityClassSyncResponse.getStatus() == RequestStatus.REGISTER_OK.ordinal()) {
            boolean ret = requestWatchExecutor.update(new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                    entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed));

            if (ret) {
                logger.debug("register success, uid [{}], appId [{}], env [{}], version [{}] success.",
                        entityClassSyncResponse.getUid(), entityClassSyncResponse.getAppId(),
                        entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion());
            }

        } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {
            /**
             * 执行返回结果
             */
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

    private void accept(EntityClassSyncResponse entityClassSyncResponse) {
        /**
         * 执行OQS更新EntityClass
         */
        EntityClassSyncRequest.Builder entityClassSyncRequestBuilder = execute(entityClassSyncResponse);
        if (entityClassSyncRequestBuilder.getStatus() != SYNC_OK.ordinal()) {
            logger.warn("execute data sync fail, [{}]", entityClassSyncRequestBuilder.build().toString());
            return;
        }
        /**
         * 回写处理结果, entityClassSyncRequest为空则代表传输存在问题.
         */
        sendRequestWithALiveCheck(requestWatchExecutor.watcher(),
                            entityClassSyncRequestBuilder.setUid(entityClassSyncResponse.getUid()).build());


        logger.debug("sync data fin, uid [{}], appId [{}], env [{}], version [{}], status[{}].",
                entityClassSyncResponse.getUid(), entityClassSyncResponse.getAppId(),
                entityClassSyncResponse.getEnv(), entityClassSyncResponse.getVersion(), entityClassSyncRequestBuilder.getStatus());
    }

    /**
     * 执行方法
     *
     * @param entityClassSyncResponse
     * @return EntityClassSyncRequest.Builder
     */
    @SuppressWarnings("unchecked")
    private EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse) {
        int status = SYNC_FAIL.ordinal();
        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
        try {
            if (null == entityClassSyncResponse.getAppId() || entityClassSyncResponse.getAppId().isEmpty() ||
                    NOT_EXIST_VERSION == entityClassSyncResponse.getVersion()) {
                throw new MetaSyncClientException("sync appId or version could not be null...", false);
            }

            builder.setAppId(entityClassSyncResponse.getAppId())
                    .setVersion(entityClassSyncResponse.getVersion())
                    .setEnv(entityClassSyncResponse.getEnv());

            /**
             * 该方法返回的错误不会导致重新连接、但会通知服务端本次推送更新失败
             */
            try {
                /**
                 * md5 check && 是否已存在版本判断
                 */
                EntityClassSyncRspProto result = entityClassSyncResponse.getEntityClassSyncRspProto();
                if (md5Check(entityClassSyncResponse.getMd5(), result)) {
                    WatchElement w = new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed);
                    /**
                     * 当前关注此版本
                     */
                    if (requestWatchExecutor.watcher().onWatch(w)) {
                        /**
                         * 执行外部传入的执行器
                         */
                        try {
                            status = syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(), result) ?
                                    RequestStatus.SYNC_OK.ordinal() : SYNC_FAIL.ordinal();
                        } catch (Exception e) {
                            status = DATA_ERROR.ordinal();
                            logger.warn(e.getMessage());
                        }

                        if (status == RequestStatus.SYNC_OK.ordinal()) {
                            requestWatchExecutor.update(w);
                        }
                    } else {
                        logger.warn("current oqs-version bigger than sync-version : {}, will ignore...",
                                entityClassSyncResponse.getVersion());
                        status = RequestStatus.SYNC_OK.ordinal();
                    }
                }

            } catch (Exception e) {
                logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
        }

        return builder.setStatus(status);
    }

    private boolean md5Check(String md5, EntityClassSyncRspProto entityClassSyncRspProto) {
        if (null == md5 || md5.isEmpty() || null == entityClassSyncRspProto) {
            return false;
        }
        return md5.equals(getMD5(entityClassSyncRspProto.toByteArray()));
    }

    /**
     * 保持和服务端的KeepAlive
     * @return
     */
    private boolean keepAlive() {
        logger.debug("start keepAlive task ok...");
        while (!isShutDown()) {
            RequestWatcher requestWatcher = requestWatchExecutor.watcher();
            if (null != requestWatcher) {
                try {
                    if (requestWatcher.isActive()) {
                        if (System.currentTimeMillis() - requestWatcher.heartBeat() >
                                gRpcParamsConfig.getDefaultHeartbeatTimeout()) {
                            requestWatcher.observer().onCompleted();
                            logger.warn("last heartbeat time [{}] reaches max timeout [{}]"
                                    , System.currentTimeMillis() - requestWatcher.heartBeat(),
                                    gRpcParamsConfig.getDefaultHeartbeatTimeout());
                        }
                    }

                    EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                            .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                    sendRequestWithALiveCheck(requestWatcher, request);
                } catch (Exception e) {
                    //  ignore
                    logger.warn("send keepAlive failed, message [{}], but exception will ignore due to retry...", e.getMessage());
                }
                logger.debug("keepAlive ok, next check after duration ({})ms...", gRpcParamsConfig.getKeepAliveSendDuration());
            }
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getKeepAliveSendDuration(), TimeUnit.MILLISECONDS);
        }
        logger.debug("keepAlive task has quited due to sync-client shutdown...");
        return true;
    }


    /**
     * 检查当前APP的注册状态，处于INIT、REGISTER的APP将被重新注册到服务端
     * @return
     */
    private boolean watchElementCheck() {
        logger.debug("start appCheck task ok...");
        while (!isShutDown()) {

            RequestWatcher requestWatcher = requestWatchExecutor.watcher();
            if (null != requestWatcher && requestWatcher.isActive()) {
                /**
                 * 将forgetQueue中的数据添加到watch中
                 */
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
                    /**
                     * 过滤出INIT、已经长时间处于REGISTER的watchElement
                     * 时间超长的判定标准为当前时间-最后注册时间 > delay时间
                     */
                    return s.getStatus().ordinal() == WatchElement.AppStatus.Init.ordinal() ||
                            (s.getStatus().ordinal() < WatchElement.AppStatus.Confirmed.ordinal() &&
                                    System.currentTimeMillis() - s.getRegisterTime() > gRpcParamsConfig.getDefaultDelayTaskDuration());
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
                                k.setStatus(WatchElement.AppStatus.Init);
                            }
                        }
                );

                logger.debug("app check ok, next check after duration ({})ms...", gRpcParamsConfig.getMonitorSleepDuration());
            }
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }
        logger.debug("appCheck task has quited due to sync-client shutdown...");

        return true;
    }

    /**
     * only test use， not interface
     * @return
     */
    public Queue<WatchElement> getForgotQueue() {
        return forgotQueue;
    }
}