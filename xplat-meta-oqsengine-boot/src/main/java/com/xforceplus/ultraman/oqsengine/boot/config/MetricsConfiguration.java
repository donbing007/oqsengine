package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import io.kontainers.micrometer.akka.AkkaMetricRegistry;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * 指标配置.
 *
 * @author dongbin
 * @version 0.1 2020/4/22 15:01
 * @since 1.8
 */
@Configuration
public class MetricsConfiguration {

    @Resource(name = "callThreadPool")
    private ExecutorService callThreadPool;

    @Bean
    public ExecutorServiceMetrics callExecutorServiceMetrics() {
        ExecutorServiceMetrics esm = new ExecutorServiceMetrics(callThreadPool, MetricsDefine.PREFIX + ".call", Tags.empty());
        esm.bindTo(Metrics.globalRegistry);
        return esm;
    }

    @Bean
    public TimedAspect timedAspect() {
        return new TimedAspect(Metrics.globalRegistry, pjp -> Tags.empty());
    }

    @Bean
    public void register() {
        AkkaMetricRegistry.setRegistry(Metrics.globalRegistry);
    }
}
