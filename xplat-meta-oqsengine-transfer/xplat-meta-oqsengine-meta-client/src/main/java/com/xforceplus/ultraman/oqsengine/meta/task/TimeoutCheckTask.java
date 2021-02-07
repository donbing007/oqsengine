package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;

import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.defaultHeartbeatTimeout;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.monitorSleepDuration;

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

    public TimeoutCheckTask(RequestWatcher requestWatcher) {
        this.requestWatcher = requestWatcher;
    }

    @Override
    public void run() {
        while (true) {
            if (!requestWatcher.isReleased()) {
                if (System.currentTimeMillis() - requestWatcher.heartBeat() > defaultHeartbeatTimeout) {
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
