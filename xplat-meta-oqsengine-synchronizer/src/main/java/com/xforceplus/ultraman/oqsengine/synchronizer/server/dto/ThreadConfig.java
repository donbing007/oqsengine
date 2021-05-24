package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;

/**
 * thread config.
 */
public class ThreadConfig {

    private long heartbeatTimeoutInMilli;

    private long lastMemoInSecond;

    public long getHeartbeatTimeoutInMilli() {
        return heartbeatTimeoutInMilli;
    }

    public void setHeartbeatTimeoutInMilli(long heartbeatTimeoutInMilli) {
        this.heartbeatTimeoutInMilli = heartbeatTimeoutInMilli;
    }

    public long getLastMemoInSecond() {
        return lastMemoInSecond;
    }

    public void setLastMemoInSecond(long lastMemoInSecond) {
        this.lastMemoInSecond = lastMemoInSecond;
    }
}
