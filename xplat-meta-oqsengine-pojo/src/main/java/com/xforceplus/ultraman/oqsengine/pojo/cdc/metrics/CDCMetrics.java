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

    public void heartBeat(long batchId, long lastConnectTime) {
        this.batchId = batchId;
        this.cdcAckMetrics.setCdcConsumerStatus(CDCStatus.CONNECTED);
        this.cdcAckMetrics.setLastConnectedTime(lastConnectTime);
        this.cdcAckMetrics.setLastUpdateTime(System.currentTimeMillis());
    }

    public void consumeSuccess(CDCMetrics temp, boolean isConnectSync) {
        this.batchId = temp.getBatchId();
        this.cdcAckMetrics.setCdcConsumerStatus(CDCStatus.CONNECTED);
        this.cdcAckMetrics.setLastConnectedTime(temp.getCdcAckMetrics().getLastConnectedTime());

        //  启动则更新LastConnectedTime, 否则为成功消费
        if (!isConnectSync) {
            this.cdcAckMetrics.setLastConsumerTime(System.currentTimeMillis());
        }

        if (!temp.getCdcAckMetrics().getCommitList().isEmpty()) {
            this.cdcAckMetrics.setCommitList(temp.getCdcAckMetrics().getCommitList());
        }

        if (temp.getCdcAckMetrics().getMaxSyncUseTime() > ZERO) {
            this.cdcAckMetrics.setExecuteRows(temp.getCdcAckMetrics().getExecuteRows());
            this.cdcAckMetrics.setMaxSyncUseTime(temp.getCdcAckMetrics().getMaxSyncUseTime());
            this.cdcAckMetrics.setTotalUseTime(temp.getCdcAckMetrics().getTotalUseTime());
        }
    }

    public void resetStatus(CDCStatus cdcStatus) {
        this.cdcAckMetrics.setCdcConsumerStatus(cdcStatus);
    }
}
