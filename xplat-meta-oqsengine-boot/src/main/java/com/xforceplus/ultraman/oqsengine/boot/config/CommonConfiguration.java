package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceConfiguration;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * reuse the read thread
     * @param worker
     * @param queue
     * @return
     */
    @Bean("callChangelogThreadPool")
    public ExecutorService callChangelogThreadPool(
            @Value("${threadPool.call.read.worker:0}") int worker, @Value("${threadPool.call.read.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call-changelog", false);
    }


    @Bean("callReadThreadPool")
    public ExecutorService callReadThreadPool(
        @Value("${threadPool.call.read.worker:0}") int worker, @Value("${threadPool.call.read.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call-read", false);
    }

    @Bean("callWriteThreadPool")
    public ExecutorService callWriteThreadPool(
            @Value("${threadPool.call.write.worker:0}") int worker, @Value("${threadPool.call.write.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call-write", false);
    }

    @Bean("callRebuildThreadPool")
    public ExecutorService callRebuildThreadPool(
            @Value("${threadPool.call.rebuild.worker:0}") int worker, @Value("${threadPool.call.rebuild.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "oqsengine-call-rebuild", false);
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

    @Bean("entityValueBuilder")
    public IEntityValueBuilder entityValueBuilder() {
        return new SQLJsonIEntityValueBuilder();
    }

    private ExecutorService buildThreadPool(int worker, int queue, String namePrefix, boolean daemon) {
        return new ThreadPoolExecutor(worker, worker,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queue),
            ExecutorHelper.buildNameThreadFactory(namePrefix, daemon),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

}
