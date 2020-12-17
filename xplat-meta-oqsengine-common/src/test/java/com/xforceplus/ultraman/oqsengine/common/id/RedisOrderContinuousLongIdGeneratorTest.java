package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractRedisContainerTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * RedisOrderContinuousLongIdGenerator Tester.
 *
 * @author <Authors name>
 * @version 1.0 12/17/2020
 * @since <pre>Dec 17, 2020</pre>
 */
public class RedisOrderContinuousLongIdGeneratorTest extends AbstractRedisContainerTest {

    private RedisClient redisClient;
    private RedisOrderContinuousLongIdGenerator idGenerator;

    private StatefulRedisConnection<String, String> conn;

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("status.redis.ip");
        int redisPort = Integer.parseInt(System.getProperty("status.redis.port"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        idGenerator = new RedisOrderContinuousLongIdGenerator(redisClient, "test");
        idGenerator.init();

        conn = redisClient.connect();
    }

    @After
    public void after() throws Exception {
        idGenerator.destroy();

        conn.close();

    }

    @Test
    public void testNext() throws Exception {
        Assert.assertEquals(1L, idGenerator.next().longValue());
        Assert.assertEquals(2L, idGenerator.next().longValue());
        Assert.assertEquals(3L, idGenerator.next().longValue());
    }

    @Test
    public void testConcurrentNext() throws Exception {
        Map<Long, Object> buff = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                Long id = idGenerator.next();
                buff.put(id, new Object());
                latch.countDown();
            });
        }

        latch.await();

        Assert.assertEquals(100, buff.size());
    }

} 
