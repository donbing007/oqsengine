package com.xforceplus.ultraman.oqsengine.meta;

import com.google.common.util.concurrent.Uninterruptibles;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.metrics.ConnectorMetricsDefine;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * desc :
 * name : EntityClassSyncClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public class EntityClassSyncClient implements IBasicSyncExecutor {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncClient.class);

    @Resource(name = "gRpcClient")
    private GRpcClient client;

    @Resource(name = "requestHandler")
    private IRequestHandler requestHandler;

    @Resource
    private GRpcParams gRpcParamsConfig;

    private Thread observerStreamMonitorThread;

    private AtomicInteger clientRebuildStreamCounter =
            Metrics.gauge(ConnectorMetricsDefine.CLIENT_CONTINUES_REBUILD_STREAM, new AtomicInteger(0));

    @Override
    @PostConstruct
    public void start() {
        client.start();

        if (!client.opened()) {
            throw new MetaSyncClientException("client stub create failed.", true);
        }

        /**
         * 启动observerStream监控, 启动一个新的线程进行stream的监听
         */
        observerStreamMonitorThread = ThreadUtils.create(this::monitor);

        observerStreamMonitorThread.start();

        requestHandler.start();

        logger.info("entityClassSyncClient start.");
    }

    @Override
    public void stop() {

        requestHandler.stop();

        if (client.opened()) {
            client.stop();
        }
        logger.info("entityClassSyncClient stop.");
    }


    /**
     * streamObserver监控，断流重连逻辑
     */
    private boolean monitor() {
        /**
         * 当发生断流时，将会重新进行stream的创建.
         */
        while (!requestHandler.isShutDown()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            StreamObserver<EntityClassSyncRequest> streamObserver = null;
            String uid = UUID.randomUUID().toString();
            try {
                /**
                 * 初始化observer，如果失败，说明当前连接不可用，将等待5秒后重试
                 */
                streamObserver = responseEvent(countDownLatch);
            } catch (Exception e) {
                logger.warn("observer init error, message : {}, retry after ({})ms"
                        , e.getMessage(), gRpcParamsConfig.getReconnectDuration());
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);
                continue;
            }

            /**
             * 创建uid->streamObserver
             */
            requestHandler.watchExecutor().create(uid, streamObserver);

            /**
             * 重新注册所有watchList到服务端
             * 当注册失败时，将直接标记该observer不可用
             * 注册成功则直接进入wait状态，直到observer上发生错误为止
             */
            if (requestHandler.reRegister()) {
                /**
                 * 设置服务可用
                 */
                requestHandler.watchExecutor().active();
                /**
                 * wait直到countDownLatch = 0;
                 * 由于onNext方法调用reRegister只是将数据写入缓冲区，并不能确认发送是否成功，
                 * 这里如果countDownLatch==0，则说明streamObserver已断开，将等待5秒后重新进行连接
                 */
                Uninterruptibles.awaitUninterruptibly(countDownLatch);
            }

            /**
             * 设置服务不可用
             */
            requestHandler.watchExecutor().inActive();

            /**
             * 如果是服务关闭，则直接跳出while循环
             */
            if (requestHandler.isShutDown()) {
                logger.warn("stream has broken due to client has been shutdown...");
            } else {
                logger.warn("stream [{}] has broken, reCreate new stream after ({})ms..."
                        , uid, gRpcParamsConfig.getReconnectDuration());

                /**
                 * 设置睡眠
                 */
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);

                /**
                 * 进行资源清理
                 */
                requestHandler.watchExecutor().release(uid);

                /**
                 * 断线统计 metrics + 1
                 */
                clientRebuildStreamCounter.incrementAndGet();
            }
        }

        return true;
    }

    /**
     * 初始化stream实现方法
     *
     * @param countDownLatch
     */
    private StreamObserver<EntityClassSyncRequest> responseEvent(CountDownLatch countDownLatch) {
        return client.channelStub().register(new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                /**
                 * 重启中所有的Response将被忽略
                 * 不是同批次请求将被忽略
                 */
                if (requestHandler.watchExecutor().isAlive(entityClassSyncResponse.getUid())) {
                    /**
                     * 当接收到服务端事件，且当前watchExecutor处于活动状态，重置Metrics指标
                     */
                    clientRebuildStreamCounter.set(0);
                    /**
                     * 执行response处理
                     */
                    requestHandler.invoke(entityClassSyncResponse, null);
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
                logger.info("request stream observer completed.");
                countDownLatch.countDown();
            }
        });
    }
}
