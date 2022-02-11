package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * desc :.
 * name : ConnectorTest
 *
 * @author : xujia 2020/11/19
 * @since : 1.8
 */
@Disabled
public class ConnectorTest extends AbstractCDCTestHelper {

    final Logger logger = LoggerFactory.getLogger(ConnectorTest.class);

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
    public void testStartFromDisConnected() throws Exception {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);
        mockRedisCallbackService.cdcSaveLastUnCommit(cdcMetricsService.getCdcMetrics());

        cdcDaemonService.init();

        CDCStatus cdcMetricsServiceStatus;
        CDCStatus mockRedisCallbackServiceStatus;
        for (int i = 0; i < 10000; i++) {
            cdcMetricsServiceStatus = cdcMetricsService.getCdcMetrics().getCdcAckMetrics().getCdcConsumerStatus();
            CDCAckMetrics cdcAckMetrics = mockRedisCallbackService.getAckMetrics();
            if (cdcAckMetrics == null) {

                logger.info("The status query is not CONNECTED, wait 1 second and try again.");

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));

                continue;

            } else {
                mockRedisCallbackServiceStatus = cdcAckMetrics.getCdcConsumerStatus();
            }

            if (cdcMetricsServiceStatus == CDCStatus.CONNECTED
                && mockRedisCallbackServiceStatus == CDCStatus.CONNECTED) {

                break;

            } else {
                // 等待1秒.

                logger.info("The status query is not CONNECTED, wait 1 second and try again.");

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            }
        }

        Assertions.assertEquals(CDCStatus.CONNECTED,
            cdcMetricsService.getCdcMetrics().getCdcAckMetrics().getCdcConsumerStatus());
        Assertions.assertEquals(CDCStatus.CONNECTED, mockRedisCallbackService.getAckMetrics().getCdcConsumerStatus());
    }
}