package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.core.service.impl.CombinedStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:02
 * @since 1.8
 */
@Configuration
public class StorageConfiguration {

    @Bean
    public MasterStorage masterStorage(
        @Value("${storage.master.name:oqsbigentity}") String tableName,
        @Value("${storage.timeoutMs.query:3000}") long masterQueryTimeout) {
        SQLMasterStorage storage = new SQLMasterStorage();
        storage.setTableName(tableName);
        storage.setQueryTimeout(masterQueryTimeout);
        return storage;
    }

    @Bean
    public IndexStorage indexStorage(
        @Value("${storage.index.search.name:oqsindex}") String searchIndexName,
        @Value("${storage.index.search.maxQueryTimeMs:0}") long maxQueryTimeMs,
        @Value("${storage.index.search.maxBatchSize:20}") int maxBatchSize) {

        SphinxQLIndexStorage storage = new SphinxQLIndexStorage();
        storage.setSearchIndexName(searchIndexName);
        storage.setMaxSearchTimeoutMs(maxQueryTimeMs);
        storage.setMaxBatchSize(maxBatchSize);
        return storage;
    }

    @Bean
    public CombinedStorage combinedStorage(MasterStorage masterStorage, IndexStorage indexStorage) {
        CombinedStorage combinedStorage = new CombinedStorage(masterStorage, indexStorage);
        return combinedStorage;
    }

    @Bean
    public StorageStrategyFactory masterStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());
        return storageStrategyFactory;
    }

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
}
