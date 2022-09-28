package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * RedisOrderContinuousLongIdGenerator Tester.
 *
 * @author dongbin
 * @version 1.0 12/17/2020
 * @since <pre>Dec 17, 2020</pre>
 */
@ExtendWith({RedisContainer.class})
public class RedisOrderContinuousLongIdGeneratorTest {

    private RedisOrderContinuousLongIdGenerator idGenerator;

    private StatefulRedisConnection<String, String> conn;

    private RedisClient redisClient;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        redisClient = CommonInitialization.getInstance().getRedisClient();
        idGenerator = new RedisOrderContinuousLongIdGenerator(redisClient, "test");
        idGenerator.init();

        conn = redisClient.connect();
    }

    /**
     * 清理
     */
    @AfterEach
    public void after() throws Exception {
        idGenerator.destroy();

        if (null != conn) {
            conn.close();
        }

        InitializationHelper.clearAll();
    }

    @Test
    public void testCurrent() throws Exception {
        Assertions.assertEquals(1L, idGenerator.next().longValue());
        Assertions.assertEquals(1L, idGenerator.current().longValue());

        Assertions.assertEquals(2L, idGenerator.next().longValue());
        Assertions.assertEquals(2L, idGenerator.current().longValue());

        Assertions.assertEquals(3L, idGenerator.next().longValue());
        Assertions.assertEquals(3L, idGenerator.current().longValue());
    }

    @Test
    public void testNext() throws Exception {
        Assertions.assertEquals(1L, idGenerator.next().longValue());
        Assertions.assertEquals(2L, idGenerator.next().longValue());
        Assertions.assertEquals(3L, idGenerator.next().longValue());
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

        Assertions.assertEquals(100, buff.size());
    }

    @Test
    public void testNextWithNs() throws Exception {
        String ns0 = "ns0";
        String ns1 = "ns1";

        CountDownLatch latch = new CountDownLatch(200);
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                idGenerator.next(ns0);
                latch.countDown();
            });
        }

        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                idGenerator.next(ns1);
                latch.countDown();
            });
        }

        latch.await();
        Assertions.assertEquals(101, idGenerator.next(ns0));
        Assertions.assertEquals(101, idGenerator.next(ns1));
    }

    @Test
    public void testReset() throws Exception {
        Assertions.assertEquals(1L, idGenerator.next().longValue());
        Assertions.assertEquals(2L, idGenerator.next().longValue());
        Assertions.assertEquals(3L, idGenerator.next().longValue());

        idGenerator.reset();

        Assertions.assertEquals(1L, idGenerator.next().longValue());
    }

}
