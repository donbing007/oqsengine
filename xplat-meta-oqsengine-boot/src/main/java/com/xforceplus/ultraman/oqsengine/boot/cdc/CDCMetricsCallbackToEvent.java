package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.TimeGauge;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * cdc metrics callback
 */
public class CDCMetricsCallbackToEvent implements CDCMetricsCallback {

    @Resource
    private ApplicationEventPublisher publisher;

    @Resource
    private CDCStatusService cdcStatusService;

    private AtomicLong cdcSyncTime = new AtomicLong(0);
    private TimeGauge.Builder<AtomicLong> cdcSyncTimeGauge;

    @PostConstruct
    public void init() {
        cdcSyncTimeGauge =
            TimeGauge.builder(
                MetricsDefine.CDC_SYNC_DELAY_LATENCY_SECONDS, cdcSyncTime, TimeUnit.SECONDS, AtomicLong::get);
        cdcSyncTimeGauge.register(Metrics.globalRegistry);
    }

    public CDCMetricsCallbackToEvent(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        publisher.publishEvent(ackMetrics);

        cdcSyncTime.set(ackMetrics.getTotalUseTime() / 1000);
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
