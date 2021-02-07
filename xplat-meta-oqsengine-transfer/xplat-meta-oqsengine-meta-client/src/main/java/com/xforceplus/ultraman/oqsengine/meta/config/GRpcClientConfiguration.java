package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.IEntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.IEntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

/**
 * desc :
 * name : GRpcClientConfiguration
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.client", name = "enabled", havingValue = "true")
public class GRpcClientConfiguration {

    @Bean
    public GRpcParamsConfig gRpcParamsConfig(
            @Value("${grpc.timeout.seconds.heartbeat:30}") long heartbeatTimeoutSec,
            @Value("${grpc.sleep.seconds.monitor:1}") long sleepMonitorSec,
            @Value("${grpc.sleep.seconds.reconnect:5}") long sleepReconnectSec,
            @Value("${grpc.keep.alive.seconds.duration:5}") long keepAliveSendDuration) {
        GRpcParamsConfig gRpcParamsConfig = new GRpcParamsConfig();
        gRpcParamsConfig.setDefaultHeartbeatTimeout(TimeUnit.SECONDS.toMillis(heartbeatTimeoutSec));
        gRpcParamsConfig.setMonitorSleepDuration(TimeUnit.SECONDS.toMillis(sleepMonitorSec));
        gRpcParamsConfig.setReconnectDuration(TimeUnit.SECONDS.toMillis(sleepReconnectSec));
        gRpcParamsConfig.setKeepAliveSendDuration(TimeUnit.SECONDS.toMillis(keepAliveSendDuration));

        return gRpcParamsConfig;
    }

    @Bean("oqsSyncThreadPool")
    public ExecutorService asyncDispatcher(
            @Value("${threadPool.call.grpc.client.worker:0}") int worker,
            @Value("${threadPool.call.grpc.client.queue:500}") int queue) {
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
    public GRpcClient metaSyncGRpcClient(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port
    ) {
        MetaSyncGRpcClient metaSyncGRpcClient = new MetaSyncGRpcClient(host, port);
        metaSyncGRpcClient.create();

        return metaSyncGRpcClient;
    }

    @Bean
    public IEntityClassExecutor entityClassExecutor() {
        return new EntityClassExecutor();
    }

    @Bean
    public IRequestWatchExecutor requestWatchExecutor() {
        return new RequestWatchExecutor();
    }

    @Bean
    public IEntityClassSyncClient entityClassSyncClient() {
        return new EntityClassSyncClient();
    }
}
