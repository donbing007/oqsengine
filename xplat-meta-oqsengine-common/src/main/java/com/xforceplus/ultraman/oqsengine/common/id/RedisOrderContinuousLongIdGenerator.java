package com.xforceplus.ultraman.oqsengine.common.id;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;

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

    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> connection;

    private String key;

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient) {
        this(redisClient, DEFAULT_KEY);
    }

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient, String key) {

        this.redisClient = redisClient;
        this.key = key;
        this.connection = redisClient.connect();
    }

    @Override
    public Long next() {
        RedisStringCommands<String, String> sync = connection.sync();
        return sync.incr(key);
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
