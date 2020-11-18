package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.alibaba.fastjson.JSON;
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

    public void reset() {
        cdcMetrics = null;
        executed.set(0);
    }

    private long lastConsumerTime = 0;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        cdcMetrics.setCdcAckMetrics(ackMetrics);

        if (cdcMetrics.getCdcAckMetrics().getLastConsumerTime() > lastConsumerTime) {
            executed.addAndGet(cdcMetrics.getCdcAckMetrics().getExecuteRows());
            lastConsumerTime = cdcMetrics.getCdcAckMetrics().getLastConsumerTime();
        }

//        logger.info("mock cdcAck info : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
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

    public AtomicInteger getExecuted() {
        return executed;
    }
}
