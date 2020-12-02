package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
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

    @Bean
    public TransactionManager transactionManager(
        LongIdGenerator snowflakeIdGenerator,
        LongIdGenerator redisIdGenerator,
        @Value("${transaction.timeoutMs:3000}") int transactionTimeoutMs,
        CommitIdStatusService commitIdStatusService) {
        return new DefaultTransactionManager(
            transactionTimeoutMs, snowflakeIdGenerator, redisIdGenerator, commitIdStatusService);
    }

    @Bean
    public SphinxQLTransactionResourceFactory sphinxQLTransactionResourceFactory() {
        return new SphinxQLTransactionResourceFactory();
    }

    @Bean
    public TransactionExecutor storageSphinxQLTransactionExecutor(
        SphinxQLTransactionResourceFactory factory, TransactionManager tm) {
        return new AutoJoinTransactionExecutor(tm, factory);
    }

    @Bean
    public SqlConnectionTransactionResourceFactory connectionTransactionResourceFactory(
        @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new SqlConnectionTransactionResourceFactory(tableName);
    }

    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor(
        SqlConnectionTransactionResourceFactory factory, TransactionManager tm) {
        return new AutoJoinTransactionExecutor(tm, factory);
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor(TransactionManager tm) {
        return new AutoCreateTransactionExecutor(tm);
    }

}
