package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ClientShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.IShutDown;
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
 * name : ClientConfiguration
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.on", name = "side", havingValue = "client")
public class ClientConfiguration {

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

    @Bean
    public GRpcClient metaSyncClient(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port
    ) {
        MetaSyncGRpcClient metaSyncGRpcClient = new MetaSyncGRpcClient(host, port);
        metaSyncGRpcClient.start();

        return metaSyncGRpcClient;
    }

    @Bean
    public IRequestHandler requestHandler() {
        return new SyncRequestHandler();
    }

    @Bean
    public IRequestWatchExecutor requestWatchExecutor() {
        return new RequestWatchExecutor();
    }

    @Bean
    public EntityClassSyncClient entityClassSyncClient() {
        return new EntityClassSyncClient();
    }

    @Bean
    public ShutDownExecutor shutDownExecutor() {
        return shutDownExecutor();
    }

    @Bean
    public IShutDown shutDown() {
        return new ClientShutDown();
    }
}
