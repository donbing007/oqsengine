package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * cdc metrics callback
 */
public class DefaultCDCMetricsCallback implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(DefaultCDCMetricsCallback.class);

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        try {
            long[] ids = ackMetrics.getCommitList().stream().mapToLong(id -> id).toArray();
            commitIdStatusService.obsolete(ids);

            cdcStatusService.saveAck(ackMetrics);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void heartBeat() {
        cdcStatusService.heartBeat();
    }

    @Override
    public void notReady(long commitId) {
        cdcStatusService.notReady(commitId);
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        cdcStatusService.saveUnCommit(cdcMetrics);
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        Optional<CDCMetrics> cdcMetricsOp = cdcStatusService.getUnCommit();
        if (cdcMetricsOp.isPresent()) {
            return cdcMetricsOp.get();
        } else {
            return null;
        }
    }

    @Override
    public boolean isReadyCommit(long commitId) {
        return commitIdStatusService.isReady(commitId);
    }
}
