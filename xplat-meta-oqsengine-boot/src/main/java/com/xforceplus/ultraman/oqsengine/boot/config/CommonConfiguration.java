package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceConfiguration;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
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

    /**
     * reuse the read thread
     * @param worker
     * @param queue
     * @return
     */
    @Bean("callChangelogThreadPool")
    public ExecutorService callChangelogThreadPool(
            @Value("${threadPool.call.read.worker:0}") int worker, @Value("${threadPool.call.read.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-call-changelog", false);
    }


    @Bean("callReadThreadPool")
    public ExecutorService callReadThreadPool(
        @Value("${threadPool.call.read.worker:0}") int worker, @Value("${threadPool.call.read.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-call-read", false);
    }

    @Bean("callWriteThreadPool")
    public ExecutorService callWriteThreadPool(
        @Value("${threadPool.call.write.worker:0}") int worker, @Value("${threadPool.call.write.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-call-write", false);
    }

    @Bean("callRebuildThreadPool")
    public ExecutorService callRebuildThreadPool(
        @Value("${threadPool.call.rebuild.worker:0}") int worker, @Value("${threadPool.call.rebuild.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-call-rebuild", false);
    }

    @Bean("eventWorker")
    public ExecutorService eventWorker(
        @Value("${threadPool.event.worker:0}") int worker,
        @Value("${threadPool.event.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-event", false);
    }

    @Bean("waitVersionExecutor")
    public ExecutorService waitVersionExecutor(
        @Value("${threadPool.call.read.worker:0}") int worker, @Value("${threadPool.call.read.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-meta-version", false);
    }

    @Bean(value = "redisClient")
    public RedisClient redisClient(LettuceConfiguration configuration) {
        RedisClient redisClient = RedisClient.create(configuration.getUri());

        redisClient.setOptions(ClientOptions.builder()
            .autoReconnect(true)
            .requestQueueSize(configuration.getMaxReqQueue())
            .build()
        );
        return redisClient;
    }

    @Bean(value = "redisClientChangeLog")
    public RedisClient redisClientChangeLog(LettuceConfiguration configuration) {
        RedisClient redisClient = RedisClient.create(configuration.uriWithChangeLogDB());

        redisClient.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .requestQueueSize(configuration.getMaxReqQueue())
                .build()
        );
        return redisClient;
    }

    @Bean(value = "redisClientCacheEvent")
    public RedisClient redisClientCacheEvent(LettuceConfiguration configuration) {
        RedisClient redisClient = RedisClient.create(configuration.uriWithCacheEventDb());

        redisClient.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .requestQueueSize(configuration.getMaxReqQueue())
                .build()
        );
        return redisClient;
    }


    @Bean(value = "tokenizerFactory")
    public TokenizerFactory tokenizerFactory(
        @Value("${storage.tokenizer.segmentation.lexicon.url:-}") String lexUrl) throws IOException {
        if ("-".equals(lexUrl)) {
            return new DefaultTokenizerFactory();
        } else {
            return new DefaultTokenizerFactory(new URL(lexUrl));
        }
    }

    private ExecutorService buildThreadPool(int worker, int queue, String namePrefix, boolean daemon) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }
        return new ThreadPoolExecutor(useWorker, useWorker,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(useQueue),
            ExecutorHelper.buildNameThreadFactory(namePrefix, daemon),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

}
