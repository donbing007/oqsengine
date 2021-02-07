package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;

import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : TimeoutCheckTask
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class TimeoutCheckTask implements Runnable {

    private RequestWatcher requestWatcher;

    private long heartbeatTimeout;
    private long monitorSleepDuration;

    public TimeoutCheckTask(RequestWatcher requestWatcher, long heartbeatTimeout, long monitorSleepDuration) {
        this.requestWatcher = requestWatcher;
        this.heartbeatTimeout = heartbeatTimeout;
        this.monitorSleepDuration = monitorSleepDuration;
    }

    @Override
    public void run() {
        while (true) {
            if (!requestWatcher.isReleased()) {
                if (System.currentTimeMillis() - requestWatcher.heartBeat() > heartbeatTimeout) {
                    try {
                        requestWatcher.observer().onCompleted();
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
            TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
        }
    }
}
