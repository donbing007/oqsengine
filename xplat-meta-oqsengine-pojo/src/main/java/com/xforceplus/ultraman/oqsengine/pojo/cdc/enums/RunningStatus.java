package com.xforceplus.ultraman.oqsengine.pojo.cdc.enums;

/**
 * desc :.
 * name : RunningStatus
 *
 * @author : xujia 2020/11/9
 * @since : 1.8
 */
public enum RunningStatus {
    RUN,
    TRY_STOP,
    STOP_SUCCESS;

    RunningStatus() {
    }

    public boolean shouldStop() {
        return this.ordinal() >= TRY_STOP.ordinal();
    }

    public boolean isStop() {
        return this.ordinal() >= STOP_SUCCESS.ordinal();
    }
}
