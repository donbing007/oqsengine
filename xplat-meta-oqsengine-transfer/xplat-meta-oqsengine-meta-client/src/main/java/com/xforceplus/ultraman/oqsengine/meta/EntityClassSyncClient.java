package com.xforceplus.ultraman.oqsengine.meta;

import com.google.common.util.concurrent.Uninterruptibles;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;

/**
 * desc :
 * name : EntityClassSyncClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public class EntityClassSyncClient implements IEntityClassSyncClient {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncClient.class);

    @Resource(name = "gRpcClient")
    private GRpcClient client;

    @Resource(name = "entityClassExecutor")
    private EntityClassExecutor entityClassExecutor;

    @Resource
    private RequestWatchExecutor requestWatchExecutor;

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    private volatile boolean isReRegister = false;

    @PostConstruct
    @Override
    public void start() {
        client.create();

        if (!client.opened()) {
            throw new MetaSyncClientException("client stub create failed.", true);
        }
        observerStream();
    }

    @Override
    public void destroy() {
        requestWatchExecutor.stop();

        if (client.opened()) {
            client.destroy();
        }
    }

    /**
     * 初始化observerStream
     */
    private void observerStream() {
        /**
         * 启动一个新的线程进行stream的监听,当发生断流时，将会重新进行stream的创建.
         */
        new Thread(() -> {

            while (true) {
                CountDownLatch countDownLatch = new CountDownLatch(1);

                StreamObserver<EntityClassSyncRequest> streamObserver = null;
                String uid = UUID.randomUUID().toString();
                try {
                    /**
                     * 初始化observer，如果失败，说明当前连接不可用，将等待5秒后重试
                     */
                    streamObserver = newObserver(uid, countDownLatch);
                } catch (Exception e) {
                    logger.warn("observer init error, message : {}, retry after ({})ms", gRpcParamsConfig.getReconnectDuration(), e.getMessage());
                    TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);
                    continue;
                }

                /**
                 * 判断是服务重启还是断流
                 */
                requestWatchExecutor.create(uid, streamObserver);

                /**
                 * 重新注册所有watchList到服务段
                 * 当注册失败时，将直接标记该observer不可用
                 * 注册成功则直接进入wait状态，直到observer上发生错误为止
                 */
                if (reRegister(requestWatchExecutor.watcher())) {
                    /**
                     * 设置服务可用
                     */
                    requestWatchExecutor.watcher().canServer();
                    /**
                     * wait直到countDownLatch = 0;
                     */
                    Uninterruptibles.awaitUninterruptibly(countDownLatch);
                }

                /**
                 * 设置服务不可用
                 */
                requestWatchExecutor.watcher().canNotServer();

                logger.warn("stream has broken, will re-create new one after ({})ms...", gRpcParamsConfig.getReconnectDuration());
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);

                requestWatchExecutor.release();
            }
        }).start();
    }

    @Override
    public boolean register(String appId, int version) {
        return register(Collections.singletonList(new AbstractMap.SimpleEntry<>(appId, version)));
    }

    @Override
    public synchronized boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries) {
        RequestWatcher watcher = requestWatchExecutor.watcher();
        if (null == watcher) {
            logger.warn("current gRpc-client is not init, can't offer appIds:{}."
                    , appIdEntries.stream().map(AbstractMap.SimpleEntry::getKey).collect(Collectors.toList()));
            return false;
        }

        appIdEntries.stream()
                .filter(s -> {
                    if (watcher.watches().containsKey(s.getKey())) {
                        logger.info("appId : {} is already in watchList, will ignore...", s.getKey());
                        return false;
                    } else {
                        logger.info("add appId : {} in watchList", s.getKey());
                        return true;
                    }
                })
                .forEach(
                        v -> {
                            if (isReRegister || watcher.isReleased()) {
                                watcher.addWatch(
                                        new WatchElement(v.getKey(), v.getValue(), WatchElement.AppStatus.Init));
                            } else {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setUid(watcher.uid()).setAppId(v.getKey()).setVersion(v.getValue())
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                WatchElement.AppStatus status = WatchElement.AppStatus.Register;
                                try {
                                    sendRequest(requestWatchExecutor.watcher(), entityClassSyncRequest,
                                            requestWatchExecutor.canAccessFunction(), entityClassSyncRequest.getUid());
                                } catch (Exception e) {
                                    status = WatchElement.AppStatus.Init;
                                }
                                watcher.addWatch(
                                        new WatchElement(v.getKey(), v.getValue(), status));
                            }
                        }
                );

        logger.info("current watchList status : {}", watcher.watches().toString());

        return true;
    }

    /**
     * 初始化stream实现方法
     *
     * @param countDownLatch
     */
    private StreamObserver<EntityClassSyncRequest> newObserver(String uid, CountDownLatch countDownLatch) {
        return client.channelStub().register(new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                /**
                 * 重启中所有的Response将被忽略
                 * 不是同批次请求将被忽略
                 */
                if (!requestWatchExecutor.canAccess(entityClassSyncResponse.getUid())) {
                    return;
                }

                /**
                 * reset heartbeat
                 */
                requestWatchExecutor.resetHeartBeat();

                /**
                 * 更新状态
                 */
                if (entityClassSyncResponse.getStatus() == RequestStatus.CONFIRM_REGISTER.ordinal()) {
                   requestWatchExecutor.update(new WatchElement(entityClassSyncResponse.getAppId(),
                            entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed));
                } else {
                    /**
                     * 执行OQS更新EntityClass
                     */
                    EntityClassSyncRequest.Builder entityClassSyncRequestBuilder =
                            entityClassExecutor.execute(entityClassSyncResponse);
                    /**
                     * 回写处理结果, entityClassSyncRequest为空则代表传输存在问题.
                     */
                    try {
                        sendRequest(requestWatchExecutor.watcher(), entityClassSyncRequestBuilder.setUid(uid).build(),
                                                requestWatchExecutor.canAccessFunction(), entityClassSyncResponse.getUid());
                    } catch (Exception ex) {
                        logger.error("stream observer ack error, message-[{}].", ex.getMessage());
                        onError(ex);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("stream observer on error, message-[{}]."
                        , throwable.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
    }


    /**
     * 当发生断线时，需要重新连接，每次重新连接后，需要将当前OQS的WatchList重新注册到元数据中
     */
    private boolean reRegister(RequestWatcher requestWatcher) {
        try {
            isReRegister = true;
            /**
             * 当开启reRegister操作时，所有的register操作将被中断
             */

            if (requestWatcher.watches().size() > 0) {
                try {
                    requestWatcher.watches().forEach(
                            (k, v) -> {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setAppId(k)
                                        .setVersion(entityClassExecutor.version(k))
                                        .setUid(requestWatcher.uid())
                                        .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                try {
                                    sendRequest(requestWatcher, entityClassSyncRequest);
                                } catch (Exception e) {
                                    throw new MetaSyncClientException(
                                            String.format("reRegister watchers-[%s] failed.", entityClassSyncRequest.toString()), true);
                                }
                            }
                    );

                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    requestWatcher.observer().onError(e);
                    return false;
                }
            }
            return true;
        } finally {
            isReRegister = false;
        }
    }


}
