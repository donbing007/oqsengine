package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServerConfiguration;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.EntityClassSyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.listener.EntityClassListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.defaultHeartbeatTimeout;

/**
 * desc :
 * name : GRpcBeanConfiguration
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "enabled", havingValue = "true")
public class GRpcBeanConfiguration {

    @Bean
    public GRpcServerConfiguration gRpcServerConfiguration() {
        return new GRpcServerConfiguration();
    }

    @Bean
    public GRpcServer gRpcServer() {
        return new GRpcServer();
    }

    @Bean
    public IWatchExecutor watchExecutor(@Value("${grpc.watch.remove.threshold.seconds:0}") long removeThreshold) {

        if (removeThreshold > 0) {
            removeThreshold = TimeUnit.SECONDS.toMillis(removeThreshold);
        } else {
            removeThreshold = defaultHeartbeatTimeout;
        }
        ResponseWatchExecutor watchExecutor = new ResponseWatchExecutor(removeThreshold);

        /**
         * 启动监控线程
         */
        watchExecutor.start();

        return watchExecutor;
    }

    @Bean
    public IRetryExecutor retryExecutor() {
        return new RetryExecutor();
    }

    @Bean
    public EntityClassSyncResponseHandler entityClassProvider() {
        EntityClassSyncResponseHandler responseHandler = new EntityClassSyncResponseHandler();

        responseHandler.start();

        return responseHandler;
    }

    @Bean
    public EntityClassSyncServer entityClassSyncServer() {
        return new EntityClassSyncServer();
    }


    @Bean("gRpcTaskExecutor")
    public ExecutorService gRpcTaskThreadPool(
            @Value("${threadPool.call.grpc.task.worker:0}") int worker,
            @Value("${threadPool.call.grpc.task.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "gRpc-task-call", false);
    }

    @Bean("gRpcServerExecutor")
    public ExecutorService callGRpcThreadPool(
            @Value("${threadPool.call.grpc.server.worker:0}") int worker,
            @Value("${threadPool.call.grpc.server.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "gRpc-server-call", false);
    }

    @Bean
    public EntityClassListener entityClassListener() {
        return new EntityClassListener();
    }

    private ExecutorService buildThreadPool(int worker, int queue, String namePrefix, boolean daemon) {
        return new ThreadPoolExecutor(worker, worker,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queue),
                ExecutorHelper.buildNameThreadFactory(namePrefix, daemon),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
