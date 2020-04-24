package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * 指标配置.
 * @author dongbin
 * @version 0.1 2020/4/22 15:01
 * @since 1.8
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public ExecutorServiceMetrics executorServiceMetrics(ExecutorService threadPool) {
        ExecutorServiceMetrics esm = new ExecutorServiceMetrics(threadPool, MetricsDefine.PREFIX, Tags.empty());
        esm.bindTo(Metrics.globalRegistry);
        return esm;
    }

    @Bean
    public TimedAspect timedAspect() {
        return new TimedAspect(Metrics.globalRegistry, pjp -> Tags.empty());
    }
}
