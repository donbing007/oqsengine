package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * 基于redisson的资源锁封装测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/10 19:08
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class RedisResourceLockerTest extends AbstractResourceLockerTest {

    private RedissonClient redissonClient;
    private RedisResourceLocker locker;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        Config config = new Config();
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        redissonClient = Redisson.create(config);

        locker = new RedisResourceLocker(redissonClient);
    }

    @AfterEach
    public void after() throws Exception {
        redissonClient.shutdown();
    }

    @Override
    public ResourceLocker getLocker() {
        return locker;
    }
}