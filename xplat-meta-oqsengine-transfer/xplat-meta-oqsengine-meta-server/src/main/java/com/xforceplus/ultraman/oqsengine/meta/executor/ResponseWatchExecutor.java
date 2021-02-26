package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc :
 * name : ResponseWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatchExecutor implements IResponseWatchExecutor {

    /**
     * 记录appId中的watcher的UID
     */
    private static Map<String, Set<String>> watchersByApp = new ConcurrentHashMap<>();

    /**
     * 记录UID与Watcher的映射关系
     */
    private static Map<String, ResponseWatcher> watchers = new ConcurrentHashMap<>();

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        watchers.forEach(
                (k, v) -> {
                    if (v.isOnServe()) {
                        v.release();
                    }
                });
    }

    public void keepAliceCheck(long heartbeatTimeout) {
        long current = System.currentTimeMillis();
        watchers.forEach(
                (k, v) -> {
                    /**
                     * 当最后获得的心跳时间与当前时间差值大于可容忍值时，对该watcher进行释放清理
                     * 可容忍值 = 最大超时时间(heartbeatTimeout) - 1000ms(monitorSleepDuration);
                     */
                    if (current - v.heartBeat() >= heartbeatTimeout) {
                        release(k);
                    }
                }
        );
    }


    /**
     * 当注册时，初始化observer的映射关系
     *
     * @param uid
     * @param observer
     * @param watchElement
     */
    @Override
    public synchronized void add(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement) {
        ResponseWatcher watcher = watchers.get(uid);
        if (null == watcher) {
            watcher = new ResponseWatcher(uid, observer);
        }

        watcher.addWatch(watchElement);

        watchers.put(uid, watcher);
        watchersByApp.computeIfAbsent(watchElement.getAppId(), k -> new HashSet<>()).add(uid);
    }

    @Override
    public void heartBeat(String uid) {
        ResponseWatcher w = watchers.get(uid);
        if (null != w) {
            w.resetHeartBeat();
        }
    }

    /**
     * 更新版本
     *
     * @param uid
     * @param watchElement
     */
    @Override
    public synchronized boolean update(String uid, WatchElement watchElement) {
        ResponseWatcher watcher = watchers.get(uid);
        if (null != watcher) {
            watcher.watches().put(watchElement.getAppId(), watchElement);
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
        ResponseWatcher watcher = watchers.remove(uid);

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
     *
     * @param watchElement
     */
    @Override
    public List<ResponseWatcher> need(WatchElement watchElement) {
        Set<String> res = watchersByApp.get(watchElement.getAppId());
        List<ResponseWatcher> needList = new ArrayList<>();
        if (null != res) {
            res.forEach(
                    r -> {
                        ResponseWatcher watcher = watchers.get(r);
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
     *
     * @param uid
     */
    @Override
    public ResponseWatcher watcher(String uid) {
        return watchers.get(uid);
    }
}
