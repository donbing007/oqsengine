package com.xforceplus.ultraman.oqsengine.cdc.metrics.dto;

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
        this.cdcUnCommitMetrics = new CDCUnCommitMetrics();
    }

    public CDCMetrics(CDCUnCommitMetrics cdcUnCommitMetrics) throws CloneNotSupportedException {
        this.cdcAckMetrics = new CDCAckMetrics(CDCStatus.CONNECTED);
        //  必须深copy
        this.cdcUnCommitMetrics = (CDCUnCommitMetrics) cdcUnCommitMetrics.clone();
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
