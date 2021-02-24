package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient.isShutdown;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;

/**
 * desc :
 * name : AppCheckTask
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class AppCheckTask implements Runnable {

    final Logger logger = LoggerFactory.getLogger(AppCheckTask.class);

    private RequestWatcher requestWatcher;
    private long monitorSleepDuration;
    private long delayTaskDuration;
    private Queue<WatchElement> forgotQueue;
    private Function<String, Boolean> canAccessFunction;

    public AppCheckTask(RequestWatcher requestWatcher, Queue<WatchElement> forgotQueue, long monitorSleepDuration, long delayTaskDuration,
                                    Function<String, Boolean> canAccessFunction) {
        this.requestWatcher = requestWatcher;
        this.forgotQueue = forgotQueue;
        this.monitorSleepDuration = monitorSleepDuration;
        this.delayTaskDuration = delayTaskDuration;
        this.canAccessFunction = canAccessFunction;
    }

    @Override
    public void run() {
        logger.info("start appCheck task ok...");
        while (!isShutdown) {
            if (requestWatcher.isOnServe()) {
                /**
                 * 将forgetQueue中的数据添加到watch中
                 */
                int checkSize = forgotQueue.size();
                while (checkSize > 0) {
                    try {
                        WatchElement w = forgotQueue.remove();
                        if (!requestWatcher.watches().containsKey(w.getAppId())) {
                            requestWatcher.watches().put(w.getAppId(), w);
                        }
                    } catch (Exception e) {
                        //  ignore
                    }
                    checkSize--;
                }

                requestWatcher.watches().values().stream().filter(s -> {
                    return s.getStatus().ordinal() == WatchElement.AppStatus.Init.ordinal() ||
                            (s.getStatus().ordinal() < WatchElement.AppStatus.Confirmed.ordinal() &&
                                        System.currentTimeMillis() - s.getRegisterTime() > delayTaskDuration);
                }).forEach(
                        k -> {
                            EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                            EntityClassSyncRequest entityClassSyncRequest =
                                    builder.setUid(requestWatcher.uid()).setAppId(k.getAppId()).setVersion(k.getVersion())
                                            .setStatus(RequestStatus.REGISTER.ordinal()).build();

                            sendRequest(requestWatcher, entityClassSyncRequest, canAccessFunction, requestWatcher.uid());
                        }
                );

                logger.debug("app check ok...");
            }
            TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
        }
    }
}
