package com.xforceplus.ultraman.oqsengine.meta;

import com.google.common.util.concurrent.Uninterruptibles;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;


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
    private GRpcParamsConfig gRpcParamsConfig;

    private Thread observerStreamMonitorThread;



    @PostConstruct
    @Override
    public void start() {
        client.start();

        if (!client.opened()) {
            throw new MetaSyncClientException("client stub create failed.", true);
        }

        /**
         * 启动observerStream监控, 启动一个新的线程进行stream的监听
         */
        observerStreamMonitorThread = ThreadUtils.create(this::observerStreamMonitor);

        observerStreamMonitorThread.start();

        requestHandler.start();

        logger.info("entityClassSyncClient start.");
    }

    @Override
    public void stop() {

        requestHandler.stop();

        ThreadUtils.shutdown(observerStreamMonitorThread, SHUT_DOWN_WAIT_TIME_OUT);

        if (client.opened()) {
            client.stop();
        }
        logger.info("entityClassSyncClient stop.");
    }



    /**
     * observerStream监控
     */
    private boolean observerStreamMonitor() {
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
             * 判断是服务重启还是断流
             */
            requestHandler.watchExecutor().create(uid, streamObserver);

            /**
             * 重新注册所有watchList到服务段
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
                 * 这里线设置睡眠再进行资源清理
                 */
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getReconnectDuration(), TimeUnit.MILLISECONDS);

                requestHandler.watchExecutor().release(uid);
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
                requestHandler.onNext(entityClassSyncResponse, null);
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
