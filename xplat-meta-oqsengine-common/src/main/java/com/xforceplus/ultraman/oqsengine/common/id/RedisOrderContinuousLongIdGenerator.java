package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.micrometer.core.instrument.Metrics;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Continuous partial ID generator based on redis.
 *
 * @author luye
 * @version 0.1 2020/11/2 22:40
 * @since 1.8
 */
public class RedisOrderContinuousLongIdGenerator implements LongIdGenerator {

    private static final String DEFAULT_KEY = "com.xforceplus.ultraman.oqsengine.common.id";


    private StatefulRedisConnection<String, String> connection;

    private String key;
    private Supplier<Long> supplier;

    private AtomicLong commitIdNumber = Metrics.gauge(MetricsDefine.NOW_COMMITID, new AtomicLong(0));

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient) {
        this(redisClient, DEFAULT_KEY, () -> 0L);
    }

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient, Supplier<Long> supplier) {
        this(redisClient, DEFAULT_KEY, supplier);
    }

    /**
     * 构造实例.
     *
     * @param redisClient redis 客户端实例.
     * @param key         操作的key.
     * @param supplier    初始化动作.
     */
    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient, String key, Supplier<Long> supplier) {

        this.key = key;
        this.connection = redisClient.connect();
        this.supplier = supplier;
    }

    @Override
    public Long next() {
        RedisStringCommands<String, String> sync = connection.sync();
        Long newId = sync.incr(key);

        try {
            return newId;
        } finally {
            Long finalNewId = newId;
            CompletableFuture.runAsync(() -> commitIdNumber.set(finalNewId));
        }
    }

    @PostConstruct
    public void init() {
        //do init will block startup
        if (key == null || key.isEmpty()) {
            key = DEFAULT_KEY;
        }
        initializeId();
    }

    @PreDestroy
    public void destroy() {
        connection.close();
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isPartialOrder() {
        return true;
    }

    private synchronized void initializeId() {
        RedisStringCommands<String, String> sync = connection.sync();
        sync.setnx(key, supplier.get().toString());
    }
}
