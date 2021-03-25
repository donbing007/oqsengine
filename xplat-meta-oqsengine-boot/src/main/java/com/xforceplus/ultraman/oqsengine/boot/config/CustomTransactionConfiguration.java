package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
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

import javax.sql.DataSource;

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
        CommitIdStatusService commitIdStatusService,
        EventBus eventBus) {
        return DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withSurvivalTimeMs(transactionTimeoutMs)
            .withTxIdGenerator(snowflakeIdGenerator)
            .withCommitIdGenerator(redisIdGenerator)
            .withCommitIdStatusService(commitIdStatusService)
            .withWaitCommitSync(true)
            .withEventBus()
            .build();
    }

    @Bean
    public SphinxQLTransactionResourceFactory sphinxQLTransactionResourceFactory() {
        return new SphinxQLTransactionResourceFactory();
    }

    @Bean
    public TransactionExecutor sphinxQLSearchTransactionExecutor(
        SphinxQLTransactionResourceFactory factory,
        TransactionManager tm,
        DataSource indexSearchDataSource,
        @Value("${storage.index.search.name:oqsindex}") String searchTableName) {
        return new AutoJoinTransactionExecutor(
            tm,
            factory,
            new NoSelector(indexSearchDataSource),
            new NoSelector(searchTableName)
        );
    }

    @Bean
    public TransactionExecutor sphinxQLWriteTransactionExecutor(
        SphinxQLTransactionResourceFactory factory,
        TransactionManager tm,
        Selector<DataSource> indexWriteDataSourceSelector,
        Selector<String> indexWriteIndexNameSelector) {
        return new AutoJoinTransactionExecutor(tm, factory, indexWriteDataSourceSelector, indexWriteIndexNameSelector);
    }

    @Bean
    public SqlConnectionTransactionResourceFactory connectionTransactionResourceFactory(
        @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new SqlConnectionTransactionResourceFactory(tableName);
    }

    /**
     * master 的shard将由shard-jdbc来支持,所以主库不需处理shard.
     */
    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor(
        SqlConnectionTransactionResourceFactory factory,
        TransactionManager tm,
        DataSource masterDataSource,
        @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new AutoJoinTransactionExecutor(tm, factory, new NoSelector(masterDataSource), new NoSelector(tableName));
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor(TransactionManager tm) {
        return new AutoCreateTransactionExecutor(tm);
    }

}
