package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient.isShutdown;

/**
 * desc :
 * name : TimeoutCheckTask
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class TimeoutCheckTask implements Runnable {

    final Logger logger = LoggerFactory.getLogger(TimeoutCheckTask.class);

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
        logger.info("start timeout task ok...");
        while (!isShutdown) {
            if (requestWatcher.isOnServe()) {
                if (System.currentTimeMillis() - requestWatcher.heartBeat() > heartbeatTimeout) {
                    try {
                        requestWatcher.observer().onCompleted();
                        logger.warn("last heartbeat time [{}] reaches max timeout [{}]"
                                , System.currentTimeMillis() - requestWatcher.heartBeat(), heartbeatTimeout);
                    } catch (Exception e) {
                        //ignore
                    }

                }
            }
            TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
        }
    }
}
