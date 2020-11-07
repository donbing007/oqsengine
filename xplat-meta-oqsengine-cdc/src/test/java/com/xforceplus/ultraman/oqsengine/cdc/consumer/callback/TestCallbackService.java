package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;

/**
 * desc :
 * name : TestCallbackService
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class TestCallbackService implements CDCMetricsCallback {

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {

    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {

    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return null;
    }
}
