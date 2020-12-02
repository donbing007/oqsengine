package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private long heartBeat;

    public void reset() {
        cdcMetrics = null;
        ackMetrics = null;
        executed = new AtomicInteger(0);
    }

    private long lastConsumerTime = 0;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        this.ackMetrics = ackMetrics;

        if (ackMetrics.getCdcConsumerStatus() == CDCStatus.CONNECTED &&
                this.ackMetrics.getLastConsumerTime() > lastConsumerTime) {
            executed.addAndGet(cdcMetrics.getCdcAckMetrics().getExecuteRows());
            lastConsumerTime = cdcMetrics.getCdcAckMetrics().getLastConsumerTime();
        }
//
//        logger.info("mock cdcAck info : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
    }

    @Override
    public void heartBeat() {
        this.heartBeat = System.currentTimeMillis();
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

    public AtomicInteger getExecuted() {
        return executed;
    }
}
