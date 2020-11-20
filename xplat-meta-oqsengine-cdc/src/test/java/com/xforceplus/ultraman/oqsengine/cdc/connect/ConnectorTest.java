package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;


/**
 * desc :
 * name : ConnectorTest
 *
 * @author : xujia
 * date : 2020/11/19
 * @since : 1.8
 */
public class ConnectorTest extends AbstractContainer  {

    private static final boolean isDoTest = false;

    private MockRedisCallbackService mockRedisCallbackService;

    private CDCDaemonService cdcDaemonService;

    @Before
    public void before() throws Exception {
        if (isDoTest) {
            initMaster();
            clear();
            initDaemonService();
        }
    }

    @After
    public void after() throws SQLException {
        if (isDoTest) {
            cdcDaemonService.stopDaemon();
            clear();
        }
    }

    private void initDaemonService() throws Exception {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init("172.18.31.7", 11111,
                "oqscdc", "canal", "canal");

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);
    }

    @Test
    public void testServerStartAndStop() throws InterruptedException {
        if (isDoTest) {
            cdcDaemonService.startDaemon();

            Thread.sleep(10_000);

            while (true) {
                Thread.sleep(60_000);
//                System.out.println("canal server down.");
//                cannal.stop();
//                Thread.sleep(30_000);
//                cannal.start();
//                Thread.sleep(30_000);
//                System.out.println("canal server start.");
            }
        }
    }
}
