package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.micrometer.core.instrument.Metrics;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicLong;

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

    private AtomicLong commitIdNumber = Metrics.gauge(MetricsDefine.NOW_COMMITID, new AtomicLong(0));

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient) {
        this(redisClient, DEFAULT_KEY);
    }

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient, String key) {

        this.key = key;
        this.connection = redisClient.connect();
    }

    @Override
    public Long next() {
        RedisStringCommands<String, String> sync = connection.sync();
        Long newId = sync.incr(key);

        try {
            return newId;
        } finally {
            commitIdNumber.set(newId);
        }
    }

    @PostConstruct
    public void init() {
        //do init will block startup
        RedisStringCommands<String, String> sync = connection.sync();
        if (key == null || key.isEmpty()) {
            key = DEFAULT_KEY;
        }
        sync.setnx(key, "0");
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
}
