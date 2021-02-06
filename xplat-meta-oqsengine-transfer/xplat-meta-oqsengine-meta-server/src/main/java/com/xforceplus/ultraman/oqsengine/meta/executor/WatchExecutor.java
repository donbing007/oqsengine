package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.GRpcConstant.monitorSleepDuration;

/**
 * desc :
 * name : WatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class WatchExecutor implements IWatchExecutor<EntityClassSyncResponse, Integer> {
    /**
     * 记录appId中的watcher的UID
     */
    private static Map<String, Set<String>> watchersByApp = new ConcurrentHashMap<>();

    /**
     * 记录UID与Watcher的映射关系
     */
    private static Map<String, IWatcher<EntityClassSyncResponse, Integer>> watchers = new ConcurrentHashMap<>();

    private long heartbeatTimeout;

    private Thread thread;

    /**
     * 启动一个监控线程、当watchers中observer超过阈值未响应后，将调用complete进行关闭并释放
     */
    public WatchExecutor(long heartbeatTimeout) {

        this.heartbeatTimeout = heartbeatTimeout;
    }

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
                                    (heartbeatTimeout - monitorSleepDuration)) {
                                remove(k);
                            }
                        }
                );
                /**
                 * 等待一秒进入下一次循环
                 */
                TimeWaitUtils.wakeupAfter(monitorSleepDuration, TimeUnit.MILLISECONDS);
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        watchers.forEach((k, v) -> {v.remove();});
        ThreadUtils.shutdown(thread);
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
     * @param appId
     * @param version
     * @param uid
     */
    @Override
    public boolean update(String appId, int version, String uid) {
        IWatcher<EntityClassSyncResponse, Integer> watcher = watchers.get(uid);
        if (null != watcher) {
            if (watcher.watches().get(appId) < version) {
                watcher.addWatch(appId, version);
            }
            return true;
        }
        return false;
    }

    /**
     * 当注册时，初始化observer的映射关系
     *
     * @param appId
     * @param version
     * @param uid
     * @param observer
     */
    @Override
    public synchronized void add(String appId, int version, String uid, StreamObserver<EntityClassSyncResponse> observer) {
        IWatcher<EntityClassSyncResponse, Integer> watcher = watchers.get(uid);
        if (null == watcher) {
            watcher = new ResponseWatcher(uid, observer);
        }

        watcher.addWatch(appId, version);

        watchers.put(uid, watcher);
        watchersByApp.computeIfAbsent(appId, k -> new HashSet<>()).add(uid);
    }

    /**
     * 当发生observer断流时，将watcher移除
     *
     * @param uid
     */
    @Override
    public void remove(String uid) {
        IWatcher<EntityClassSyncResponse, Integer> watcher = watchers.remove(uid);

        if (null != watcher && !watcher.isRemoved()) {
            watcher.remove(() -> {
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
     *
     * @param appId
     * @param version
     */
    @Override
    public List<IWatcher<EntityClassSyncResponse, Integer>> need(String appId, int version) {
        Set<String> res = watchersByApp.get(appId);
        List<IWatcher<EntityClassSyncResponse, Integer>> needList = new ArrayList<>();
        if (null != res) {
            res.forEach(
                    r -> {
                        IWatcher<EntityClassSyncResponse, Integer> watcher = watchers.get(r);
                        if (null != watcher && watcher.onWatch(appId, version)) {
                            needList.add(watcher);
                        }
                    }
            );
        }
        return needList;
    }


    @Override
    public Optional<IWatcher<EntityClassSyncResponse, Integer>> watcher(String uid) {
        return Optional.of(watchers.get(uid));
    }

}
