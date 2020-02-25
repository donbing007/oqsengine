package com.xforceplus.ultraman.oqsengine.boot.config;

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

    @Value("${storage.master.query.worker:0}")
    private int masterWorkerSize;

    @Value("${storage.master.query.timeout:3000}")
    private long masterQueryTimeout;

    @Bean
    public MasterStorage masterStorage() {
        SQLMasterStorage storage = new SQLMasterStorage();
        storage.setWorkerSize(masterWorkerSize);
        storage.setQueryTimeout(masterQueryTimeout);
        return storage;
    }

    @Bean
    public IndexStorage indexStorage() {
        return new SphinxQLIndexStorage();
    }
}
