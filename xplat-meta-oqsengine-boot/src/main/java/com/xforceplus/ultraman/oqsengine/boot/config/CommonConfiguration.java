package com.xforceplus.ultraman.oqsengine.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.boot.cdc.CDCMetricsCallbackToEvent;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.kubernetesStatefulsetNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.status.StatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.status.id.RedisIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.table.TableCleaner;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @ConditionalOnProperty(name = "instance.type", havingValue = "statefulset", matchIfMissing = false)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator kubernetesStatefulsetNodeIdGenerator() {
        return new kubernetesStatefulsetNodeIdGenerator();
    }

    @ConditionalOnProperty(name = "instance.type", havingValue = "static", matchIfMissing = true)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator staticNodeIdGenerator(@Value("${instance.id:0}") int instanceId) {
        return new StaticNodeIdGenerator(instanceId);
    }

    @Bean
    public LongIdGenerator longIdGenerator(@Qualifier("nodeIdGenerator") NodeIdGenerator nodeIdGenerator) {
        return new SnowflakeLongIdGenerator(nodeIdGenerator);
    }

    @ConditionalOnExpression("${storage.master.shard.table.enabled} == true")
    @Bean("tableNameSelector")
    public Selector<String> shardTableNameSelector(
        @Value("${storage.master.name:oqsbigentity}") String masterTableName,
        @Value("${storage.master.shard.table.size:1}") int masterSize) {
        return new SuffixNumberHashSelector(masterTableName, masterSize);
    }

    @ConditionalOnExpression("${storage.master.shard.table.enabled} == false")
    @Bean("tableNameSelector")
    public Selector<String> noShardTableNameSelector(
        @Value("${storage.master.name:oqsbigentity}") String masterTableName) {
        return new NoSelector(masterTableName);
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
            @Value("${threadPool.cdc.worker:0}") int worker, @Value("${cdc.connect.batchSize:2048}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
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
    public RedisClient redisClient(@Value("${redis.uri:redis://localhost:6379}") String uri){

        RedisClient redisClient = RedisClient
                .create(uri);
        return redisClient;
    }

    @Bean("redisIdGenerator")
    public RedisIdGenerator redisIdGenerator(RedisClient redisClient, @Value("${redis.gen-key:gen}") String key){
        return new RedisIdGenerator(redisClient, key);
    }

    @Bean("redisTableCleaner")
    public TableCleaner cleaner(RedisClient redisClient, @Value("${redis.cleaner.period:10}") Long period
            , @Value("${redis.cleaner.delay:10}") Long delay
            , @Value("${redis.cleaner.window:10}") Long window){
        return new TableCleaner(redisClient, period, delay, window);
    }

    @Bean("timeTable")
    public TimeTable timeTable(RedisClient redisClient, @Value("${redis.table:cdc}") String tableName){
        return new TimeTable(redisClient, tableName);
    }

    @Bean("statusService")
    public StatusService statusService(RedisIdGenerator redisIdGenerator, TimeTable timeTable, RedisClient redisClient){
        return new StatusServiceImpl(redisIdGenerator, timeTable, redisClient);
    }

    @Bean("cdcCallback")
    public CDCMetricsCallback cdcMetricsCallback(ApplicationEventPublisher publisher, StatusService statusService
            , @Value("${redis.cdc.key:cdcmetric}") String key, ObjectMapper mapper){
        return new CDCMetricsCallbackToEvent(publisher, statusService, key, mapper);
    }

    @Bean("entityValueBuilder")
    public IEntityValueBuilder entityValueBuilder() {
        return new SQLJsonIEntityValueBuilder();
    }

}
