package com.xforceplus.ultraman.oqsengine.cdc.context;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class RunnerContext {

    private CDCMetrics cdcMetrics;

    private int continuesConnectFails;

    private volatile RunningStatus runningStatus;

    public RunnerContext() {
        cdcMetrics = new CDCMetrics();
        continuesConnectFails = 0;
        runningStatus = RunningStatus.RUN;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    public void setCdcMetrics(CDCMetrics cdcMetrics) {
        this.cdcMetrics = cdcMetrics;
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
