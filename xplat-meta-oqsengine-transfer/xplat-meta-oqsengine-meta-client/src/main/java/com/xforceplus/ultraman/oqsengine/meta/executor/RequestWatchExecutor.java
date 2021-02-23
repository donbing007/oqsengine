package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.CLIENT_TASK_COUNT;

/**
 * desc :
 * name : RequestWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class RequestWatchExecutor implements IRequestWatchExecutor {

    private RequestWatcher requestWatcher;

    private Queue<WatchElement> forgotQueue = new ConcurrentLinkedDeque<>();

    private List<Thread> executors = new ArrayList<>(CLIENT_TASK_COUNT);

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    @Override
    public void resetHeartBeat() {
        requestWatcher.resetHeartBeat();
    }

    @Override
    public void create(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        if (null == requestWatcher) {
            init(uid, observer);
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
            } else {
                requestWatcher.addWatch(watchElement);
            }
            return true;
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
    public void addForgot(String appId, int version) {
        forgotQueue.add(new WatchElement(appId, version, WatchElement.AppStatus.Init));
    }

    @Override
    public void start() {
        int pos = 0;
        /**
         * 启动TimeoutCheck线程
         */
        TimeoutCheckTask timeoutCheckTask = new TimeoutCheckTask(requestWatcher,
                gRpcParamsConfig.getDefaultHeartbeatTimeout(), gRpcParamsConfig.getMonitorSleepDuration());
        executors.add(new Thread(timeoutCheckTask));
        executors.get(pos++).start();

        /**
         * 启动keepAlive线程, 1秒check1次
         */
        KeepAliveTask keepAliveTask = new KeepAliveTask(requestWatcher,
                gRpcParamsConfig.getKeepAliveSendDuration());
        executors.add(new Thread(keepAliveTask));
        executors.get(pos++).start();

        /**
         * 启动AppCheck线程
         */
        AppCheckTask appCheckTask = new AppCheckTask(requestWatcher,
                forgotQueue, gRpcParamsConfig.getMonitorSleepDuration(), gRpcParamsConfig.getDefaultDelayTaskDuration(), accessFunction());
        executors.add(new Thread(appCheckTask));
        executors.get(pos).start();
    }

    @Override
    public void stop() {
        if (null != requestWatcher) {
            requestWatcher.notServer();

            requestWatcher.release();

            executors.forEach(s -> {
                ThreadUtils.shutdown(s, SHUT_DOWN_WAIT_TIME_OUT);
            });
        }
    }

    public Function<String, Boolean> accessFunction() {
        return this::canAccess;
    }

    private void init(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        requestWatcher = new RequestWatcher(uid, observer);
        this.start();
    }
}
