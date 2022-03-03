package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : CDCMetricsRecorder
 *
 * @author : xujia 2020/11/13
 * @since : 1.8
 */
public class CDCMetricsRecorder {

    final Logger logger = LoggerFactory.getLogger(CDCMetricsRecorder.class);

    private CDCMetrics cdcMetrics;
    private long start;


    /**
     * 开始记录.
     */
    public CDCMetricsRecorder startRecord(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        start = System.currentTimeMillis();
        //  将上一次的剩余信息设置回来
        cdcMetrics = new CDCMetrics();

        cdcMetrics.setBatchId(batchId);

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-metrics-record] start consume batch, batchId : {}", batchId);
        }

        if (null != cdcUnCommitMetrics) {
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitIds(cdcUnCommitMetrics.getUnCommitIds());

            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-metrics-record] current batch : {} have last batch un-commit ids : {}",
                    batchId, JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            }
        }

        return this;
    }

    /**
     * 结束记录.
     */
    public CDCMetricsRecorder finishRecord(int syncCount) {
        cdcMetrics.getCdcAckMetrics().setExecuteRows(syncCount);
        cdcMetrics.getCdcAckMetrics().setTotalUseTime(System.currentTimeMillis() - start);

        if (logger.isDebugEnabled()) {
            logger.debug(
                "[cdc-metrics-record] finish consume batch, batchId : {}, success sync rows : {}, totalUseTime : {}",
                cdcMetrics.getBatchId(), cdcMetrics.getCdcAckMetrics().getExecuteRows(),
                cdcMetrics.getCdcAckMetrics().getTotalUseTime());
        }

        return this;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }
}
