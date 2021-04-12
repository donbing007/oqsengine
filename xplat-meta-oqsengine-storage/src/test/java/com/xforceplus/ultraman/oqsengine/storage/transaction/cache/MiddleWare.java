package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;


/**
 * desc :
 * name : InitBase
 *
 * @author : xujia
 * date : 2021/4/7
 * @since : 1.8
 */
public class MiddleWare {
    public static RedisClient redisClient;
    public static RedisCommands<String, String> syncCommands;
    public static void initRedis() {
        /**
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        syncCommands = redisClient.connect().sync();
        syncCommands.clientSetname("oqs.event.test");
    }

    public static void closeRedis() {
        syncCommands.flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    public void clearRedis() {
        redisClient.connect().sync().flushall();
    }
}
