package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.CDCTestHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * desc :.
 * name : ConnectorTest
 *
 * @author : xujia 2020/11/19
 * @since : 1.8
 */
public class ConnectorTest extends CDCTestHelper {

    private CDCDaemonService cdcDaemonService;

    @BeforeEach
    public void before() throws Exception {
        super.init(true);
        cdcDaemonService = initDaemonService();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(true);
    }

    @Test
    public void testStartFromDisConnected() throws InterruptedException {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);
        mockRedisCallbackService.cdcSaveLastUnCommit(cdcMetricsService.getCdcMetrics());

        cdcDaemonService.startDaemon();

        Thread.sleep(10_000);

        Assertions.assertEquals(CDCStatus.CONNECTED,
            cdcMetricsService.getCdcMetrics().getCdcAckMetrics().getCdcConsumerStatus());
        Assertions.assertEquals(CDCStatus.CONNECTED, mockRedisCallbackService.getAckMetrics().getCdcConsumerStatus());
    }
}
