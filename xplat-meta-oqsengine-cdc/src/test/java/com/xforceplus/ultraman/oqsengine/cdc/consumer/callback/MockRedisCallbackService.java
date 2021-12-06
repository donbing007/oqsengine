package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CDCStatusServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : MockRedisCallbackService
 *
 * @author : xujia 2020/11/10
 * @since : 1.8
 */
public class MockRedisCallbackService implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(MockRedisCallbackService.class);

    private AtomicInteger executed = new AtomicInteger(0);

    private CDCMetrics cdcMetrics;
    private CDCAckMetrics ackMetrics;
    private CommitIdStatusService commitIdStatusService;
    private CDCStatusServiceImpl cdcStatusService;
    private long heartBeat;
    private long notReady;

    /**
     * 重置.
     */
    public void reset() {
        cdcMetrics = null;
        ackMetrics = null;
        executed = new AtomicInteger(0);
    }

    private long lastConsumerTime = 0;

    public CDCAckMetrics getAckMetrics() {
        return ackMetrics;
    }

    public MockRedisCallbackService(CommitIdStatusService commitIdStatusService) {
        this.commitIdStatusService = commitIdStatusService;
    }

    public MockRedisCallbackService(CommitIdStatusService commitIdStatusService,
                                    CDCStatusServiceImpl cdcStatusService) {
        this.commitIdStatusService = commitIdStatusService;
        this.cdcStatusService = cdcStatusService;
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        this.ackMetrics = ackMetrics;

        if (ackMetrics.getCdcConsumerStatus() == CDCStatus.CONNECTED) {
            if (null != commitIdStatusService) {
                ackMetrics.getCommitList().forEach(
                    id -> {
                        commitIdStatusService.obsolete(id);
                    }
                );
            }

            if (this.ackMetrics.getLastConsumerTime() > lastConsumerTime) {
                executed.addAndGet(cdcMetrics.getCdcAckMetrics().getExecuteRows());
                lastConsumerTime = cdcMetrics.getCdcAckMetrics().getLastConsumerTime();
            }
        }
    }

    @Override
    public void heartBeat() {
        this.heartBeat = System.currentTimeMillis();
    }

    @Override
    public void notReady(long commitId) {
        this.notReady = commitId;
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return cdcMetrics;
    }

    @Override
    public boolean isReadyCommit(long commitId) {
        return true;
    }

    @Override
    public List<Long> isNotReadyCommits(List<Long> commitIds) {
        return new ArrayList<>();
    }

    public AtomicInteger getExecuted() {
        return executed;
    }
}
