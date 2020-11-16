package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * cdc metrics callback
 */
public class CDCMetricsCallbackToEvent implements CDCMetricsCallback {

    @Resource
    private ApplicationEventPublisher publisher;

    @Resource
    private CDCStatusService cdcStatusService;

    public CDCMetricsCallbackToEvent(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        publisher.publishEvent(ackMetrics);
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        publisher.publishEvent(cdcMetrics);
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        Optional<CDCMetrics> cdcMetricsOp = cdcStatusService.get();
        if (cdcMetricsOp.isPresent()) {
            return cdcMetricsOp.get();
        } else {
            return null;
        }
    }
}
