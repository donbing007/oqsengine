package com.xforceplus.ultraman.oqsengine.status.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CDCStatusServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/16/2020
 * @since <pre>Nov 16, 2020</pre>
 */
public class CDCStatusServiceImplTest extends AbstractContainer {

    private RedisClient redisClient;
    private CDCStatusServiceImpl impl;
    private String statusKey = "status-cdc";
    private String ackKey = "ack-cdc";
    private String heartBeatKey = "cdc-heartBeat";
    private String notReadyKey = "cdc-commitId-notReady";
    private StatefulRedisConnection<String, String> conn;

    @BeforeClass
    public static void beforeTestClass() {
        startRedis();
    }

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        ObjectMapper objectMapper = new ObjectMapper();
        impl = new CDCStatusServiceImpl(statusKey, ackKey, heartBeatKey, notReadyKey);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        ReflectionTestUtils.setField(impl, "objectMapper", objectMapper);
        impl.init();

        conn = redisClient.connect();
    }

    @After
    public void after() throws Exception {
        impl.destroy();
        impl = null;

        conn.close();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    @Test
    public void testSaveGet() throws Exception {
        CDCMetrics metrics = new CDCMetrics();
        metrics.setBatchId(100);
        Assert.assertTrue(impl.saveUnCommit(metrics));
        metrics = impl.getUnCommit().get();
        Assert.assertEquals(100, metrics.getBatchId());
    }

    @Test
    public void testHeartBeat() throws Exception {
        Assert.assertTrue(impl.heartBeat());
        Assert.assertTrue(impl.heartBeat());
        Assert.assertTrue(impl.heartBeat());

        long heartBeatValue = Long.parseLong(conn.sync().get(heartBeatKey));
        Assert.assertEquals(3, heartBeatValue);
    }

    /**
     * 如果逻辑时间达到了Long.MAX_VALUE进行回卷.
     *
     * @throws Exception
     */
    @Test
    public void testHeartBeatRewind() throws Exception {
        conn.sync().set(heartBeatKey, Long.toString(Long.MAX_VALUE));

        impl.heartBeat();

        Assert.assertTrue(impl.isAlive());

        Assert.assertEquals("0", conn.sync().get(heartBeatKey));
    }

    @Test
    public void testHeartBeatNotExist() throws Exception {
        conn.sync().del(heartBeatKey);

        Assert.assertTrue(impl.isAlive());

        impl.heartBeat();

        Assert.assertTrue(impl.isAlive());
    }

    @Test
    public void testSaveAck() throws Exception {
        CDCAckMetrics ack = new CDCAckMetrics(CDCStatus.CONNECTED);
        Assert.assertTrue(impl.saveAck(ack));

        ack = impl.getAck().get();
        Assert.assertEquals(CDCStatus.CONNECTED, ack.getCdcConsumerStatus());
    }
} 
