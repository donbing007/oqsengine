package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceRedisConfiguration;
import com.xforceplus.ultraman.oqsengine.boot.config.redis.RedissonRedisConfiguration;
import com.xforceplus.ultraman.oqsengine.common.mode.CompatibilityMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.lettuce.core.resource.ClientResources;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(CommonConfiguration.class);

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
    @Bean(
        value = {"redisClient", "redisClientState", "redisClientChangeLog", "redisClientCacheEvent"},
        destroyMethod = "shutdown"
    )
    public RedisClient lettuceClient(LettuceRedisConfiguration configuration) {
        if (configuration.isCluster()) {
            throw new UnsupportedOperationException("Cluster mode is not supported.");

        } else {
            MicrometerOptions options = MicrometerOptions.builder()
                .histogram(false)
                .targetPercentiles(new double[] {0.5, 0.9, 0.99})
                .enable()
                .build();
            ClientResources resources = ClientResources.builder()
                .commandLatencyRecorder(new MicrometerCommandLatencyRecorder(Metrics.globalRegistry, options))
                .build();
            RedisClient redisClient = RedisClient.create(resources, configuration.getUri());
            redisClient.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(configuration.getDisconnectedBehavior())
                .requestQueueSize(configuration.getRequestQueueSize())
                .pingBeforeActivateConnection(configuration.isPingBeforeActivateConnection())
                .suspendReconnectOnProtocolFailure(configuration.isSuspendReconnectOnProtocolFailure())
                .socketOptions(SocketOptions.builder()
                    .keepAlive(true)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build()
                ).build());
            return redisClient;
        }
    }

    /**
     * 自动编号使用的redisson客户端.
     *
     * @param configuration redis配置.
     * @return 实例.
     */
    @Bean(value = {"redissonClient", "redissonClientAutoId"}, destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonRedisConfiguration configuration) {
        Config config = new Config()
            .setThreads(configuration.getThreads())
            .setNettyThreads(configuration.getNettyThreads())
            .setKeepPubSubOrder(configuration.isKeepPubSubOrder())
            .setLockWatchdogTimeout(configuration.getLockWatchdogTimeout());

        if (configuration.getSingel().isEnabled()) {
            config.useSingleServer()
                .setDatabase(configuration.getDatabase())
                .setRetryAttempts(configuration.getRetryAttempts())
                .setRetryInterval(configuration.getRetryInterval())
                .setDnsMonitoringInterval(configuration.getDnsMonitoringInterval())
                .setIdleConnectionTimeout(configuration.getIdleConnectionTimeout())
                .setConnectTimeout(configuration.getConnectTimeout())
                .setTimeout(configuration.getTimeout())
                .setPassword(configuration.getPassword())
                .setClientName(configuration.getClientName())
                .setAddress(configuration.getSingel().getAddress())
                .setSubscriptionConnectionPoolSize(configuration.getSubscriptionConnectionPoolSize())
                .setConnectionMinimumIdleSize(configuration.getSingel().getConnectionMinimumIdleSize())
                .setConnectionPoolSize(configuration.getSingel().getConnectionPoolSize());

        } else if (configuration.getSentine().isEnabled()) {
            config.useSentinelServers()
                .setDatabase(configuration.getDatabase())
                .setRetryAttempts(configuration.getRetryAttempts())
                .setRetryInterval(configuration.getRetryInterval())
                .setDnsMonitoringInterval(configuration.getDnsMonitoringInterval())
                .setIdleConnectionTimeout(configuration.getIdleConnectionTimeout())
                .setConnectTimeout(configuration.getConnectTimeout())
                .setTimeout(configuration.getTimeout())
                .setPassword(configuration.getPassword())
                .setClientName(configuration.getClientName())
                .setSubscriptionConnectionPoolSize(configuration.getSubscriptionConnectionPoolSize())
                .setMasterName(configuration.getSentine().getMasterName())
                .setReadMode(configuration.getReadMode())
                .addSentinelAddress(configuration.getSentine().getSentinelAddresses())
                .setSlaveConnectionMinimumIdleSize(configuration.getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(configuration.getSlaveConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(configuration.getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(configuration.getMasterConnectionPoolSize());

        } else if (configuration.getMasterSlave().isEnabled()) {
            config.useMasterSlaveServers()
                .setDatabase(configuration.getDatabase())
                .setRetryAttempts(configuration.getRetryAttempts())
                .setRetryInterval(configuration.getRetryInterval())
                .setDnsMonitoringInterval(configuration.getDnsMonitoringInterval())
                .setIdleConnectionTimeout(configuration.getIdleConnectionTimeout())
                .setConnectTimeout(configuration.getConnectTimeout())
                .setTimeout(configuration.getTimeout())
                .setPassword(configuration.getPassword())
                .setClientName(configuration.getClientName())
                .setSubscriptionConnectionPoolSize(configuration.getSubscriptionConnectionPoolSize())
                .setReadMode(configuration.getReadMode())
                .setSlaveConnectionMinimumIdleSize(configuration.getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(configuration.getSlaveConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(configuration.getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(configuration.getMasterConnectionPoolSize())
                .setMasterAddress(configuration.getMasterSlave().getMasterAddress())
                .addSlaveAddress(configuration.getMasterSlave().getSlaveAddresses());

        } else if (configuration.getCluster().isEnabled()) {
            config.useClusterServers()
                .setRetryAttempts(configuration.getRetryAttempts())
                .setRetryInterval(configuration.getRetryInterval())
                .setDnsMonitoringInterval(configuration.getDnsMonitoringInterval())
                .setIdleConnectionTimeout(configuration.getIdleConnectionTimeout())
                .setConnectTimeout(configuration.getConnectTimeout())
                .setTimeout(configuration.getTimeout())
                .setPassword(configuration.getPassword())
                .setClientName(configuration.getClientName())
                .setSubscriptionConnectionPoolSize(configuration.getSubscriptionConnectionPoolSize())
                .setReadMode(configuration.getReadMode())
                .setSlaveConnectionMinimumIdleSize(configuration.getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(configuration.getSlaveConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(configuration.getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(configuration.getMasterConnectionPoolSize())
                .setScanInterval(configuration.getCluster().getScanInterval())
                .addNodeAddress(configuration.getCluster().getNodeAddresses());

        } else {
            throw new IllegalStateException("Invalid Redisson connection mode configuration.");
        }

        return Redisson.create(config);
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

    /**
     * redis的lua脚本 watch dog. 保证脚本可以正确处理.
     *
     * @param redisClient 操作使用redis.
     * @return 实例.
     */
    @Bean(value = "redisLuaScriptWatchDog")
    public RedisLuaScriptWatchDog redisLuaScriptWatchDog(RedisClient redisClient) {
        // 检查时间在1分钟到10分钟之间随机,防止多结点时集中检查.
        long minCheckTimeIntervalMs = 1000 * 60;
        long maxCheckTimeIntervalMs = 1000 * 60 * 10;
        long checkTimeIntervalMs =
            minCheckTimeIntervalMs + (long) (Math.random() * (maxCheckTimeIntervalMs - minCheckTimeIntervalMs + 1));

        return new RedisLuaScriptWatchDog(redisClient, checkTimeIntervalMs);
    }

    /**
     * 判断当前是否处于兼容模式.
     */
    @Bean
    public CompatibilityMode compatibilityMode(@Value("${compatibilityMode:false}") boolean compatibility) {
        CompatibilityMode compatibilityMode = new CompatibilityMode(compatibility);
        if (compatibilityMode.isCompatibility()) {
            logger.info("Run in compatibility mode.");
        }
        return compatibilityMode;
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
