package com.xforceplus.ultraman.oqsengine.cdc;



import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;


import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : CDCDaemonServiceTest
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class CDCDaemonServiceTest extends CDCAbstractContainer {

    private CDCDaemonService cdcDaemonService;

    private boolean isTest = true;

    private MockRedisCallbackService testCallbackService;

    @Before
    public void before() throws Exception {
        initDaemonService();
        cdcDaemonService.startDaemon();
    }

    @After
    public void after() {
        cdcDaemonService.stopDaemon();
        closeAll();
    }

    private void initDaemonService() throws Exception {

        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        testCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", testCallbackService);

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initAll());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);
    }

    @Test
    public void binlogSyncTest() throws InterruptedException {
        if (isTest) {
            Thread.sleep(10_000);
        }
    }
}
