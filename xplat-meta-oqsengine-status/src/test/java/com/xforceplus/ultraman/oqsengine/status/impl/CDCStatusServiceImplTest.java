package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.AbstractRedisContainerTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CDCStatusServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/16/2020
 * @since <pre>Nov 16, 2020</pre>
 */
public class CDCStatusServiceImplTest extends AbstractRedisContainerTest {

    private RedisClient redisClient;
    private CDCStatusServiceImpl impl;
    private String key = "cdc";

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("status.redis.ip");
        int redisPort = Integer.parseInt(System.getProperty("status.redis.port"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        ObjectMapper objectMapper = new ObjectMapper();
        impl = new CDCStatusServiceImpl(key);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        ReflectionTestUtils.setField(impl, "objectMapper", objectMapper);
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

    @Test
    public void testSaveGet() throws Exception {
        CDCMetrics metrics = new CDCMetrics();
        metrics.setBatchId(100);
        Assert.assertTrue(impl.save(metrics));
        metrics = impl.get().get();
        Assert.assertEquals(100, metrics.getBatchId());
    }

} 
