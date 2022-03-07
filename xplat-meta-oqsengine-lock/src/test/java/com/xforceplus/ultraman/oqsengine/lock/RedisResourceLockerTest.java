package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 基于redisson的资源锁封装测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/10 19:08
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class RedisResourceLockerTest extends AbstractResourceLockerTest {

    private RedisClient redisClient;
    private RedisResourceLocker locker;

    /**
     * 初始化.
     */
    @BeforeEach
    @Override
    public void before() throws Exception {

        super.before();

        redisClient = CommonInitialization.getInstance().getRedisClient();

        locker = new RedisResourceLocker(redisClient);
        locker.init();
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        super.after();
        locker.destroy();

        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Override
    public ResourceLocker getLocker() {
        return locker;
    }

    /**
     * 测试是否正确续期.
     */
    @Test
    public void testTenewalIntervalMs() throws Exception {
        String resource = "test.resource";
        locker.lock(resource);
        long ttl = locker.getTtlMs();

        /*
        等待比TTL多50%的时间.
         */
        TimeUnit.MILLISECONDS.sleep(ttl + (long) (ttl * 0.5F));

        Assertions.assertTrue(locker.isLocking(resource));

        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                return locker.tryLock(resource);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        Assertions.assertFalse(future.get());
    }
}