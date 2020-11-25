package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.status.AbstractRedisContainerTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("status.redis.ip");
        int redisPort = Integer.parseInt(System.getProperty("status.redis.port"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        impl = new CommitIdStatusServiceImpl(key);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        impl.init();
    }

    @After
    public void after() throws Exception {
        impl.destroy();
        impl = null;

        redisClient.connect().sync().del(key);
        redisClient.shutdown();
        redisClient = null;
    }

    /**
     * Method: save(long commitId)
     */
    @Test
    public void testSave() throws Exception {
        long expectedTotal = LongStream.rangeClosed(1, 1000).parallel().map(i -> impl.save(i)).sum();

        long[] allIds = impl.getAll();
        long actualTotal = Arrays.stream(allIds).sum();
        Assert.assertEquals(expectedTotal, actualTotal);
    }

    /**
     * Method: getMin()
     */
    @Test
    public void testGetMin() throws Exception {
        long expectedMin = LongStream
            .rangeClosed(9, 1000).parallel().map(i -> impl.save(i)).min().getAsLong();

        long actualMin = impl.getMin().get();
        Assert.assertEquals(expectedMin, actualMin);
    }

    /**
     * Method: getMax()
     */
    @Test
    public void testGetMax() throws Exception {
        long expectedMax = LongStream
            .rangeClosed(9, 1000).parallel().map(i -> impl.save(i)).max().getAsLong();

        long actualMax = impl.getMax().get();
        Assert.assertEquals(expectedMax, actualMax);
    }

    /**
     * Method: getAll()
     */
    @Test
    public void testGetAll() throws Exception {
        long[] expectedAll = LongStream
            .rangeClosed(9, 1000).parallel().map(i -> impl.save(i)).sorted().toArray();

        long[] actualAll = impl.getAll();

        Assert.assertArrayEquals(expectedAll, actualAll);

    }

    /**
     * Method: size()
     */
    @Test
    public void testSize() throws Exception {
        long expectedCount = LongStream
            .rangeClosed(9, 1000).parallel().map(i -> impl.save(i)).count();
        Assert.assertEquals(expectedCount, impl.size());
    }

    /**
     * Method: obsolete(long commitId)
     */
    @Test
    public void testObsoleteCommitId() throws Exception {
        long[] expected = LongStream
            .rangeClosed(9, 1000).parallel().map(i -> impl.save(i)).filter(i -> i != 20).filter(i -> i != 9)
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
            .rangeClosed(9, 1000).map(i -> impl.save(i)).filter(i -> i != 20).filter(i -> i != 9).filter(i -> i != 1000)
            .sorted().toArray();
        impl.obsolete(20, 9, 1000);
        Assert.assertEquals(expected.length, impl.getAll().length);
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
} 
