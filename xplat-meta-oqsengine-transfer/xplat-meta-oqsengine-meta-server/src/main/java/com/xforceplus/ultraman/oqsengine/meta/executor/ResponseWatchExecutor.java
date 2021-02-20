package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;

/**
 * desc :
 * name : ResponseWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatchExecutor implements IResponseWatchExecutor, IWatchExecutor {

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    /**
     * 记录appId中的watcher的UID
     */
    private static Map<String, Set<String>> watchersByApp = new ConcurrentHashMap<>();

    /**
     * 记录UID与Watcher的映射关系
     */
    private static Map<String, IWatcher<EntityClassSyncResponse>> watchers = new ConcurrentHashMap<>();

    private long heartbeatTimeout;

    private Thread thread;

    /**
     * 启动一个监控线程、当watchers中observer超过阈值未响应后，将调用complete进行关闭并释放
     */
    public ResponseWatchExecutor(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public void start() {
        thread = ThreadUtils.create(() -> {
            while (true) {
                long current = System.currentTimeMillis();
                watchers.forEach(
                        (k, v) -> {
                            /**
                             * 当最后获得的心跳时间与当前时间差值大于可容忍值时，对该watcher进行释放清理
                             * 可容忍值 = 最大超时时间(heartbeatTimeout) - 1000ms(monitorSleepDuration);
                             */
                            if (current - v.heartBeat() >=
                                    (heartbeatTimeout - gRpcParamsConfig.getMonitorSleepDuration())) {
                                release(k);
                            }
                        }
                );
                /**
                 * 等待一秒进入下一次循环
                 */
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        watchers.forEach(
                (k, v) -> {
                        if (v.isOnServe()) {
                            v.release();
                        }
                });
        ThreadUtils.shutdown(thread, SHUT_DOWN_WAIT_TIME_OUT);
    }


    /**
     * 当注册时，初始化observer的映射关系
     *
     * @param uid
     * @param observer
     * @param watchElement
     */
    @Override
    public void add(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement) {
        IWatcher<EntityClassSyncResponse> watcher = watchers.get(uid);
        if (null == watcher) {
            watcher = new ResponseWatcher(uid, observer);
        }

        watcher.addWatch(watchElement);

        watchers.put(uid, watcher);
        watchersByApp.computeIfAbsent(watchElement.getAppId(), k -> new HashSet<>()).add(uid);
    }

    @Override
    public void heartBeat(String uid) {
        IWatcher w = watchers.get(uid);
        if (null != w) {
            w.resetHeartBeat();
        }
    }

    /**
     * 更新版本
     * @param uid
     * @param watchElement
     */
    @Override
    public boolean update(String uid, WatchElement watchElement) {
        IWatcher<EntityClassSyncResponse> watcher = watchers.get(uid);
        if (null != watcher) {
            WatchElement we = watcher.watches().get(watchElement.getAppId());
            if (null == we) {
                watcher.watches().put(watchElement.getAppId(), watchElement);
            } else {
                we.setStatus(watchElement.getStatus());
                we.setVersion(watchElement.getVersion());
            }
            return true;
        }
        return false;
    }

    /**
     * 当发生observer断流时，将watcher移除
     *
     * @param uid
     */
    @Override
    public void release(String uid) {
        IWatcher<EntityClassSyncResponse> watcher = watchers.remove(uid);

        if (null != watcher && watcher.isOnServe()) {
            watcher.release(() -> {
                watcher.watches().forEach(
                        (k, v) -> {
                            Set<String> uidSet = watchersByApp.get(k);
                            if (null != uidSet && uidSet.size() > 0) {
                                uidSet.remove(uid);
                            }
                        }
                );
                return true;
            });
        }
    }

    /**
     * 当发生observer断流时，将watcher移除
     * @param watchElement
     */
    @Override
    public List<IWatcher<EntityClassSyncResponse>> need(WatchElement watchElement) {
        Set<String> res = watchersByApp.get(watchElement.getAppId());
        List<IWatcher<EntityClassSyncResponse>> needList = new ArrayList<>();
        if (null != res) {
            res.forEach(
                    r -> {
                        IWatcher<EntityClassSyncResponse> watcher = watchers.get(r);
                        if (null != watcher && watcher.onWatch(watchElement)) {
                            needList.add(watcher);
                        }
                    }
            );
        }
        return needList;
    }

    /**
     * 返回watcher
     * @param uid
     */
    @Override
    public Optional<IWatcher<EntityClassSyncResponse>> watcher(String uid) {
        return Optional.of(watchers.get(uid));
    }
}
