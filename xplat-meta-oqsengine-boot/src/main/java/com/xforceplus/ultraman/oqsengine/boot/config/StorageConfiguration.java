package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.CombinedSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLManticoreIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.SqlKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SimpleFieldKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
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
     * kv储存.
     *
     * @param tableName 表名.
     * @param timeoutMs 超时时间.
     * @return 实例.
     */
    @Bean
    public KeyValueStorage keyValueStorage(
        @Value("${storage.kv.name:kv}") String tableName,
        @Value("${storage.timeoutMs.query:3000}") long timeoutMs) {
        SqlKeyValueStorage storage = new SqlKeyValueStorage();
        storage.setTableName(tableName);
        storage.setTimeoutMs(timeoutMs);
        return storage;
    }

    /**
     * 主库存储存.
     */
    @Bean
    public MasterStorage masterStorage(
        @Value("${storage.master.name:oqsbigentity}") String tableName,
        @Value("${storage.timeoutMs.query:3000}") long timeoutMs) {
        SQLMasterStorage storage = new SQLMasterStorage();
        storage.setTableName(tableName);
        storage.setTimeoutMs(timeoutMs);
        return storage;
    }

    /**
     * 索引存储存.
     */
    @Bean
    public IndexStorage indexStorage(
        @Value("${storage.index.search.name:oqsindex}") String searchIndexName,
        @Value("${storage.timeoutMs.query:3000}") long timeoutMs) {
        SphinxQLManticoreIndexStorage storage = new SphinxQLManticoreIndexStorage();
        storage.setTimeoutMs(timeoutMs);
        storage.setSearchIndexName(searchIndexName);
        return storage;
    }

    /**
     * 联合查询策略储存实现.
     */
    @Bean
    public CombinedSelectStorage combinedSelectStorage(
        MasterStorage masterStorage, IndexStorage indexStorage,
        TransactionManager transactionManager, CommitIdStatusService commitIdStatusService) {
        CombinedSelectStorage storage = new CombinedSelectStorage(masterStorage, indexStorage);
        storage.setTransactionManager(transactionManager);
        storage.setCommitIdStatusService(commitIdStatusService);
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
        storageStrategyFactory.register(FieldType.STRINGS, new SphinxQLStringsStorageStrategy());
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

    @Bean
    public UniqueKeyGenerator uniqueKeyGenerator() {
        return new SimpleFieldKeyGenerator();
    }
}
