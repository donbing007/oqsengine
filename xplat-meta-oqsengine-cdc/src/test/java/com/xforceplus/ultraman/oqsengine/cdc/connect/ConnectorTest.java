package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerHelper;
import org.junit.*;
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
public class ConnectorTest extends CDCAbstractContainer {

    private MockRedisCallbackService mockRedisCallbackService;

    private CDCDaemonService cdcDaemonService;

    private CDCMetricsService cdcMetricsService;

    @BeforeClass
    public static void beforeClass() {
        ContainerHelper.startMysql();
        ContainerHelper.startManticore();
        ContainerHelper.startRedis();
        ContainerHelper.startCannal();
    }

    @Before
    public void before() throws Exception {
        initDaemonService();
        clear();
    }

    @After
    public void after() throws SQLException {
        cdcDaemonService.stopDaemon();
        clear();
        closeAll();
    }

    private void initDaemonService() throws Exception {
        cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();

        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initAll());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);
    }


    @Test
    public void testStartFromDIS_CONNECTED() throws InterruptedException {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);
        mockRedisCallbackService.cdcSaveLastUnCommit(cdcMetricsService.getCdcMetrics());

        cdcDaemonService.startDaemon();

        Thread.sleep(10_000);

        Assert.assertEquals(CDCStatus.CONNECTED, cdcMetricsService.getCdcMetrics().getCdcAckMetrics().getCdcConsumerStatus());
        Assert.assertEquals(CDCStatus.CONNECTED, mockRedisCallbackService.getAckMetrics().getCdcConsumerStatus());
    }
}
