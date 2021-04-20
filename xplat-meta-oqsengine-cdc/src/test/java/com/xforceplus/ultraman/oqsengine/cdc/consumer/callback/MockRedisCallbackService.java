package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CDCStatusServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * desc :
 * name : MockRedisCallbackService
 *
 * @author : xujia
 * date : 2020/11/10
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

    public MockRedisCallbackService(CommitIdStatusService commitIdStatusService, CDCStatusServiceImpl cdcStatusService) {
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
//
//        logger.info("mock cdcAck info : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
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
//        logger.info("mock cdcUnCommitMetrics info : {}", JSON.toJSON(cdcMetrics));
        this.cdcMetrics = cdcMetrics;
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
//        logger.info("mock queryLastUnCommit info : {}", JSON.toJSON(cdcMetrics));
        return cdcMetrics;
    }

    @Override
    public boolean isReadyCommit(long commitId) {
        return true;
    }

    @Override
    public Map<String, String> querySkipRows() {
        return cdcStatusService.querySkipRows();
    }

    @Override
    public void expiredSkipRows(String[] skips) {
        cdcStatusService.expiredSkipRows(skips);
    }

    public AtomicInteger getExecuted() {
        return executed;
    }
}
