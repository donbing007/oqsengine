package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * CommitIdStatusServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/13/2020
 * @since <pre>Nov 13, 2020</pre>
 */
@RunWith(ContainerRunner.class)
@DependentContainers(ContainerType.REDIS)
public class CommitIdStatusServiceImplTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl impl;
    private String key = "test";
    private String statusKeyPreifx = "test.status";

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        impl = new CommitIdStatusServiceImpl(key, statusKeyPreifx);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        impl.init();

        TimeUnit.MILLISECONDS.sleep(100L);
    }

    @After
    public void after() throws Exception {
        impl.destroy();
        impl = null;

        redisClient.connect().sync().flushall();
        redisClient.shutdown(0, 1, TimeUnit.MINUTES);
        redisClient = null;
    }

    /**
     * Method: save(long commitId)
     */
    @Test
    public void testSaveNotReady() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 100).map(i -> {
            impl.save(i, false);
            return i;
        }).sum();

        long[] allIds = impl.getAll();
        long actualTotal = Arrays.stream(allIds).sum();
        Assert.assertEquals(expectedTotal, actualTotal);

        Arrays.stream(allIds).forEach(id -> {
            Assert.assertFalse(impl.isReady(id));
        });
    }

    @Test
    public void testSaveReady() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 100).map(i -> {
            impl.save(i, true);
            return i;
        }).sum();

        long[] allIds = impl.getAll();
        long actualTotal = Arrays.stream(allIds).sum();
        Assert.assertEquals(expectedTotal, actualTotal);

        Arrays.stream(allIds).forEach(id -> {
            Assert.assertTrue(impl.isReady(id));
        });
    }

    @Test
    public void testSetReady() throws Exception {
        long commitId = 100;
        impl.save(commitId, false);
        Assert.assertEquals(commitId, impl.getMin().get().longValue());

        Assert.assertFalse(impl.isReady(commitId));

        impl.ready(commitId);

        /**
         * 多次判断相等.
         */
        Assert.assertTrue(impl.isReady(commitId));
        Assert.assertTrue(impl.isReady(commitId));
    }

    @Test
    public void testStatusClean() throws Exception {
        long commitId = 100;
        impl.save(commitId, false);
        Assert.assertEquals(commitId, impl.getMin().get().longValue());
        impl.ready(commitId);
        impl.obsolete(commitId);

        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(impl.isReady(commitId));
        }
    }

    /**
     * Method: getMin()
     */
    @Test
    public void testGetMin() throws Exception {
        long expectedMin = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).min().getAsLong();

        long actualMin = impl.getMin().get();
        Assert.assertEquals(expectedMin, actualMin);
    }

    /**
     * Method: getMax()
     */
    @Test
    public void testGetMax() throws Exception {
        long expectedMax = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).max().getAsLong();

        long actualMax = impl.getMax().get();
        Assert.assertEquals(expectedMax, actualMax);
    }

    /**
     * Method: getAll()
     */
    @Test
    public void testGetAll() throws Exception {
        long[] expectedAll = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).sorted().toArray();

        long[] actualAll = impl.getAll();

        Assert.assertEquals(expectedAll.length, actualAll.length);

    }

    /**
     * Method: size()
     */
    @Test
    public void testSize() throws Exception {
        long expectedCount = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).count();
        Assert.assertEquals(expectedCount, impl.size());
    }

    /**
     * Method: obsolete(long commitId)
     */
    @Test
    public void testObsoleteCommitId() throws Exception {
        long[] expected = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).filter(i -> i != 20).filter(i -> i != 9)
            .sorted().toArray();
        impl.obsolete(20);
        impl.obsolete(9);
        Assert.assertArrayEquals(expected, impl.getAll());

        Assert.assertEquals(10L, impl.getMin().get().longValue());

        try (StatefulRedisConnection<String, String> conn = redisClient.connect()) {
            Assert.assertEquals(0, conn.sync().exists("test.status.20").longValue());
            Assert.assertEquals(0, conn.sync().exists("test.status.9").longValue());
        }
    }

    /**
     * Method: obsolete(long... commitIds)
     */
    @Test
    public void testObsoleteCommitIds() throws Exception {
        long[] expected = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).filter(i -> i != 20).filter(i -> i != 9).filter(i -> i != 100)
            .sorted().toArray();
        impl.obsolete(20, 9, 100);
        // Idempotent
        impl.obsolete(20, 9, 100);
        Assert.assertEquals(expected.length, impl.size());
        Assert.assertArrayEquals(expected, impl.getAll());

        Assert.assertEquals(10L, impl.getMin().get().longValue());
        Assert.assertEquals(99L, impl.getMax().get().longValue());

        // 因为同步指标是异步的,所以等待成功.
        TimeUnit.MILLISECONDS.sleep(300);

        Field unSyncCommitIdSizeField = impl.getClass().getDeclaredField("unSyncCommitIdSize");
        unSyncCommitIdSizeField.setAccessible(true);
        AtomicLong unSyncCommitIdSize = (AtomicLong) unSyncCommitIdSizeField.get(impl);
        Assert.assertEquals(89L, unSyncCommitIdSize.longValue());
    }

    @Test
    public void testGetUnreadiness() throws Exception {
        impl.save(1, false);
        impl.save(2, false);
        impl.save(3, true);

        Assert.assertEquals(2, Arrays.stream(impl.getUnreadiness()).filter(i -> i == 1 | i == 2).count());
    }

    @Test
    public void testIsObsolete() throws Exception {
        impl.save(1, true);
        impl.save(2, true);
        Assert.assertFalse(impl.isObsolete(1));
        Assert.assertFalse(impl.isObsolete(2));

        impl.obsolete(1, 2);
        Assert.assertTrue(impl.isObsolete(1));
        Assert.assertTrue(impl.isObsolete(2));
    }

    /**
     * 测试如果状态为unknown,isReady在检查到一个阀值时会自动设置为就绪状态.
     *
     * @throws Exception
     */
    @Test
    public void testUnknownNumber() throws Exception {
        for (int i = 0; i < 30; i++) {
            Assert.assertFalse(impl.isReady(Integer.MAX_VALUE));
        }
        Assert.assertTrue(impl.isReady(Integer.MAX_VALUE));
    }

    @Test
    public void testObsoleteAfterSave() throws Exception {
        impl.save(100, true);
        impl.save(200, true);
        impl.obsolete(100);
        impl.save(100, true);

        Assert.assertFalse(impl.isReady(100));
        Assert.assertTrue(impl.isObsolete(100));
        Assert.assertEquals(200, impl.getMin().get().longValue());
        Assert.assertEquals(1, impl.size());
        Assert.assertTrue(Arrays.equals(new long[]{200L}, impl.getAll()));
    }

    /**
     * 测试并发保存和淘汰,查询状态是否正常.
     *
     * @throws Exception
     */
    @Test
    public void testConcurrentSaveObsolete() throws Exception {
        for (int i = 1; i < 100; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(2);
            int finalI = i;
            AtomicBoolean saved = new AtomicBoolean(false);
            CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                impl.obsolete(finalI);
                finishLatch.countDown();
            });
            int finalI1 = i;
            CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                saved.set(impl.save(finalI1, true));
                finishLatch.countDown();
            });

            latch.countDown();
            finishLatch.await();
            /**
             * 有两种可能,先淘汰那不应该保存,先保存应该状态为淘汰.
             */
            if (impl.isObsolete(i)) {

                Assert.assertFalse(impl.isReady(i));

            } else if (impl.isReady(i + 1)) {

                Assert.assertFalse(impl.isObsolete(i));

            }
        }
    }
}
