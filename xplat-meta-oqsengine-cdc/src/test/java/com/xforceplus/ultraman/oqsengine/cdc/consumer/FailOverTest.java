package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.TestCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : FailOverTest
 *
 * @author : xujia
 * date : 2020/11/11
 * @since : 1.8
 */
public class FailOverTest extends AbstractContainer {
    private MockRedisCallbackService mockRedisCallbackService;

    private CDCDaemonService cdcDaemonService;

    @Before
    public void before() throws Exception {

        initMaster();

        initDaemonService();
    }


    private void initDaemonService() throws SQLException, InterruptedException {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init("localhost",
                environment.getServicePort("canal-server_1", 11111),
                "nly-v1", "root", "xplat");

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);
    }

}
