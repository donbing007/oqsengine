package com.xforceplus.ultraman.oqsengine.meta;

import com.google.common.util.concurrent.Uninterruptibles;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncReqProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * desc :
 * name : DefaultEntityClassSyncClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public class DefaultEntityClassSyncClient implements EntityClassSyncClient {

    private Logger logger = LoggerFactory.getLogger(DefaultEntityClassSyncClient.class);

    @Resource(name = "gRpcClient")
    private GRpcClient client;

    @Resource(name = "entityClassExecutor")
    private EntityClassExecutor entityClassExecutor;

    private StreamObserver<EntityClassSyncRequest> observer;

    private static final List<String> watchList = new ArrayList<>();

    private volatile boolean isReRegister = false;

    private static final long reconnectDuration = 5_000;

    @PostConstruct
    public void start() {
        client.create();

        if (!client.opened()) {
            throw new MetaSyncClientException("client stub create failed.", true);
        }
        observerStream();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (client.opened()) {
            client.destroy();
        }
    }

    /**
     * 初始化observerStream
     */
    public void observerStream() {
        /**
         * 启动一个新的线程进行stream的监听,当发生断流时，将会重新进行stream的创建.
         */
        new Thread(() -> {
            while (true) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                newObserver(countDownLatch);
                /**
                 * 重新注册所有watchList到服务段
                 */
                if (reRegister()) {
                    Uninterruptibles.awaitUninterruptibly(countDownLatch);
                }

                sleepForWait(reconnectDuration);
            }
        }).start();
    }

    /**
     * 等待timeSeconds秒后进行重试
     */
    private void sleepForWait(long timeSeconds) {
        try {
            Thread.sleep(timeSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean register(String appId, int version) {
        return register(Collections.singletonList(new AbstractMap.SimpleEntry<>(appId, version)));
    }

    @Override
    public synchronized boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries) {
        if (isReRegister) {
            logger.warn("current grpc-client is reRegister, can't init appIds:{}."
                    , appIdEntries.stream().map(AbstractMap.SimpleEntry::getKey).collect(Collectors.toList()));
            return false;
        }

        AtomicInteger index = new AtomicInteger(0);
        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
        appIdEntries.stream()
                .filter(s -> {
                    if (watchList.contains(s.getKey())) {
                        logger.info("appId : {} is already in watchList, will ignore...", s.getKey());
                        return false;
                    } else {
                        logger.debug("will add appId : {} in watchList", s.getKey());
                        return true;
                    }
                })
                .forEach(
                        v -> {
                            builder.setEntityClassSyncReqProtos(index.incrementAndGet(),
                                    EntityClassSyncReqProto.newBuilder()
                                            .setAppId(v.getKey())
                                            .setVersion(v.getValue())
                                            .setStatus(RequestStatus.REGISTER.ordinal()).build());
                        }
                );

        if (builder.getEntityClassSyncReqProtosBuilderList().size() > 0) {
            EntityClassSyncRequest entityClassSyncRequest = builder.build();
            if (internalRegister(entityClassSyncRequest)) {
                entityClassSyncRequest.getEntityClassSyncReqProtosList().forEach(
                        s -> {
                            watchList.add(s.getAppId());
                        }
                );
                logger.info("current watchList : {}", watchList.toString());
                return true;
            }
        }

        return false;
    }

    /**
     * 初始化stream实现方法
     *
     * @param countDownLatch
     */
    private void newObserver(CountDownLatch countDownLatch) {
        observer = client.channelStub().register(new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                EntityClassSyncRequest entityClassSyncRequest =
                        entityClassExecutor.execute(entityClassSyncResponse);

                /**
                 * 回写处理结果, entityClassSyncRequest不会为空.
                 */
                try {
                    ack(entityClassSyncRequest);
                } catch (Exception e) {
                    logger.error("stream observer ack error, message-[{}].", e.getMessage());
                    countDownLatch.countDown();
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
    private boolean reRegister() {
        try {
            isReRegister = true;
            /**
             * 当开启reRegister操作时，所有的register操作将被中断
             */

            if (watchList.size() > 0) {
                try {
                    EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
                    AtomicInteger index = new AtomicInteger(0);
                    watchList.forEach(
                            watcher -> {
                                builder.setEntityClassSyncReqProtos(index.getAndIncrement(),
                                        EntityClassSyncReqProto.newBuilder()
                                                .setAppId(watcher)
                                                .setVersion(entityClassExecutor.version(watcher))
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build());
                            }
                    );
                    EntityClassSyncRequest entityClassSyncRequest = builder.build();
                    if (internalRegister(entityClassSyncRequest)) {
                        throw new MetaSyncClientException(
                                String.format("reRegister watchers-[%s] failed.", entityClassSyncRequest.toString()), true);
                    }
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
    private boolean internalRegister(EntityClassSyncRequest entityClassSyncRequest) {

        if (null == observer) {
            logger.warn("stream observer not exists.");
            return false;
        }

        try {
            observer.onNext(entityClassSyncRequest);
        } catch (Exception e) {
            logger.warn("register error, entityClassSyncRequest : {}, message : {}", entityClassSyncRequest.toString(), e.getMessage());
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
        if (null == observer) {
            throw new MetaSyncClientException("stream observer not exists.", true);
        }

        observer.onNext(entityClassSyncRequest);
    }
}
