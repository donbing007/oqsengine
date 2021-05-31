package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceConfiguration;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通用配置.
 *
 * @author dongbin
 * @version 0.1 2020/2/24 17:10
 * @since 1.8
 */
@Configuration
public class CommonConfiguration {

    /**
     * IO响应线程池.
     * 此线程池主要用以服务响应任务.
     */
    @Bean("ioThreadPool")
    public ExecutorService ioThreadPool(
        @Value("${threadPool.io.worker:0}") int worker, @Value("${threadPool.io.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-io", false);
    }

    /**
     * 任务线程池,注意任务线程池中的任务不允许往 ioThreadPool 中创建任务.
     * 此线程主要用以处理需要异步执行的任务.
     */
    @Bean("taskThreadPool")
    public ExecutorService taskThreadPool(
        @Value("${threadPool.task.worker:0}") int worker, @Value("${threadPool.task.queue:500}") int queue) {

        return buildThreadPool(worker, queue, "oqsengine-task", false);
    }

    /**
     * redis client构造.
     *
     * @param configuration lettuce 配置.
     * @return redisClient 实例.
     */
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

    /**
     * change log 使用redis client构造.
     *
     * @param configuration lettuce 配置.
     * @return redisClient 实例.
     */
    @Bean(value = "redisClientChangeLog")
    public RedisClient redisClientChangeLog(LettuceConfiguration configuration) {
        RedisClient redisClient = RedisClient.create(configuration.uriWithChangeLogDb());

        redisClient.setOptions(ClientOptions.builder()
            .autoReconnect(true)
            .requestQueueSize(configuration.getMaxReqQueue())
            .build()
        );
        return redisClient;
    }

    /**
     * 事件使用的redis客户端实例.
     *
     * @param configuration lettuce 配置.
     * @return redisClient 实例.
     */
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

    /**
     * 分词工厂.
     *
     * @param lexUrl 外部分词词典url.
     * @return 实例.
     * @throws IOException 构造发生异常.
     */
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
