package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient.isShutdown;
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

    final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);

    private RequestWatcher requestWatcher;
    private long keepAliveSendDuration;

    public KeepAliveTask(RequestWatcher requestWatcher, long keepAliveSendDuration) {
        this.requestWatcher = requestWatcher;
        this.keepAliveSendDuration = keepAliveSendDuration;
    }

    @Override
    public void run() {
        logger.info("start keepAlive task ok...");
        while (!isShutdown) {
            if (requestWatcher.isOnServe()) {
                EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                try {
                    sendRequest(requestWatcher, request);
                    logger.debug("send keepAlive ok...");
                } catch (Exception e) {
                    //  ignore
                    logger.warn("send keepAlive failed, but exception will ignore due to retry...");
                }

            }
            TimeWaitUtils.wakeupAfter(keepAliveSendDuration, TimeUnit.MILLISECONDS);
        }
    }
}
