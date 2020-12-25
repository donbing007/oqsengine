package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerHelper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.*;

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
public class RedisOrderContinuousLongIdGeneratorTest {

    private RedisClient redisClient;
    private RedisOrderContinuousLongIdGenerator idGenerator;

    private StatefulRedisConnection<String, String> conn;

    @BeforeClass
    public static void beforeTestClass() {
        ContainerHelper.startRedis();
    }

    @AfterClass
    public static void afterClass() {
        ContainerHelper.reset();
    }

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        idGenerator = new RedisOrderContinuousLongIdGenerator(redisClient, "test", () -> 0L);
        idGenerator.init();

        conn = redisClient.connect();
    }

    @After
    public void after() throws Exception {
        idGenerator.destroy();

        conn.close();

        conn.close();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
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
