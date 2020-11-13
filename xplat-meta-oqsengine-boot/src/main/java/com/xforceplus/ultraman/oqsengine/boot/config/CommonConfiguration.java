package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.cdc.CDCMetricsCallbackToEvent;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.status.StatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.status.table.TimeTable;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:10
 * @since 1.8
 */
@Configuration
public class CommonConfiguration {

    @ConditionalOnExpression("${storage.index.write.shard.enabled} == true")
    @Bean("indexWriteIndexNameSelector")
    public Selector<String> shardingIndexWriteIndexNameSelector(
        @Value("{storage.index.write.name:oqsindex}") String baseIndexName,
        @Value("{storage.index.write.shard.size:1}") int shardSize) {
        return new SuffixNumberHashSelector(baseIndexName, shardSize);
    }

    @ConditionalOnExpression("${storage.index.write.shard.enabled} == false")
    @Bean("indexWriteIndexNameSelector")
    public Selector<String> noShardingIndexWriteIndexNameSelector(
        @Value("{storage.index.write.name:oqsindex}") String baseIndexName) {
        return new NoSelector(baseIndexName);
    }

    @Bean
    public SphinxQLConditionsBuilderFactory indexConditionsBuilderFactory() {
        return new SphinxQLConditionsBuilderFactory();
    }

    @Bean
    public SQLJsonConditionsBuilderFactory masterConditionsBuilderFactory() {
        return new SQLJsonConditionsBuilderFactory();
    }

    @Bean
    public StorageStrategyFactory masterStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());
        return storageStrategyFactory;
    }

    @Bean
    public StorageStrategyFactory indexStorageStrategy() {
        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        return storageStrategyFactory;
    }

    @Bean("callThreadPool")
    public ExecutorService callThreadPool(
        @Value("${threadPool.call.worker:0}") int worker, @Value("${threadPool.call.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call", false);
    }

    @Bean("cdcConsumerPool")
    public ExecutorService cdcConsumerPool(
        @Value("${threadPool.cdc.worker:1}") int worker, @Value("${cdc.connect.batchSize:2048}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 1) {
            useQueue = 1;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-cdc", false);
    }

    private ExecutorService buildThreadPool(int worker, int queue, String namePrefix, boolean daemon) {
        return new ThreadPoolExecutor(worker, worker,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queue),
            ExecutorHelper.buildNameThreadFactory(namePrefix, daemon),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean("redisClient")
    public RedisClient redisClient(@Value("${redis.uri:redis://localhost:6379}") String uri) {

        RedisClient redisClient = RedisClient
            .create(uri);
        return redisClient;
    }

//    @Bean("redisTableCleaner")
//    public TableCleaner cleaner(RedisClient redisClient, @Value("${redis.cleaner.period:10}") Long period
//        , @Value("${redis.cleaner.delay:10}") Long delay
//        , @Value("${redis.cleaner.window:10}") Long window) {
//        return new TableCleaner(redisClient, period, delay, window);
//    }

    @Bean("timeTable")
    public TimeTable timeTable(RedisClient redisClient, @Value("${redis.table:cdc}") String tableName) {
        return new TimeTable(redisClient, tableName);
    }

    @Bean("statusService")
    public StatusService statusService(LongIdGenerator redisIdGenerator, TimeTable timeTable) {
        return new StatusServiceImpl(redisIdGenerator, timeTable);
    }

    @Bean("cdcCallback")
    public CDCMetricsCallback cdcMetricsCallback(ApplicationEventPublisher publisher) {
        return new CDCMetricsCallbackToEvent(publisher);
    }

    @Bean("entityValueBuilder")
    public IEntityValueBuilder entityValueBuilder() {
        return new SQLJsonIEntityValueBuilder();
    }

}
