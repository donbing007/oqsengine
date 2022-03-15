package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        int size = 10;
        ExecutorService worker = Executors.newFixedThreadPool(size);
        CountDownLatch latch = new CountDownLatch(size);
        Queue<Boolean> queue = new ConcurrentLinkedQueue();
        for (int i = 0; i < size; i++) {
            int finalI = i;
            worker.submit(() -> {
                String resource = "test.resource" + finalI;

                try {
                    locker.lock(resource);


                    long ttl = locker.getTtlMs();

                    TimeUnit.MILLISECONDS.sleep(ttl + (long) (ttl * 0.2F));

                    queue.add(locker.isLocking(resource));

                } catch (InterruptedException e) {

                    logger.error(e.getMessage(), e);

                } finally {

                    latch.countDown();
                }
            });
        }

        latch.await();

        ExecutorHelper.shutdownAndAwaitTermination(worker, 1);

        Assertions.assertEquals(size, queue.size());
        Assertions.assertEquals(size, queue.stream().filter(b -> b).count());
    }
}