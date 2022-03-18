package com.xforceplus.ultraman.oqsengine.cdc.context;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class RunnerContext {

    private long totalExecutedRecords;

    private CDCMetrics cdcMetrics;

    private int continuesConnectFails;

    private volatile RunningStatus runningStatus;

    /**
     * 构造实例.
     */
    public RunnerContext() {
        cdcMetrics = new CDCMetrics();
        continuesConnectFails = 0;
        runningStatus = RunningStatus.RUN;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    /**
     * 设置CDC指标.
     */
    public void setCdcMetrics(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;

        this.totalExecutedRecords += cdcMetrics.getCdcAckMetrics().getExecuteRows();
    }

    public long totalExecutedRecords() {
        return totalExecutedRecords;
    }

    public int getContinuesConnectFails() {
        return continuesConnectFails;
    }

    public void resetContinuesConnectFails() {
        this.continuesConnectFails = 0;
    }

    public void incrementContinuesConnectFails() {
        this.continuesConnectFails++;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(RunningStatus runningStatus) {
        this.runningStatus = runningStatus;
    }
}
