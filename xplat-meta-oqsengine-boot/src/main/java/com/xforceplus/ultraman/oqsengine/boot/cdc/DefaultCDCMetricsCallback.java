package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cdc metrics callback.
 */
public class DefaultCDCMetricsCallback implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(DefaultCDCMetricsCallback.class);

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Override
    public void ack(CDCAckMetrics ackMetrics) {
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
    public void saveLastUnCommit(CDCMetrics cdcMetrics) {
        cdcStatusService.saveUnCommit(cdcMetrics);
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        Optional<CDCMetrics> cdcMetricsOp = cdcStatusService.getUnCommit();
        return cdcMetricsOp.orElse(null);
    }

    @Override
    public boolean isReady(long commitId) {
        return commitIdStatusService.isReady(commitId);
    }

    @Override
    public List<Long> notReady(List<Long> commitIds) {
        long[] checks = new long[commitIds.size()];
        for (int i = 0; i < commitIds.size(); i++) {
            checks[i] = commitIds.get(i);
        }
        boolean[] res = commitIdStatusService.isReady(checks);

        List<Long> notReadyIds = new ArrayList<>();
        for (int i = 0; i < res.length; i++) {
            if (!res[i]) {
                notReadyIds.add(checks[i]);
            }
        }

        return notReadyIds;
    }
}
