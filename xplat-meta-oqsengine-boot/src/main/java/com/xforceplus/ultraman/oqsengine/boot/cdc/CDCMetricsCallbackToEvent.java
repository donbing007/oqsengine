package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import org.springframework.context.ApplicationEventPublisher;

/**
 * cdc metrics callback
 */
public class CDCMetricsCallbackToEvent implements CDCMetricsCallback {

    private ApplicationEventPublisher publisher;

    public CDCMetricsCallbackToEvent(ApplicationEventPublisher publisher){
        this.publisher = publisher;
    }


    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        publisher.publishEvent(ackMetrics);
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        //cdcMetrics.
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return null;
    }
}
