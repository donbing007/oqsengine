package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.kubernetesStatefulsetNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer.DefaultSphinxQLQueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.DecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.query.QueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Selector<String> noShardTableNameSelector() {
        return new NoSelector<>();
    }

    @Bean
    public QueryOptimizer indexQueryOptimizer() {
        return new DefaultSphinxQLQueryOptimizer();
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
}
