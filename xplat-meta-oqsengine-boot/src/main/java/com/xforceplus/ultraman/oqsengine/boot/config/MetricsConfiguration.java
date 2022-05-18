package com.xforceplus.ultraman.oqsengine.boot.config;

import static com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine.READ_THREAD_POOL;
import static com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine.WRITE_THREAD_POOL;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import io.kontainers.micrometer.akka.AkkaMetricRegistry;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 指标配置.
 *
 * @author dongbin
 * @version 0.1 2020/4/22 15:01
 * @since 1.8
 */
@Configuration
public class MetricsConfiguration {

    @Resource(name = "ioThreadPool")
    private ExecutorService ioThreadPool;

    @Resource(name = "taskThreadPool")
    private ExecutorService taskThreadPool;

    /**
     * 搜索线程池指标.
     */
    @Bean
    public ExecutorServiceMetrics ioExecutorServiceMetrics() {
        ExecutorServiceMetrics esm = new ExecutorServiceMetrics(ioThreadPool,
            MetricsDefine.PREFIX + READ_THREAD_POOL,
            Tags.empty());
        esm.bindTo(Metrics.globalRegistry);
        return esm;
    }

    /**
     * 写入事务执行线程池指标.
     */
    @Bean
    public ExecutorServiceMetrics taskExecutorServiceMetrics() {
        ExecutorServiceMetrics esm = new ExecutorServiceMetrics(taskThreadPool,
            MetricsDefine.PREFIX + WRITE_THREAD_POOL,
            Tags.empty());
        esm.bindTo(Metrics.globalRegistry);
        return esm;
    }

    @Bean
    public TimedAspect timedAspect() {
        return new TimedAspect(Metrics.globalRegistry,
            (Function<ProceedingJoinPoint, Iterable<Tag>>) pjp -> Tags.empty());
    }

    @Bean
    public void register() {
        AkkaMetricRegistry.setRegistry(Metrics.globalRegistry);
    }
}
