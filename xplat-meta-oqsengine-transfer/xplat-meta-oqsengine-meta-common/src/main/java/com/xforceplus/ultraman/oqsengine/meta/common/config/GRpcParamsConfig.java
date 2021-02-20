package com.xforceplus.ultraman.oqsengine.meta.common.config;

/**
 * desc :
 * name : GRpcClientConfig
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class GRpcParamsConfig {

    public static final long SHUT_DOWN_WAIT_TIME_OUT = 10;

    public long defaultHeartbeatTimeout;
    public long defaultDelayTaskDuration;

    public long monitorSleepDuration;
    public long reconnectDuration;

    public long keepAliveSendDuration;

    public long getDefaultHeartbeatTimeout() {
        return defaultHeartbeatTimeout;
    }

    public void setDefaultHeartbeatTimeout(long defaultHeartbeatTimeout) {
        this.defaultHeartbeatTimeout = defaultHeartbeatTimeout;
    }

    public long getDefaultDelayTaskDuration() {
        return defaultDelayTaskDuration;
    }

    public void setDefaultDelayTaskDuration(long defaultDelayTaskDuration) {
        this.defaultDelayTaskDuration = defaultDelayTaskDuration;
    }

    public long getMonitorSleepDuration() {
        return monitorSleepDuration;
    }

    public void setMonitorSleepDuration(long monitorSleepDuration) {
        this.monitorSleepDuration = monitorSleepDuration;
    }

    public long getReconnectDuration() {
        return reconnectDuration;
    }

    public void setReconnectDuration(long reconnectDuration) {
        this.reconnectDuration = reconnectDuration;
    }

    public long getKeepAliveSendDuration() {
        return keepAliveSendDuration;
    }

    public void setKeepAliveSendDuration(long keepAliveSendDuration) {
        this.keepAliveSendDuration = keepAliveSendDuration;
    }
}
