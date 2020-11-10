package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :
 * name : TestCallbackService
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class TestCallbackService implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(TestCallbackService.class);

    private CDCAckMetrics ackMetrics;
    private CDCMetrics cdcMetrics;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        this.ackMetrics = ackMetrics;
        logger.info("cdcAck info : {}", JSON.toJSON(ackMetrics));
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
        logger.info("cdcUnCommitMetrics info : {}", JSON.toJSON(cdcMetrics));
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return cdcMetrics;
    }

    public CDCAckMetrics getAckMetrics() {
        return ackMetrics;
    }
}
