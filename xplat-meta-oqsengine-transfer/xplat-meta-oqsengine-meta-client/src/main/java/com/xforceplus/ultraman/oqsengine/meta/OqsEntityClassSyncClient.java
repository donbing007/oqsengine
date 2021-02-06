package com.xforceplus.ultraman.oqsengine.meta;

import com.google.common.util.concurrent.Uninterruptibles;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.dto.StatusElement;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * desc :
 * name : OqsEntityClassSyncClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public class OqsEntityClassSyncClient implements EntityClassSyncClient {

    private Logger logger = LoggerFactory.getLogger(OqsEntityClassSyncClient.class);

    @Resource(name = "gRpcClient")
    private GRpcClient client;

    @Resource(name = "entityClassExecutor")
    private EntityClassExecutor entityClassExecutor;

    private volatile boolean isReRegister = false;

    private static final long reconnectDuration = 5_000;

    private RequestWatcher requestWatcher = null;

    private Map<String, Integer> temp = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        client.create();

        if (!client.opened()) {
            throw new MetaSyncClientException("client stub create failed.", true);
        }
        observerStream();
    }

    public void destroy() throws InterruptedException {
        if (null != requestWatcher && requestWatcher.isRemoved()) {
            requestWatcher.release();
        }

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
                    logger.warn("observer init error, message : {}, retry after ({})ms", reconnectDuration, e.getMessage());
                    TimeWaitUtils.wakeupAfter(reconnectDuration, TimeUnit.MILLISECONDS);
                    continue;
                }
                if (null == requestWatcher) {
                    requestWatcher = new RequestWatcher(UUID.randomUUID().toString(), streamObserver);
                } else {
                    requestWatcher.reset(UUID.randomUUID().toString(), streamObserver);
                }

                /**
                 * 重新注册所有watchList到服务段
                 * 当注册失败时，将直接标记该observer不可用
                 * 注册成功则直接进入wait状态，直到observer上发生错误为止
                 */
                if (reRegister(requestWatcher)) {
                    /**
                     * 设置服务可用
                     */
                    requestWatcher.canServer();
                    /**
                     * wait直到countDownLatch = 0;
                     */
                    Uninterruptibles.awaitUninterruptibly(countDownLatch);
                    /**
                     * 设置服务不可用
                     */
                    requestWatcher.canNotServer();
                }
                requestWatcher.release();

                logger.warn("stream has broken, will re-create new one after ({})ms...", reconnectDuration);
                TimeWaitUtils.wakeupAfter(reconnectDuration, TimeUnit.MILLISECONDS);
            }
        }).start();
    }

    @Override
    public boolean register(String appId, int version) {
        return register(Collections.singletonList(new AbstractMap.SimpleEntry<>(appId, version)));
    }

    @Override
    public synchronized boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries) {

        if (null == requestWatcher) {
            logger.warn("current gRpc-client is not init, can't offer appIds:{}."
                    , appIdEntries.stream().map(AbstractMap.SimpleEntry::getKey).collect(Collectors.toList()));
            return false;
        }

        appIdEntries.stream()
                .filter(s -> {
                    if (requestWatcher.watches().containsKey(s.getKey())) {
                        logger.info("appId : {} is already in watchList, will ignore...", s.getKey());
                        return false;
                    } else {
                        logger.info("add appId : {} in watchList", s.getKey());
                        return true;
                    }
                })
                .forEach(
                        v -> {
                            if (isReRegister || requestWatcher.isRemoved()) {
                                requestWatcher.addWatch(v.getKey(),
                                        new StatusElement(v.getKey(), v.getValue(), StatusElement.Status.Wait));
                            } else {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setUid(requestWatcher.uid()).setAppId(v.getKey()).setVersion(v.getValue())
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                StatusElement.Status status = StatusElement.Status.Register;
                                if (!internalRegister(requestWatcher, entityClassSyncRequest)) {
                                    status = StatusElement.Status.Wait;
                                }
                                requestWatcher.addWatch(v.getKey(),
                                        new StatusElement(v.getKey(), v.getValue(), status));
                            }
                        }
                );
        logger.info("current watchList : {}", requestWatcher.watches().toString());
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
                if (requestWatcher.isRemoved() ||
                        !requestWatcher.uid().equals(entityClassSyncResponse.getUid())) {
                    return;
                }
                /**
                 * reset heartbeat
                 */
                requestWatcher.resetHeartBeat();
                /**
                 * 更新状态
                 */
                if (entityClassSyncResponse.getStatus() == RequestStatus.CONFIRM_REGISTER.ordinal()) {
                    StatusElement element = requestWatcher.watches().get(entityClassSyncResponse.getAppId());
                    if (requestWatcher.onWatch(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion())) {
                        element.setVersion(entityClassSyncResponse.getVersion());
                        element.setStatus(StatusElement.Status.Confirmed);
                    }
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
                        ack(entityClassSyncRequestBuilder.setUid(uid).build());
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
                                    requestWatcher.observer().onNext(entityClassSyncRequest);
                                } catch (Exception e) {
                                    throw new MetaSyncClientException(
                                            String.format("reRegister watchers-[%s] failed.", entityClassSyncRequest.toString()), true);
                                }
                            }
                    );

                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    return false;
                }
            }
            return true;
        } finally {
            isReRegister = false;
        }
    }


    /**
     * 注册appId列表到元数据
     *
     * @param entityClassSyncRequest
     * @return boolean
     */
    private boolean internalRegister(RequestWatcher requestWatcher, EntityClassSyncRequest entityClassSyncRequest) {

        if (null == requestWatcher ||
                null == requestWatcher.observer() ||
                    requestWatcher.isRemoved()) {
            logger.warn("stream observer not exists.");
            return false;
        }

        try {
            requestWatcher.observer().onNext(entityClassSyncRequest);
        } catch (Exception e) {
            logger.warn("register error, entityClassSyncRequest : {}, message : {}"
                                                    , entityClassSyncRequest.toString(), e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 响应response处理结果，告知元数据
     *
     * @param entityClassSyncRequest
     */
    private void ack(EntityClassSyncRequest entityClassSyncRequest) {
        if(null == requestWatcher) {
            logger.warn("stream observer not exists.");
            throw new MetaSyncClientException("stream observer not exists or was expired.", true);
        }
        StreamObserver<EntityClassSyncRequest> observer = requestWatcher.observer();
        if (null != observer ||
                requestWatcher.isRemoved()) {
            logger.warn("stream observer not exists.");
            throw new MetaSyncClientException("stream observer not exists or was expired.", true);
        }

        try {
            requestWatcher.observer().onNext(entityClassSyncRequest);
        } catch (Exception e) {

        }
    }
}
