package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.ConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CustomTransactionConfiguration {

    @Resource
    private LongIdGenerator longIdGenerator;

    @Resource
    private TransactionManager tm;

    @Resource
    private StatusService statusService;


    @Bean
    public TransactionManager transactionManager(
        @Value("${transaction.timeoutMs:3000}") int transactionTimeoutMs) {
        return new DefaultTransactionManager(transactionTimeoutMs, longIdGenerator, statusService);
    }

    @Bean
    public SphinxQLTransactionResourceFactory sphinxQLTransactionResourceFactory() {
        return new SphinxQLTransactionResourceFactory();
    }

    @Bean
    public TransactionExecutor storageSphinxQLTransactionExecutor(SphinxQLTransactionResourceFactory factory) {
        return new AutoJoinTransactionExecutor(tm, factory);
    }

    @Bean
    public ConnectionTransactionResourceFactory connectionTransactionResourceFactory(
        @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new ConnectionTransactionResourceFactory(tableName);
    }

    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor(ConnectionTransactionResourceFactory factory) {
        return new AutoJoinTransactionExecutor(tm, factory);
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor() {
        return new AutoCreateTransactionExecutor(tm);
    }

}
