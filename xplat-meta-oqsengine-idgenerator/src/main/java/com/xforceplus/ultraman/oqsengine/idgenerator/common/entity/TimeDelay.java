package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

import java.util.concurrent.TimeUnit;

/**
 * .
 *
 * @author leo
 * @version 0.1 2021/12/8 7:38 下午
 * @since 1.8
 */
public class TimeDelay {
    private int delay;
    private TimeUnit timeUnit;

    public TimeDelay(int delay, TimeUnit timeUnit) {
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
