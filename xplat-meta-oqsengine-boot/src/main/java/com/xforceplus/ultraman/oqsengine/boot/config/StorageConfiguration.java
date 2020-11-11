package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.core.service.impl.CombinedStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import org.springframework.beans.factory.annotation.Value;
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
        @Value("${storage.index.name:oqsindex}") String indexTableName,
        @Value("${storage.index.maxQueryTimeMs:0}") long maxQueryTimeMs) {

        SphinxQLIndexStorage storage = new SphinxQLIndexStorage();
        storage.setIndexTableName(indexTableName);
        storage.setMaxQueryTimeMs(maxQueryTimeMs);
        return storage;
    }

    @Bean
    public CombinedStorage combinedStorage(
                MasterStorage masterStorage,
                IndexStorage indexStorage
            ) {
        CombinedStorage combinedStorage = new CombinedStorage(masterStorage, indexStorage);
        return combinedStorage;
    }
}
