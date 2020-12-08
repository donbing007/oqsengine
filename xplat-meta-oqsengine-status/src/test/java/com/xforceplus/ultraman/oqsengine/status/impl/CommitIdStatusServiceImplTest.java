package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.status.AbstractRedisContainerTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * CommitIdStatusServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/13/2020
 * @since <pre>Nov 13, 2020</pre>
 */
public class CommitIdStatusServiceImplTest extends AbstractRedisContainerTest {

    final Logger logger = LoggerFactory.getLogger(CommitIdStatusServiceImplTest.class);

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl impl;
    private String key = "test";
    private String statusKeyPreifx = "test.status";

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("status.redis.ip");
        int redisPort = Integer.parseInt(System.getProperty("status.redis.port"));
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
        redisClient.shutdown();
        redisClient = null;
    }

    /**
     * Method: save(long commitId)
     */
    @Test
    public void testSaveNotReady() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 1000).map(i -> impl.save(i, false)).sum();

        long[] allIds = impl.getAll();
        long actualTotal = Arrays.stream(allIds).sum();
        Assert.assertEquals(expectedTotal, actualTotal);

        Arrays.stream(allIds).forEach(id -> {
            Assert.assertFalse(impl.isReady(id));
        });
    }

    @Test
    public void testSaveReady() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 1000).map(i -> impl.save(i, true)).sum();

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

        Assert.assertFalse(impl.isReady(commitId));
        try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
            RedisCommands<String, String> commands = connect.sync();
            long size = commands.exists(statusKeyPreifx + "." + commitId);
            Assert.assertEquals(0, size);
        }
    }

    /**
     * Method: getMin()
     */
    @Test
    public void testGetMin() throws Exception {
        long expectedMin = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).min().getAsLong();

        long actualMin = impl.getMin().get();
        Assert.assertEquals(expectedMin, actualMin);
    }

    /**
     * Method: getMax()
     */
    @Test
    public void testGetMax() throws Exception {
        long expectedMax = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).max().getAsLong();

        long actualMax = impl.getMax().get();
        Assert.assertEquals(expectedMax, actualMax);
    }

    /**
     * Method: getAll()
     */
    @Test
    public void testGetAll() throws Exception {
        long[] expectedAll = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).sorted().toArray();

        long[] actualAll = impl.getAll();

        Assert.assertEquals(expectedAll.length, actualAll.length);

    }

    /**
     * Method: size()
     */
    @Test
    public void testSize() throws Exception {
        long expectedCount = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).count();
        Assert.assertEquals(expectedCount, impl.size());
    }

    /**
     * Method: obsolete(long commitId)
     */
    @Test
    public void testObsoleteCommitId() throws Exception {
        long[] expected = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).filter(i -> i != 20).filter(i -> i != 9)
            .sorted().toArray();
        impl.obsolete(20);
        impl.obsolete(9);
        Assert.assertArrayEquals(expected, impl.getAll());

        Assert.assertEquals(10L, impl.getMin().get().longValue());
    }

    /**
     * Method: obsolete(long... commitIds)
     */
    @Test
    public void testObsoleteCommitIds() throws Exception {
        long[] expected = LongStream
            .rangeClosed(9, 1000).map(i -> impl.save(i, false)).filter(i -> i != 20).filter(i -> i != 9).filter(i -> i != 1000)
            .sorted().toArray();
        impl.obsolete(20, 9, 1000);
        // Idempotent
        impl.obsolete(20, 9, 1000);
        Assert.assertEquals(expected.length, impl.size());
        Assert.assertArrayEquals(expected, impl.getAll());

        Assert.assertEquals(10L, impl.getMin().get().longValue());
        Assert.assertEquals(999L, impl.getMax().get().longValue());

        // 因为同步指标是异步的,所以等待成功.
        TimeUnit.MILLISECONDS.sleep(10);

        Field unSyncCommitIdSizeField = impl.getClass().getDeclaredField("unSyncCommitIdSize");
        unSyncCommitIdSizeField.setAccessible(true);
        AtomicLong unSyncCommitIdSize = (AtomicLong) unSyncCommitIdSizeField.get(impl);
        Assert.assertEquals(989L, unSyncCommitIdSize.longValue());
    }

    /**
     * 测试协同.
     * 调用者应该只有在isReady()给出true的情况下才能进行提交号的淘汰.
     *
     * @throws Exception
     */
    @Test
    public void testSynergy() throws Exception {
        ExecutorService worker = Executors.newFixedThreadPool(10);
        AtomicLong commitId = new AtomicLong(0);
        BlockingQueue<Long> queue = new ArrayBlockingQueue(1000);
        Queue<Long> finishdQueue = new ConcurrentLinkedQueue();
        int size = 1000;
        //写入提交号.
        worker.submit(() -> {
            for (int i = 0; i < size; i++) {
                worker.submit(() -> {

                    long commitid = commitId.incrementAndGet();

                    queue.offer(commitid);

                    impl.save(commitid, true);
                });
            }
        });

        while (finishdQueue.size() != size) {
            worker.submit(() -> {
                Long id = null;
                try {
                    id = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!impl.isReady(id)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                impl.obsolete(id);
                finishdQueue.offer(id);
            });
        }

        worker.shutdown();

        Assert.assertEquals(0, impl.getAll().length);
        Assert.assertEquals(size, finishdQueue.size());
    }
}
