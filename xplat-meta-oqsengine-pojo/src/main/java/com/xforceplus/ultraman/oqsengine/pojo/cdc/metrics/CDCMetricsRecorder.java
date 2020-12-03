package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;

import com.alibaba.fastjson.JSON;
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

    final Logger logger = LoggerFactory.getLogger(CDCMetricsRecorder.class);

    private CDCMetrics cdcMetrics;
    private StopWatch timeRecorder = new StopWatch();


    public CDCMetricsRecorder startRecord(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        timeRecorder.start();
        //  将上一次的剩余信息设置回来
        cdcMetrics = new CDCMetrics();

        cdcMetrics.setBatchId(batchId);
        logger.debug("[cdc-metrics-record] start consume batch, batchId : {}", batchId);
        if (null != cdcUnCommitMetrics) {
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitIds(cdcUnCommitMetrics.getUnCommitIds());
            logger.debug("[cdc-metrics-record] sync last batch unCommitIds : {}"
                    , JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
        }

        return this;
    }

    public CDCMetricsRecorder finishRecord(int syncCount) {
        timeRecorder.stop();
        cdcMetrics.getCdcAckMetrics().setExecuteRows(syncCount);
        cdcMetrics.getCdcAckMetrics().setTotalUseTime(timeRecorder.getTotalTimeMillis());

        logger.info("[cdc-metrics-record] finish consume batch, batchId : {}, success sync rows : {}, totalUseTime : {}",
                cdcMetrics.getBatchId(), cdcMetrics.getCdcAckMetrics().getExecuteRows(), cdcMetrics.getCdcAckMetrics().getTotalUseTime());

        return this;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }
}
