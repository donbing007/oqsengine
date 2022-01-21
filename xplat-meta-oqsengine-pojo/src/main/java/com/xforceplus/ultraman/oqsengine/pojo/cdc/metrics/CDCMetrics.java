package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;


import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.devops.DevOpsCdcMetrics;
import java.util.HashMap;
import java.util.Map;

/**
 * desc :.
 * name : CDCMetrics
 *
 * @author : xujia 2020/11/7
 * @since : 1.8
 */
public class CDCMetrics {
    private long batchId;

    private CDCAckMetrics cdcAckMetrics;
    private CDCUnCommitMetrics cdcUnCommitMetrics;
    private Map<Long, DevOpsCdcMetrics> devOpsMetrics;

    public CDCMetrics() {
        this(EMPTY_BATCH_ID, new CDCAckMetrics(CDCStatus.CONNECTED), new CDCUnCommitMetrics());
        this.cdcAckMetrics.setLastConnectedTime(System.currentTimeMillis());
    }

    /**
     * 实例化.
     *
     * @param batchId            批次id.
     * @param cdcAckMetrics      指标.
     * @param cdcUnCommitMetrics 未同步提交号指标.
     */
    public CDCMetrics(long batchId, CDCAckMetrics cdcAckMetrics, CDCUnCommitMetrics cdcUnCommitMetrics) {
        this.batchId = batchId;
        this.cdcAckMetrics = cdcAckMetrics;
        this.cdcUnCommitMetrics = cdcUnCommitMetrics;
        this.devOpsMetrics = new HashMap<>();
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

    public void resetStatus() {
        this.cdcAckMetrics.setLastConnectedTime(System.currentTimeMillis());
        this.cdcAckMetrics.setCdcConsumerStatus(CDCStatus.CONNECTED);
    }

    public void setCdcUnCommitMetrics(CDCUnCommitMetrics cdcUnCommitMetrics) {
        this.cdcUnCommitMetrics = cdcUnCommitMetrics;
    }

    public void syncFreeMessage(long batchId) {
        this.batchId = batchId;
        this.cdcAckMetrics.setExecuteRows(ZERO);
    }

    public Map<Long, DevOpsCdcMetrics> getDevOpsMetrics() {
        return devOpsMetrics;
    }

    /**
     * 成功.
     */
    public void consumeSuccess(long originBatchId, CDCMetrics temp, boolean isConnectSync) {
        this.batchId = originBatchId;

        this.cdcAckMetrics.setCdcConsumerStatus(CDCStatus.CONNECTED);
        //  启动则更新LastConnectedTime, 否则为成功消费
        if (!isConnectSync) {
            this.cdcAckMetrics.setLastConsumerTime(System.currentTimeMillis());
        } else {
            this.cdcAckMetrics.setLastConnectedTime(System.currentTimeMillis());
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
}
