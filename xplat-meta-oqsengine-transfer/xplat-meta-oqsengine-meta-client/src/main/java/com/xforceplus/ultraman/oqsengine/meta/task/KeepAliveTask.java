package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;

import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.HEARTBEAT;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;

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
    private long keepAliveSendDuration;

    public KeepAliveTask(RequestWatcher requestWatcher, long keepAliveSendDuration) {
        this.requestWatcher = requestWatcher;
        this.keepAliveSendDuration = keepAliveSendDuration;
    }

    @Override
    public void run() {
        while (true) {
            if (!requestWatcher.isReleased()) {
                EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                try {
                    sendRequest(requestWatcher, request);
                } catch (Exception e) {
                    //  ignore

                }
            }
            TimeWaitUtils.wakeupAfter(keepAliveSendDuration, TimeUnit.MILLISECONDS);
        }
    }
}
