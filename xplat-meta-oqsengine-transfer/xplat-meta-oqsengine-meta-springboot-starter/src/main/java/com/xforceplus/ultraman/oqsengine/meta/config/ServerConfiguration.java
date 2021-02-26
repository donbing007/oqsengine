package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.ITransferExecutor;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.listener.EntityClassListener;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.IShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ServerShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ShutDownExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

/**
 * desc :
 * name : ServerConfiguration
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.on", name = "side", havingValue = "server")
public class ServerConfiguration {

    @Bean
    public GRpcParamsConfig gRpcParamsConfig(
            @Value("${grpc.timeout.seconds.heartbeat:30}") long heartbeatTimeoutSec,
            @Value("${grpc.timeout.seconds.delay.task:30}") long delayTaskDurationSec,
            @Value("${grpc.sleep.seconds.monitor:1}") long sleepMonitorSec,
            @Value("${grpc.sleep.seconds.reconnect:5}") long sleepReconnectSec,
            @Value("${grpc.keep.alive.seconds.duration:5}") long keepAliveSendDuration) {
        GRpcParamsConfig gRpcParamsConfig = new GRpcParamsConfig();
        gRpcParamsConfig.setDefaultHeartbeatTimeout(TimeUnit.SECONDS.toMillis(heartbeatTimeoutSec));
        gRpcParamsConfig.setDefaultDelayTaskDuration(TimeUnit.SECONDS.toMillis(delayTaskDurationSec));
        gRpcParamsConfig.setMonitorSleepDuration(TimeUnit.SECONDS.toMillis(sleepMonitorSec));
        gRpcParamsConfig.setReconnectDuration(TimeUnit.SECONDS.toMillis(sleepReconnectSec));
        gRpcParamsConfig.setKeepAliveSendDuration(TimeUnit.SECONDS.toMillis(keepAliveSendDuration));

        return gRpcParamsConfig;
    }

    @Bean
    public GRpcServer gRpcServer() {
        return new GRpcServer();
    }

    @Bean
    public ResponseWatchExecutor watchExecutor() {
        return new ResponseWatchExecutor();
    }

    @Bean
    public IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor() {
        return new RetryExecutor();
    }

    @Bean
    public SyncResponseHandler entityClassProvider() {
        SyncResponseHandler responseHandler = new SyncResponseHandler();

        responseHandler.start();

        return responseHandler;
    }

    @Bean
    public ITransferExecutor entityClassSyncServer() {
        return new EntityClassSyncServer();
    }


    @Bean("grpcWorkThreadPool")
    public ExecutorService metaSyncThreadPool(
            @Value("${threadPool.call.grpc.worker:0}") int worker,
            @Value("${threadPool.call.grpc.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "meta-sync-call", false);
    }

    @Bean("grpcServerExecutor")
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

        return buildThreadPool(useWorker, useQueue, "grpc-server-call", false);
    }

    @Bean
    public EntityClassListener entityClassListener() {
        return new EntityClassListener();
    }

    @Bean
    public ShutDownExecutor shutDownExecutor() {
        return shutDownExecutor();
    }

    @Bean
    public IShutDown shutDown() {
        return new ServerShutDown();
    }
}
