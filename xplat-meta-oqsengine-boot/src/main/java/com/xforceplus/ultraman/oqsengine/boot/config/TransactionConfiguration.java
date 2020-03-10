package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:08
 * @since 1.8
 */
@Configuration
public class TransactionConfiguration {

    @Resource
    private LongIdGenerator longIdGenerator;

    @Resource
    private TransactionManager tm;

    @Value("${transaction.timeoutMs:3000}")
    private int transactionTimeoutMs;

    @Bean
    public TransactionManager transactionManager() {
        return new DefaultTransactionManager(transactionTimeoutMs, longIdGenerator);
    }


    @Bean
    public TransactionExecutor storageSphinxQLTransactionExecutor() {
        return new AutoShardTransactionExecutor(tm, SphinxQLTransactionResource.class);
    }

    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor() {
        return new AutoShardTransactionExecutor(tm, ConnectionTransactionResource.class);
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor() {
        return new AutoCreateTransactionExecutor(tm);
    }
}
