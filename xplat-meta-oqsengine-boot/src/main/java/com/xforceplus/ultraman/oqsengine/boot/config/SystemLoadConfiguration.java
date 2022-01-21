package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.load.DefaultSystemLoadEvaluator;
import com.xforceplus.ultraman.oqsengine.common.load.SystemLoadEvaluator;
import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.CpuLoadFactor;
import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.HeapMemoryLoadFactory;
import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.LoadFactor;
import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.ThreadPoolExecutorLoadFactor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统负载定义.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 16:16
 * @since 1.8
 */
@Configuration
public class SystemLoadConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SystemLoadConfiguration.class);

    /**
     * cpu 负载因子.
     */
    @Bean
    @ConditionalOnExpression("'${load.enabled}'.equals('true')")
    public LoadFactor cpuLoadFactor(@Value("${load.weight.cpu:1.0}") double weight) {

        logger.info("Start the cpu load factor with a weight of {}.", weight);

        return new CpuLoadFactor(weight);
    }

    /**
     * 堆内存负载因子.
     */
    @Bean
    @ConditionalOnExpression("'${load.enabled}'.equals('true')")
    public LoadFactor heapMemoryLoadFactory(@Value("${load.weight.heap:1.0}") double weight) {

        logger.info("Start the heap memory load factor with a weight of {}.", weight);

        return new HeapMemoryLoadFactory(weight);
    }

    /**
     * IO线程负载因子.
     */
    @Bean
    @ConditionalOnExpression("'${load.enabled}'.equals('true')")
    public LoadFactor ioThreadPoolLoadFactor(
        @Value("${load.weight.io:1.0}") double weight,
        ExecutorService ioThreadPool
    ) {

        logger.info("Start the IO thread load factor with a weight of {}.", weight);

        return new ThreadPoolExecutorLoadFactor((ThreadPoolExecutor) ioThreadPool, weight);
    }

    @Bean
    public SystemLoadEvaluator systemLoadEvaluator() {
        return new DefaultSystemLoadEvaluator();
    }
}
