package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;
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
    public void cdcCallBack(CDCMetrics cdcMetrics) {
        publisher.publishEvent(cdcMetrics);
    }
}
