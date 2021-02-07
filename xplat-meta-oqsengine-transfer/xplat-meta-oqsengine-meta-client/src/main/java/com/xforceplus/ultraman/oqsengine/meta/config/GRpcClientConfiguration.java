package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.EntityClassExecutorService;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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

    @Bean
    public MetaSyncGRpcClient metaSyncGRpcClient(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port
    ) {
        MetaSyncGRpcClient metaSyncGRpcClient = new MetaSyncGRpcClient(host, port);
        metaSyncGRpcClient.create();

        return metaSyncGRpcClient;
    }

    @Bean
    public EntityClassExecutor entityClassExecutor() {
        return new EntityClassExecutorService();
    }

    @Bean
    public RequestWatchExecutor requestWatchExecutor() {
        return new RequestWatchExecutor();
    }
}
