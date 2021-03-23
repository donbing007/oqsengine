package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ShutDownExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

/**
 * desc :
 * name : CommonConfiguration
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@Configuration
public class CommonConfiguration {
    @Bean
    public GRpcParams gRpcParamsConfig(
            @Value("${meta.grpc.seconds.heartbeatTimeout:30}") long heartbeatTimeoutSec,
            @Value("${meta.grpc.seconds.delaytaskTimeout:30}") long delayTaskDurationSec,
            @Value("${meta.grpc.seconds.monitorDuration:1}") long sleepMonitorSec,
            @Value("${meta.grpc.seconds.reconnectDuration:5}") long sleepReconnectSec,
            @Value("${meta.grpc.seconds.keepAliveDuration:5}") long keepAliveSendDuration) {
        GRpcParams gRpcParamsConfig = new GRpcParams();
        gRpcParamsConfig.setDefaultHeartbeatTimeout(TimeUnit.SECONDS.toMillis(heartbeatTimeoutSec));
        gRpcParamsConfig.setDefaultDelayTaskDuration(TimeUnit.SECONDS.toMillis(delayTaskDurationSec));
        gRpcParamsConfig.setMonitorSleepDuration(TimeUnit.SECONDS.toMillis(sleepMonitorSec));
        gRpcParamsConfig.setReconnectDuration(TimeUnit.SECONDS.toMillis(sleepReconnectSec));
        gRpcParamsConfig.setKeepAliveSendDuration(TimeUnit.SECONDS.toMillis(keepAliveSendDuration));

        return gRpcParamsConfig;
    }


    @Bean("grpcTaskExecutor")
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
    public ShutDownExecutor shutDownExecutor() {
        return new ShutDownExecutor();
    }
}
