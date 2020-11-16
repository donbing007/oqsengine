package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;

/**
 * desc :
 * name : CDCMetrics
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCMetrics {
    private long batchId;
    private CDCAckMetrics cdcAckMetrics;
    private CDCUnCommitMetrics cdcUnCommitMetrics;

    public CDCMetrics() {
        this.cdcAckMetrics = new CDCAckMetrics(CDCStatus.CONNECTED);
        this.cdcAckMetrics.setLastConnectedTime(System.currentTimeMillis());
        this.cdcUnCommitMetrics = new CDCUnCommitMetrics();
    }

    public CDCMetrics(long batchId, CDCAckMetrics cdcAckMetrics, CDCUnCommitMetrics cdcUnCommitMetrics) {
        this.batchId = batchId;
        this.cdcAckMetrics = cdcAckMetrics;
        this.cdcUnCommitMetrics = cdcUnCommitMetrics;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public CDCAckMetrics getCdcAckMetrics() {
        return cdcAckMetrics;
    }

    public void setCdcAckMetrics(CDCAckMetrics cdcAckMetrics) {
        this.cdcAckMetrics = cdcAckMetrics;
    }

    public CDCUnCommitMetrics getCdcUnCommitMetrics() {
        return cdcUnCommitMetrics;
    }

    public void setCdcUnCommitMetrics(CDCUnCommitMetrics cdcUnCommitMetrics) {
        this.cdcUnCommitMetrics = cdcUnCommitMetrics;
    }
}
