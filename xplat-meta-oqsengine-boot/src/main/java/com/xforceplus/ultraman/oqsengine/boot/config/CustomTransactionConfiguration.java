package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:08
 * @since 1.8
 */
@Configuration
public class CustomTransactionConfiguration {

    @Autowired
    private LongIdGenerator longIdGenerator;

    @Autowired
    private TransactionManager tm;


    @Bean
    public TransactionManager transactionManager(@Value("${transaction.timeoutMs:3000}") int transactionTimeoutMs) {
        return new DefaultTransactionManager(transactionTimeoutMs, longIdGenerator);
    }


    @Bean
    public TransactionExecutor storageSphinxQLTransactionExecutor() {
        return new AutoJoinTransactionExecutor(tm, SphinxQLTransactionResource.class);
    }

    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor() {
        return new AutoJoinTransactionExecutor(tm, ConnectionTransactionResource.class);
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor() {
        return new AutoCreateTransactionExecutor(tm);
    }

}
