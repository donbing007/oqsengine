package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :
 * name : MockRedisCallbackService
 *
 * @author : xujia
 * date : 2020/11/10
 * @since : 1.8
 */
public class MockRedisCallbackService implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(TestCallbackService.class);

    private static int executed = 0;

    private CDCAckMetrics ackMetrics;
    private CDCMetrics cdcMetrics;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        this.ackMetrics = ackMetrics;
        logger.info("mock cdcAck info : {}", JSON.toJSON(ackMetrics));
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
        logger.info("mock cdcUnCommitMetrics info : {}", JSON.toJSON(cdcMetrics));
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return cdcMetrics;
    }
}
