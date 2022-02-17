package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CommitIdStatusServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 11/13/2020
 * @since <pre>Nov 13, 2020</pre>
 */
@ExtendWith({RedisContainer.class})
public class CommitIdStatusServiceImplTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl impl;
    private String key = "test";
    private String statusKeyPreifx = "test.status";

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        redisClient = CommonInitialization.getInstance().getRedisClient();

        impl = new CommitIdStatusServiceImpl(key, statusKeyPreifx);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        impl.init();

        TimeUnit.MILLISECONDS.sleep(100L);
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        impl.destroy();
        impl = null;

        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void testIsReadyMutil() throws Exception {
        long[] readyCommitIds = LongStream.rangeClosed(1, 10).map(i -> {
            impl.save(i, true);
            return i;
        }).toArray();

        boolean[] expected;
        boolean[] status = impl.isReady(readyCommitIds);
        expected = new boolean[readyCommitIds.length];
        Arrays.fill(expected, true);
        Assertions.assertArrayEquals(expected, status);

        long[] notReadyCommitIds = LongStream.rangeClosed(12, 19).map(i -> {
            impl.save(i, false);
            return i;
        }).toArray();

        status = impl.isReady(notReadyCommitIds);
        expected = new boolean[notReadyCommitIds.length];
        Arrays.fill(expected, false);
        Assertions.assertArrayEquals(expected, status);


        long[] notExistCommitIds = LongStream.rangeClosed(22, 32).toArray();
        status = impl.isReady(notExistCommitIds);
        expected = new boolean[notExistCommitIds.length];
        Arrays.fill(expected, true);
        Assertions.assertArrayEquals(expected, status);

        long[] mixCommitIds = LongStream.rangeClosed(1000, 1100).map(i -> {
            impl.save(i, i % 2 == 0);
            return i;
        }).toArray();

        status = impl.isReady(mixCommitIds);
        expected = new boolean[status.length];
        int number = 1000;
        for (int i = 0; i <= 1100 - 1000; i++) {
            expected[i] = number++ % 2 == 0;
        }

        Assertions.assertArrayEquals(expected, status);
    }

    /**
     * Method: save(long commitId).
     */
    @Test
    public void testSaveNotReady() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 100).map(i -> {
            impl.save(i, false);
            return i;
        }).sum();

        long[] allIds = impl.getAll();
        long actualTotal = Arrays.stream(allIds).sum();
        Assertions.assertEquals(expectedTotal, actualTotal);

        Arrays.stream(allIds).forEach(id -> {
            Assertions.assertFalse(impl.isReady(id));
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
        Assertions.assertEquals(expectedTotal, actualTotal);

        Arrays.stream(allIds).forEach(id -> {
            Assertions.assertTrue(impl.isReady(id));
        });
    }

    @Test
    public void testSetReady() throws Exception {
        long commitId = 100;
        impl.save(commitId, false);
        Assertions.assertEquals(commitId, impl.getMin().get().longValue());

        Assertions.assertFalse(impl.isReady(commitId));

        impl.ready(commitId);

        /*
         * 多次判断相等.
         */
        Assertions.assertTrue(impl.isReady(commitId));
        Assertions.assertTrue(impl.isReady(commitId));
    }

    @Test
    public void testStatusClean() throws Exception {
        long commitId = 100;
        impl.save(commitId, false);
        Assertions.assertEquals(commitId, impl.getMin().get().longValue());
        impl.ready(commitId);
        impl.obsolete(commitId);

        for (int i = 0; i < 10; i++) {
            Assertions.assertFalse(impl.isReady(commitId));
        }
    }

    /**
     * Method: getMin().
     */
    @Test
    public void testGetMin() throws Exception {
        long expectedMin = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).min().getAsLong();

        long actualMin = impl.getMin().get();
        Assertions.assertEquals(expectedMin, actualMin);
    }

    /**
     * Method: getMax().
     */
    @Test
    public void testGetMax() throws Exception {
        long expectedMax = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).max().getAsLong();

        long actualMax = impl.getMax().get();
        Assertions.assertEquals(expectedMax, actualMax);
    }

    /**
     * Method: getAll().
     */
    @Test
    public void testGetAll() throws Exception {
        long[] expectedAll = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).sorted().toArray();

        long[] actualAll = impl.getAll();

        Assertions.assertEquals(expectedAll.length, actualAll.length);

    }

    /**
     * Method: size().
     */
    @Test
    public void testSize() throws Exception {
        long expectedCount = LongStream
            .rangeClosed(9, 100).map(i -> {
                impl.save(i, false);
                return i;
            }).count();
        Assertions.assertEquals(expectedCount, impl.size());
    }

    /**
     * Method: obsolete(long commitId).
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
        Assertions.assertArrayEquals(expected, impl.getAll());

        Assertions.assertEquals(10L, impl.getMin().get().longValue());

        try (StatefulRedisConnection<String, String> conn = redisClient.connect()) {
            Assertions.assertEquals(0, conn.sync().exists("test.status.20").longValue());
            Assertions.assertEquals(0, conn.sync().exists("test.status.9").longValue());
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
        Assertions.assertEquals(expected.length, impl.size());
        Assertions.assertArrayEquals(expected, impl.getAll());

        Assertions.assertEquals(10L, impl.getMin().get().longValue());
        Assertions.assertEquals(99L, impl.getMax().get().longValue());
    }

    @Test
    public void testGetUnreadiness() throws Exception {
        impl.save(1, false);
        impl.save(2, false);
        impl.save(3, true);

        Assertions.assertEquals(2, Arrays.stream(impl.getUnreadiness()).filter(i -> i == 1 | i == 2).count());
    }

    @Test
    public void testIsObsolete() throws Exception {
        impl.save(1, true);
        impl.save(2, true);
        Assertions.assertFalse(impl.isObsolete(1));
        Assertions.assertFalse(impl.isObsolete(2));

        impl.obsolete(1, 2);
        Assertions.assertTrue(impl.isObsolete(1));
        Assertions.assertTrue(impl.isObsolete(2));
    }

    /**
     * 测试如果状态为unknown,isReady在检查到一个阀值时会自动设置为就绪状态.
     */
    @Test
    public void testUnknownNumber() throws Exception {
        for (int i = 0; i < 30; i++) {
            Assertions.assertFalse(impl.isReady(Integer.MAX_VALUE));
        }
        Assertions.assertTrue(impl.isReady(Integer.MAX_VALUE));
    }

    @Test
    public void testObsoleteAfterSave() throws Exception {
        impl.save(100, true);
        impl.save(200, true);
        impl.obsolete(100);
        impl.save(100, true);

        Assertions.assertFalse(impl.isReady(100));
        Assertions.assertTrue(impl.isObsolete(100));
        Assertions.assertEquals(200, impl.getMin().get().longValue());
        Assertions.assertEquals(1, impl.size());
        Assertions.assertTrue(Arrays.equals(new long[] {200L}, impl.getAll()));
    }

    /**
     * 测试并发保存和淘汰,查询状态是否正常.
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
            /*
             * 有两种可能,先淘汰那不应该保存,先保存应该状态为淘汰.
             */
            if (impl.isObsolete(i)) {

                Assertions.assertFalse(impl.isReady(i));

            } else if (impl.isReady(i + 1)) {

                Assertions.assertFalse(impl.isObsolete(i));

            }
        }
    }
}
