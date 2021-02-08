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
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

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

    private Thread monitorThread;

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
        monitorThread = new Thread(() -> {

            while (true) {
                CountDownLatch countDownLatch = new CountDownLatch(1);

                StreamObserver<EntityClassSyncRequest> streamObserver = null;
                String uid = UUID.randomUUID().toString();
                try {
                    /**
                     * 初始化observer，如果失败，说明当前连接不可用，将等待5秒后重试
                     */
                    streamObserver = startObserver(uid, countDownLatch);
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
                if (entityClassExecutor.reRegister()) {
                    /**
                     * 设置服务可用
                     */
                    requestWatchExecutor.watcher().onServe();
                    /**
                     * wait直到countDownLatch = 0;
                     */
                    Uninterruptibles.awaitUninterruptibly(countDownLatch);
                }

                /**
                 * 设置服务不可用
                 */
                requestWatchExecutor.watcher().notServer();

                logger.warn("stream has broken, will re-create new one after ({})ms...", gRpcParamsConfig.getReconnectDuration());
                /**
                 * 这里线设置睡眠再进行资源清理
                 */
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);

                requestWatchExecutor.release();
            }
        });

        monitorThread.start();
    }


    /**
     * 初始化stream实现方法
     *
     * @param countDownLatch
     */
    private StreamObserver<EntityClassSyncRequest> startObserver(String uid, CountDownLatch countDownLatch) {
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
                     * 执行返回结果
                     */
                    try {
                        entityClassExecutor.accept(entityClassSyncResponse);
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                        onError(e);
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

}
