package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;


/**
 * desc :.
 * name : InitBase
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class MiddleWare {
    public static RedisClient redisClient;
    public static RedisCommands<String, String> syncCommands;

    /**
     * 初始化.
     */
    public static void initRedis() throws IllegalAccessException {
        /*
         * init RedisClient
         */
        redisClient = CommonInitialization.getInstance().getRedisClient();

        syncCommands = redisClient.connect().sync();
        syncCommands.clientSetname("oqs.event.test");
    }

    /**
     * 关闭.
     */
    public static void destroyRedis() throws Exception {
        InitializationHelper.clearAll();
    }
}
