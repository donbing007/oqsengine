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

    @Bean
    public MasterStorage masterStorage(
        @Value("${storage.timeoutMs.query:3000}") long masterQueryTimeout) {
        SQLMasterStorage storage = new SQLMasterStorage();
        storage.setQueryTimeout(masterQueryTimeout);
        return storage;
    }

    @Bean
    public IndexStorage indexStorage(@Value("${storage.index.name:oqsindex}") String indexTableName) {

        SphinxQLIndexStorage storage = new SphinxQLIndexStorage();
        storage.setIndexTableName(indexTableName);
        return storage;
    }

//    @Bean
//    public StorageCommandExecutor storageCommandInvoker(
//        @Value("${storage.index.name:oqsindex}") String indexTableName,
//        @Qualifier("tableNameSelector") Selector<String> tableNameSelector
//    ) {
//        DefaultStorageCommandExecutor storageCommandInvoker = new DefaultStorageCommandExecutor();
//        storageCommandInvoker.register(TransactionResourceType.INDEX, OpType.BUILD, new BuildStorageCommand(indexTableName));
//        storageCommandInvoker.register(TransactionResourceType.INDEX, OpType.REPLACE, new ReplaceStorageCommand(indexTableName));
//        storageCommandInvoker.register(TransactionResourceType.INDEX, OpType.DELETE, new DeleteStorageCommand(indexTableName));
//
//        storageCommandInvoker.register(TransactionResourceType.MASTER, OpType.BUILD, new com.xforceplus.ultraman.oqsengine.storage.master.command.BuildStorageCommand(tableNameSelector));
//        storageCommandInvoker.register(TransactionResourceType.MASTER, OpType.REPLACE, new com.xforceplus.ultraman.oqsengine.storage.master.command.ReplaceStorageCommand(tableNameSelector));
//        storageCommandInvoker.register(TransactionResourceType.MASTER, OpType.DELETE, new com.xforceplus.ultraman.oqsengine.storage.master.command.DeleteStorageCommand(tableNameSelector));
//
//        return storageCommandInvoker;
//    }
}
