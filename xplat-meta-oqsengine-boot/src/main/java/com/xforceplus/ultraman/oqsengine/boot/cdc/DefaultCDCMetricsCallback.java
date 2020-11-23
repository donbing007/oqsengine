package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.TimeGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * cdc metrics callback
 */
public class DefaultCDCMetricsCallback implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(DefaultCDCMetricsCallback.class);

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    private AtomicLong cdcSyncTime = new AtomicLong(0);
    private AtomicLong cdcExecutedCount = new AtomicLong(0);
    private AtomicLong cdcMaxUseTime = new AtomicLong(0);
    private TimeGauge.Builder<AtomicLong> cdcSyncTimeGauge;
    private TimeGauge.Builder<AtomicLong> cdcExecutedCountGauge;
    private TimeGauge.Builder<AtomicLong> cdcMaxUseTimeGauge;

    @PostConstruct
    public void init() {
        cdcSyncTimeGauge =
            TimeGauge.builder(
                MetricsDefine.CDC_SYNC_DELAY_LATENCY_SECONDS, cdcSyncTime, TimeUnit.SECONDS, AtomicLong::get);

        cdcExecutedCountGauge =
            TimeGauge.builder(
                MetricsDefine.CDC_SYNC_EXECUTED_COUNT, cdcExecutedCount, TimeUnit.SECONDS, AtomicLong::get);

        cdcMaxUseTimeGauge =
            TimeGauge.builder(
                MetricsDefine.CDC_SYNC_MAX_HANDLE_LATENCY_SECONDS, cdcMaxUseTime, TimeUnit.SECONDS, AtomicLong::get);

        cdcSyncTimeGauge.register(Metrics.globalRegistry);
        cdcExecutedCountGauge.register(Metrics.globalRegistry);
        cdcMaxUseTimeGauge.register(Metrics.globalRegistry);
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        List<Long> idList = ackMetrics.getCommitList();
        long[] ids = idList.stream().mapToLong(id -> id).toArray();
        commitIdStatusService.obsolete(ids);

        if (logger.isDebugEnabled()) {
            Arrays.stream(ids).parallel().forEach(id -> {
                logger.debug("The {} commit number has been synchronized successfully.", id);
            });
        }

        cdcSyncTime.set(ackMetrics.getTotalUseTime() / 1000);

        cdcExecutedCount.set(ackMetrics.getExecuteRows());

        cdcMaxUseTime.set(ackMetrics.getMaxSyncUseTime());
    }

    @Override
    public void heartBeat() {
        cdcStatusService.heartBeat();

        if (logger.isDebugEnabled()) {
            logger.debug("CDC heartBeat.");
        }
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        cdcStatusService.save(cdcMetrics);

        if (logger.isDebugEnabled()) {
            String json = null;
            try {
                json = objectMapper.writeValueAsString(cdcMetrics);
            } catch (JsonProcessingException e) {
                json = "{}";
            }
            logger.debug("Save cdc status {}", json);
        }
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
