package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;

import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.monitorSleepDuration;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.HEARTBEAT;

/**
 * desc :
 * name : KeepAliveTask
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class KeepAliveTask implements Runnable {

    private RequestWatcher requestWatcher;

    public KeepAliveTask(RequestWatcher requestWatcher) {
        this.requestWatcher = requestWatcher;
    }

    @Override
    public void run() {
        while (true) {
            if (!requestWatcher.isReleased()) {
                EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                requestWatcher.observer().onNext(request);
            }
            TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
        }
    }
}
