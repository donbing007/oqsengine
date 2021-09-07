package com.xforceplus.ultraman.oqsengine.status.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CDCStatusServiceImpl Tester.
 *
 * @author xujia
 * @version 1.0 11/16/2020
 * @since <pre>Nov 16, 2020</pre>
 */
@ExtendWith({RedisContainer.class})
public class CDCStatusServiceImplTest {

    private RedisClient redisClient;
    private CDCStatusServiceImpl impl;
    private String statusKey = "status-cdc";
    private String ackKey = "ack-cdc";
    private String heartBeatKey = "cdc-heartBeat";
    private String notReadyKey = "cdc-commitId-notReady";
    private StatefulRedisConnection<String, String> conn;

    /**
     * 准备.
     */
    @BeforeEach
    public void before() throws Exception {

        redisClient = CommonInitialization.getInstance().getRedisClient();

        ObjectMapper objectMapper = new ObjectMapper();
        impl = new CDCStatusServiceImpl(statusKey, ackKey, heartBeatKey, notReadyKey);
        ReflectionTestUtils.setField(impl, "redisClient", redisClient);
        ReflectionTestUtils.setField(impl, "objectMapper", objectMapper);
        impl.init();

        conn = redisClient.connect();
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        impl.destroy();
        impl = null;

        conn.close();
        InitializationHelper.clearAll();
    }

    @Test
    public void testSaveGet() throws Exception {
        CDCMetrics metrics = new CDCMetrics();
        metrics.setBatchId(100);
        Assertions.assertTrue(impl.saveUnCommit(metrics));
        metrics = impl.getUnCommit().get();
        Assertions.assertEquals(100, metrics.getBatchId());
    }

    @Test
    public void testHeartBeat() throws Exception {
        Assertions.assertTrue(impl.heartBeat());
        Assertions.assertTrue(impl.heartBeat());
        Assertions.assertTrue(impl.heartBeat());

        long heartBeatValue = Long.parseLong(conn.sync().get(heartBeatKey));
        Assertions.assertEquals(3, heartBeatValue);
    }

    /**
     * 如果逻辑时间达到了Long.MAX_VALUE进行回卷.
     */
    @Test
    public void testHeartBeatRewind() throws Exception {
        conn.sync().set(heartBeatKey, Long.toString(Long.MAX_VALUE));

        impl.heartBeat();

        Assertions.assertTrue(impl.isAlive());

        Assertions.assertEquals("0", conn.sync().get(heartBeatKey));
    }

    @Test
    public void testHeartBeatNotExist() throws Exception {
        conn.sync().del(heartBeatKey);

        Assertions.assertTrue(impl.isAlive());

        impl.heartBeat();

        Assertions.assertTrue(impl.isAlive());
    }

    @Test
    public void testSaveAck() throws Exception {
        CDCAckMetrics ack = new CDCAckMetrics(CDCStatus.CONNECTED);
        Assertions.assertTrue(impl.saveAck(ack));

        ack = impl.getAck().get();
        Assertions.assertEquals(CDCStatus.CONNECTED, ack.getCdcConsumerStatus());
    }
} 
