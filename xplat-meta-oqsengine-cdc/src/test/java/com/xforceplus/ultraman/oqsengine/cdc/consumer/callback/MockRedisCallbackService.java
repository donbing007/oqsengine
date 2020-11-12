package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * desc :
 * name : MockRedisCallbackService
 *
 * @author : xujia
 * date : 2020/11/10
 * @since : 1.8
 */
public class MockRedisCallbackService implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(MockRedisCallbackService.class);

    private static AtomicInteger executed = new AtomicInteger(0);

    private CDCAckMetrics ackMetrics;
    private CDCMetrics cdcMetrics;

    public void reset() {
        ackMetrics = null;
        cdcMetrics = null;
        executed = new AtomicInteger(0);
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        this.ackMetrics = ackMetrics;
        logger.info("mock cdcAck info : {}", JSON.toJSON(ackMetrics));
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
        logger.info("mock cdcUnCommitMetrics info : {}", JSON.toJSON(cdcMetrics));
        executed.addAndGet(cdcMetrics.getCdcUnCommitMetrics().getExecuteJobCount());
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return cdcMetrics;
    }
}
