package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.task.AppCheckTask;
import com.xforceplus.ultraman.oqsengine.meta.task.KeepAliveTask;
import com.xforceplus.ultraman.oqsengine.meta.task.TimeoutCheckTask;
import io.grpc.stub.StreamObserver;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.clientTaskSize;

/**
 * desc :
 * name : RequestWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class RequestWatchExecutor implements IRequestWatchExecutor, IWatchExecutor {

    private RequestWatcher requestWatcher;

    private List<Thread> executors = new ArrayList<>(clientTaskSize);

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    @Override
    public void resetHeartBeat() {
        requestWatcher.resetHeartBeat();
    }

    @Override
    public void create(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        if (null == requestWatcher) {
            requestWatcher = new RequestWatcher(uid, observer);
            start();
        } else {
            requestWatcher.reset(uid, observer);
        }
    }

    @Override
    public void add(WatchElement watchElement) {
        if (null != requestWatcher) {
            requestWatcher.addWatch(watchElement);
        }
    }

    @Override
    public boolean update(WatchElement watchElement) {
        if (requestWatcher.onWatch(watchElement)) {
            WatchElement we = requestWatcher.watches().get(watchElement.getAppId());
            if (null != we) {
                we.setStatus(watchElement.getStatus());
                we.setVersion(watchElement.getVersion());

                return true;
            }
        }
        return false;
    }


    @Override
    public void release() {
        if (null != requestWatcher && requestWatcher.isOnServe()) {
            requestWatcher.release();
        }
    }

    @Override
    public RequestWatcher watcher() {
        return requestWatcher;
    }

    @Override
    public boolean canAccess(String uid) {
        /**
         * 判断是否可用
         */
        boolean status = (null != requestWatcher && requestWatcher.isOnServe());
        if (null != uid && status) {
            try {
                return uid.equals(requestWatcher.uid());
            } catch (Exception e) {
                /**
                 * 兜底瞬间将isReleased置为true同时uid = null的逻辑.
                 */
                return false;
            }
        }
        return status;
    }

    @Override
    public void start() {
        /**
         * 启动TimeoutCheck线程
         */
        executors.add(ThreadUtils.create(() -> new TimeoutCheckTask(requestWatcher,
                        gRpcParamsConfig.getDefaultHeartbeatTimeout(), gRpcParamsConfig.getMonitorSleepDuration())));

        /**
         * 启动keepAlive线程, 1秒check1次
         */
        executors.add(ThreadUtils.create(() -> new KeepAliveTask(requestWatcher,
                        gRpcParamsConfig.getKeepAliveSendDuration())));

        /**
         * 启动AppCheck线程
         */
        executors.add(ThreadUtils.create(() -> new AppCheckTask(requestWatcher,
                        gRpcParamsConfig.getMonitorSleepDuration(), gRpcParamsConfig.getDefaultDelayTaskDuration(), canAccessFunction())));
    }

    @Override
    public void stop() {
        if (null != requestWatcher) {
            requestWatcher.notServer();

            requestWatcher.release();

            executors.forEach(s -> {
                ThreadUtils.shutdown(s, gRpcParamsConfig.getMonitorSleepDuration());
            });
        }
    }

    public Function<String, Boolean> canAccessFunction() {
        return this::canAccess;
    }
}
