package com.xforceplus.ultraman.oqsengine.meta.config;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.CachedMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ShutDownExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 公共配置.
 *
 * @author xujia
 * @since 1.8
 */
@Configuration
@ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
public class CommonConfiguration {
    /**
     * 初始化配置.
     */
    @Bean
    public GRpcParams grpcParamsConfig(
        @Value("${meta.grpc.seconds.heartbeatTimeout:30}") long heartbeatTimeoutSec,
        @Value("${meta.grpc.seconds.delaytaskTimeout:30}") long delayTaskDurationSec,
        @Value("${meta.grpc.seconds.monitorDuration:1}") long sleepMonitorSec,
        @Value("${meta.grpc.seconds.reconnectDuration:5}") long sleepReconnectSec,
        @Value("${meta.grpc.seconds.keepAliveDuration:5}") long keepAliveSendDuration) {
        GRpcParams grpcParamsConfig = new GRpcParams();
        grpcParamsConfig.setDefaultHeartbeatTimeout(TimeUnit.SECONDS.toMillis(heartbeatTimeoutSec));
        grpcParamsConfig.setDefaultDelayTaskDuration(TimeUnit.SECONDS.toMillis(delayTaskDurationSec));
        grpcParamsConfig.setMonitorSleepDuration(TimeUnit.SECONDS.toMillis(sleepMonitorSec));
        grpcParamsConfig.setReconnectDuration(TimeUnit.SECONDS.toMillis(sleepReconnectSec));
        grpcParamsConfig.setKeepAliveSendDuration(TimeUnit.SECONDS.toMillis(keepAliveSendDuration));

        return grpcParamsConfig;
    }

    /**
     * 初始化线程池.
     */
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


    @Bean
    public MetricsRecorder cachedMetricsRecorder() {
        return new CachedMetricsRecorder();
    }
}
