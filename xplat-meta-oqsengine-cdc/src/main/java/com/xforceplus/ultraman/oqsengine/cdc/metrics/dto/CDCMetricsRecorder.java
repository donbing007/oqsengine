package com.xforceplus.ultraman.oqsengine.cdc.metrics.dto;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * desc :
 * name : CDCMetricsRecorder
 *
 * @author : xujia
 * date : 2020/11/13
 * @since : 1.8
 */
public class CDCMetricsRecorder {

    final Logger logger = LoggerFactory.getLogger(SphinxConsumerService.class);

    private CDCMetrics cdcMetrics;
    private StopWatch timeRecorder = new StopWatch();

    public CDCMetricsRecorder startRecord(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        timeRecorder.start();
        //  将上一次的剩余信息设置回来
        cdcMetrics = new CDCMetrics();

        if (null != cdcUnCommitMetrics) {
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(cdcUnCommitMetrics.getUnCommitId());
            cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues().putAll(cdcUnCommitMetrics.getUnCommitEntityValues());
        }
        cdcMetrics.setBatchId(batchId);
        return this;
    }

    public CDCMetricsRecorder finishRecord(int syncCount) {
        timeRecorder.stop();
        cdcMetrics.getCdcAckMetrics().setExecuteRows(syncCount);
        cdcMetrics.getCdcAckMetrics().setTotalUseTime(timeRecorder.getTotalTimeMillis());


        logger.info("finish job, batchId {}, success sync raw data : {}, totalUseTime : {}",
                cdcMetrics.getBatchId(), cdcMetrics.getCdcAckMetrics().getExecuteRows(), cdcMetrics.getCdcAckMetrics().getTotalUseTime());

        return this;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }
}
