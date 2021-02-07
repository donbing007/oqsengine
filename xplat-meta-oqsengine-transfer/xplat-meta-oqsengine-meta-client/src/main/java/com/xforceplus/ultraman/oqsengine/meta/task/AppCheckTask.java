package com.xforceplus.ultraman.oqsengine.meta.task;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.monitorSleepDuration;
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

    private RequestWatcher requestWatcher;
    private Function<String, Boolean> canAccessFunction;

    public AppCheckTask(RequestWatcher requestWatcher, Function<String, Boolean> canAccessFunction) {
        this.requestWatcher = requestWatcher;
        this.canAccessFunction = canAccessFunction;
    }

    @Override
    public void run() {
        while (true) {
            if (!requestWatcher.isReleased()) {
                requestWatcher.watches().values().stream().filter(s -> {
                    return s.getStatus().ordinal() == WatchElement.AppStatus.Init.ordinal();
                }).forEach(
                        k -> {
                            EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                            EntityClassSyncRequest entityClassSyncRequest =
                                    builder.setUid(requestWatcher.uid()).setAppId(k.getAppId()).setVersion(k.getVersion())
                                            .setStatus(RequestStatus.REGISTER.ordinal()).build();

                            sendRequest(requestWatcher, entityClassSyncRequest, canAccessFunction, requestWatcher.uid());
                        }
                );
            }
            TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
        }
    }
}
