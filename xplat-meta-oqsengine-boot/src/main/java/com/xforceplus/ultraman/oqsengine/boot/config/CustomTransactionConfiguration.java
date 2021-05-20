package com.xforceplus.ultraman.oqsengine.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.CacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.RedisEventHandler;
import io.lettuce.core.RedisClient;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 自定义事务配置.
 *
 * @author dongbin
 * @version 0.1 2020/2/24 17:08
 * @since 1.8
 */
@Configuration
public class CustomTransactionConfiguration {

    /**
     * 事务管理器.
     */
    @Bean
    public TransactionManager transactionManager(
        LongIdGenerator snowflakeIdGenerator,
        LongIdGenerator redisIdGenerator,
        @Value("${transaction.timeoutMs:3000}") int transactionTimeoutMs,
        CommitIdStatusService commitIdStatusService,
        EventBus eventBus,
        CacheEventHandler cacheEventHandler) {
        return DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withSurvivalTimeMs(transactionTimeoutMs)
            .withTxIdGenerator(snowflakeIdGenerator)
            .withCommitIdGenerator(redisIdGenerator)
            .withCommitIdStatusService(commitIdStatusService)
            .withWaitCommitSync(true)
            .withEventBus(eventBus)
            .withCacheEventHandler(cacheEventHandler)
            .build();
    }

    @Bean
    public SphinxQLTransactionResourceFactory sphinxQLTransactionResourceFactory() {
        return new SphinxQLTransactionResourceFactory();
    }

    /**
     * 索引搜索执行器.
     */
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
    @Primary
    public SqlConnectionTransactionResourceFactory connectionTransactionResourceFactory(
            @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new SqlConnectionTransactionResourceFactory(tableName);
    }

    //@Bean(name = "segmentTransactionResourceFactory")
    //public SegmentTransactionResourceFactory segmentTransactionResourceFactory(
    //        @Value("${storage.generator.name:segment}") String tableName) {
    //    return new SegmentTransactionResourceFactory(tableName);
    //}



    /**
     * master 的shard将由shard-jdbc来支持,所以主库不需处理shard.
     */
    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor(
        SqlConnectionTransactionResourceFactory factory,
        TransactionManager tm,
        DataSource masterDataSource,
        @Value("${storage.master.name:oqsbigentity}") String tableName) {
        return new AutoJoinTransactionExecutor(tm, factory, new NoSelector(masterDataSource),
            new NoSelector(tableName));
    }

    ///**
    // * Segment.
    // */
    //@Bean
    //public TransactionExecutor segmentJDBCTransactionExecutor(
    //        @Qualifier(value = "segmentTransactionResourceFactory") SegmentTransactionResourceFactory factory,
    //        TransactionManager tm,
    //        DataSource masterDataSource,
    //        @Value("${storage.generator.name:segment}") String tableName) {
    //    return new AutoJoinTransactionExecutor(tm, factory, new NoSelector(masterDataSource), new NoSelector(tableName));
    //}

    /**
     * serviceTransactionExecutor.
     */
    @Bean
    public TransactionExecutor serviceTransactionExecutor(TransactionManager tm) {
        return new AutoCreateTransactionExecutor(tm);
    }

    /**
     * cacheEventHandler.
     */
    @Bean
    public CacheEventHandler cacheEventHandler(RedisClient redisClientCacheEvent,
                                               ObjectMapper objectMapper,
                                               @Value("${cache.event.expire:0}") long expire) {
        return new RedisEventHandler(redisClientCacheEvent, objectMapper, expire);
    }

}
