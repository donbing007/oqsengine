package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

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

    public void heartBeat(long batchId) {
        this.batchId = batchId;
        this.cdcAckMetrics.setLastConnectedTime(System.currentTimeMillis());
        this.cdcAckMetrics.setLastUpdateTime(System.currentTimeMillis());
    }

    public void consumeSuccess(CDCAckMetrics temp) {
        this.cdcAckMetrics.setLastConsumerTime(System.currentTimeMillis());
        this.cdcAckMetrics.setExecuteRows(temp.getExecuteRows());
        this.cdcAckMetrics.setTotalUseTime(temp.getTotalUseTime());
        if (!temp.getCommitList().isEmpty()) {
            this.cdcAckMetrics.setCommitList(temp.getCommitList());
        }

        if (temp.getMaxSyncUseTime() > ZERO) {
            this.cdcAckMetrics.setMaxSyncUseTime(temp.getMaxSyncUseTime());
        }
    }

    public void resetStatus(CDCStatus cdcStatus) {
        this.cdcAckMetrics.setCdcConsumerStatus(cdcStatus);
    }
}
