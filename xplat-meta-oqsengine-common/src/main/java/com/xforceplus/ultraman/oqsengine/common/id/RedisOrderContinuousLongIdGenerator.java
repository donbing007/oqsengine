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

    private static final String DEFAULT_NAMESPACE = "com.xforceplus.ultraman.oqsengine.default";


    private StatefulRedisConnection<String, String> connection;

    private String ns;

    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient) {
        this(redisClient, DEFAULT_NAMESPACE);
    }

    /**
     * 构造实例.
     *
     * @param redisClient redis 客户端实例.
     * @param ns         操作的key.
     */
    public RedisOrderContinuousLongIdGenerator(RedisClient redisClient, String ns) {

        this.ns = ns;
        this.connection = redisClient.connect();
    }

    @Override
    public Long next() {
        return next(ns);
    }

    @Override
    public Long next(String nameSpace) {
        RedisStringCommands<String, String> sync = connection.sync();
        Long newId = sync.incr(nameSpace);
        return newId;
    }

    @Override
    public boolean supportNameSpace() {
        return true;
    }

    @PostConstruct
    public void init() {
        //do init will block startup
        if (ns == null || ns.isEmpty()) {
            ns = DEFAULT_NAMESPACE;
        }
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

    @Override
    public void reset() {
        RedisStringCommands<String, String> sync = connection.sync();
        sync.set(ns, "0");
    }
}
