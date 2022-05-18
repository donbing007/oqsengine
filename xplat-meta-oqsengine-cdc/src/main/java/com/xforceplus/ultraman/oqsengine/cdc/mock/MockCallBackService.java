package com.xforceplus.ultraman.oqsengine.cdc.mock;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class MockCallBackService implements CDCMetricsCallback {

    private CDCAckMetrics ackMetrics;
    private CDCMetrics cdcMetrics;
    private long heartBeat;
    private long notReady;

    private CommitIdStatusService commitIdStatusService;
    private AtomicInteger executed = new AtomicInteger(0);

    private long lastConsumerTime = 0;

    public MockCallBackService(CommitIdStatusService commitIdStatusService) {
        this.commitIdStatusService = commitIdStatusService;
    }

    /**
     * 重置.
     */
    public void reset() {
        cdcMetrics = null;
        ackMetrics = null;
        executed = new AtomicInteger(0);
    }

    @Override
    public void ack(CDCAckMetrics ackMetrics) {
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
    public List<Long> notReady(List<Long> commitIds) {
        return new ArrayList<>();
    }

    @Override
    public void saveLastUnCommit(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        return cdcMetrics;
    }

    @Override
    public boolean isReady(long commitId) {
        return commitId != this.notReady;
    }

    public AtomicInteger getExecuted() {
        return executed;
    }

    public CDCAckMetrics getAckMetrics() {
        return ackMetrics;
    }
}
