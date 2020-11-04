package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.kubernetesStatefulsetNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.DecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:10
 * @since 1.8
 */
@Configuration
public class CommonConfiguration {

    @ConditionalOnProperty(name = "instance.type", havingValue = "statefulset", matchIfMissing = false)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator kubernetesStatefulsetNodeIdGenerator() {
        return new kubernetesStatefulsetNodeIdGenerator();
    }

    @ConditionalOnProperty(name = "instance.type", havingValue = "static", matchIfMissing = true)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator staticNodeIdGenerator(@Value("${instance.id:0}") int instanceId) {
        return new StaticNodeIdGenerator(instanceId);
    }

    @Bean
    public LongIdGenerator longIdGenerator(@Qualifier("nodeIdGenerator") NodeIdGenerator nodeIdGenerator) {
        return new SnowflakeLongIdGenerator(nodeIdGenerator);
    }

    @ConditionalOnExpression("${storage.master.shard.table.enabled} == true")
    @Bean("tableNameSelector")
    public Selector<String> shardTableNameSelector(
        @Value("${storage.master.name:oqsbigentity}") String masterTableName,
        @Value("${storage.master.shard.table.size:1}") int masterSize) {
        return new SuffixNumberHashSelector(masterTableName, masterSize);
    }

    @ConditionalOnExpression("${storage.master.shard.table.enabled} == false")
    @Bean("tableNameSelector")
    public Selector<String> noShardTableNameSelector(
        @Value("${storage.master.name:oqsbigentity}") String masterTableName) {
        return new NoSelector(masterTableName);
    }

    @Bean
    public SphinxQLConditionsBuilderFactory indexConditionsBuilderFactory() {
        return new SphinxQLConditionsBuilderFactory();
    }

    @Bean
    public StorageStrategyFactory masterStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new DecimalStorageStrategy());
        return storageStrategyFactory;
    }

    @Bean
    public StorageStrategyFactory indexStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        return storageStrategyFactory;
    }

    @Bean("ioThreadPool")
    public ExecutorService ioThreadPool(
        @Value("${threadPool.io.worker:0}") int worker, @Value("${threadPool.io.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-io", false);
    }

    @Bean("callThreadPool")
    public ExecutorService callThreadPool(
        @Value("${threadPool.call.worker:0}") int worker, @Value("${threadPool.call.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call", false);
    }

    private ExecutorService buildThreadPool(int worker, int queue, String namePrefix, boolean daemon) {
        return new ThreadPoolExecutor(worker, worker,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queue),
            ExecutorHelper.buildNameThreadFactory(namePrefix, daemon),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
