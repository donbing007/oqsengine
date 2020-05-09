package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.kubernetesStatefulsetNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.DecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
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
    @Bean
    public NodeIdGenerator kubernetesStatefulsetNodeIdGenerator() {
        return new kubernetesStatefulsetNodeIdGenerator();
    }

    @ConditionalOnProperty(name = "instance.type", havingValue = "static", matchIfMissing = true)
    @Bean
    public NodeIdGenerator staticNodeIdGenerator(@Value("${instance.id:0}") int instanceId) {
        return new StaticNodeIdGenerator(instanceId);
    }

    @Bean
    public LongIdGenerator longIdGenerator(NodeIdGenerator nodeIdGenerator) {
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

    @Bean
    public ExecutorService threadPool(@Value("${threadPool.size:0}") int size) {
        if (size == 0) {
            size = Runtime.getRuntime().availableProcessors() + 1;
        }

        return new ThreadPoolExecutor(size, size,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(500),
            ExecutorHelper.buildNameThreadFactory("oqs-engine", false),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean(name = "dispatcher")
    public ExecutorService asyncDispatcher(@Value("${dispatcher.threadPool.size:10}") int size) {
        if (size == 0) {
            size = Runtime.getRuntime().availableProcessors() + 1;
        }

        return new ThreadPoolExecutor(size, size,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(500),
            ExecutorHelper.buildNameThreadFactory("grpc-blocking", false),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
