package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLManticoreIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterUniqueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.UniqueMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SimpleFieldKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 储存配置.
 *
 * @author dongbin
 * @version 0.1 2020/2/24 17:02
 * @since 1.8
 */
@Configuration
public class StorageConfiguration {

    /**
     * 主库存储存.
     */
    @Bean
    public MasterStorage masterStorage(
        @Value("${storage.master.name:oqsbigentity}") String tableName,
        @Value("${storage.timeoutMs.query:3000}") long masterQueryTimeout) {
        SQLMasterStorage storage = new SQLMasterStorage();
        storage.setTableName(tableName);
        storage.setQueryTimeout(masterQueryTimeout);
        return storage;
    }

    /**
     * 索引存储存.
     */
    @Bean
    public IndexStorage indexStorage(
        @Value("${storage.index.search.name:oqsindex}") String searchIndexName,
        @Value("${storage.index.search.maxQueryTimeMs:3000}") long maxQueryTimeMs) {

        SphinxQLManticoreIndexStorage storage = new SphinxQLManticoreIndexStorage();
        storage.setMaxSearchTimeoutMs(maxQueryTimeMs);
        storage.setSearchIndexName(searchIndexName);
        return storage;
    }

    /**
     * 主库存储存策略工厂.
     */
    @Bean
    public StorageStrategyFactory masterStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());
        return storageStrategyFactory;
    }

    /**
     * 索引储存策略工厂.
     */
    @Bean
    public StorageStrategyFactory indexStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        return storageStrategyFactory;
    }

    @Bean
    public SphinxQLConditionsBuilderFactory indexConditionsBuilderFactory() {
        return new SphinxQLConditionsBuilderFactory();
    }

    @Bean
    public SQLJsonConditionsBuilderFactory masterConditionsBuilderFactory() {
        return new SQLJsonConditionsBuilderFactory();
    }

    @ConditionalOnExpression("${storage.index.write.shard.enabled:false} == true")
    @Bean("indexWriteIndexNameSelector")
    public Selector<String> shardingIndexWriteIndexNameSelector(
        @Value("${storage.index.write.name:oqsindex}") String baseIndexName,
        @Value("${storage.index.write.shard.size:1}") int shardSize) {
        return new SuffixNumberHashSelector(baseIndexName, shardSize);
    }

    @ConditionalOnExpression("${storage.index.write.shard.enabled:false} == false")
    @Bean("indexWriteIndexNameSelector")
    public Selector<String> noShardingIndexWriteIndexNameSelector(
        @Value("${storage.index.write.name:oqsindex}") String baseIndexName) {
        return new NoSelector(baseIndexName);
    }

    //TODO
    @Bean
    public UniqueMasterStorage uniqueMasterStorage() {
        return new MasterUniqueStorage();
    }

    @Bean
    public UniqueKeyGenerator generator() {
        return new SimpleFieldKeyGenerator();
    }
}
