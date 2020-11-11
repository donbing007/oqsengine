package com.xforceplus.ultraman.oqsengine.status.id;

import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.apache.commons.lang3.StringUtils;

/**
 * redis id generator
 */
public class RedisIdGenerator implements IdGenerator<Long> {

    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> connection;

    private String keyName;

    public RedisIdGenerator(RedisClient redisClient, String key) {

        this.redisClient = redisClient;
        this.keyName = key;
        this.connection = redisClient.connect();
        //do init will block startup
        RedisStringCommands<String, String> sync = connection.sync();
        if (StringUtils.isEmpty(key)) {
            key = "gen";
        }
        sync.setnx(key, "0");
    }

    @Override
    public Long next() {
        RedisStringCommands<String, String> sync = connection.sync();
        return sync.incr(keyName);
    }
}
