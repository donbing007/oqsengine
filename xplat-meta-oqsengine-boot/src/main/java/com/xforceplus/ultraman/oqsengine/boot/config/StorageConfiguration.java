package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:02
 * @since 1.8
 */
//@Configuration
public class StorageConfiguration {


    @Bean
    public MasterStorage masterStorage() {
        return new SQLMasterStorage();
    }

    @Bean
    public IndexStorage indexStorage() {
        return new SphinxQLIndexStorage();
    }
}
