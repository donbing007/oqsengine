package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor.Operation.NEW;
import static com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor.Operation.RELEASE;

/**
 * desc :
 * name : ResponseWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatchExecutor implements IResponseWatchExecutor {

    private Logger logger = LoggerFactory.getLogger(ResponseWatchExecutor.class);

    /**
     * 记录app + env的version
     */
    private static Map<String, Integer> appVersionMap = new ConcurrentHashMap<>();

    /**
     * 记录app + env的UIDs
     */
    private static Map<String, Set<String>> watchersByApp = new ConcurrentHashMap<>();

    /**
     * 记录UID与Watcher的映射关系
     */
    private static Map<String, ResponseWatcher> watchers = new ConcurrentHashMap<>();



    @Override
    public void start() {
        logger.info("responseWatchExecutor start.");
    }

    @Override
    public void stop() {
        watchers.forEach(
                (k, v) -> {
                    if (v.isOnServe()) {
                        v.release();
                    }
                });
        logger.info("responseWatchExecutor stop.");
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

    @Override
    public Integer version(String appId, String env) {
        return appVersionMap.get(keyAppWithEnv(appId, env));
    }

    @Override
    public boolean addVersion(String appId, String env, int version) {
        String key = keyAppWithEnv(appId, env);
        synchronized (ResponseWatchExecutor.class) {
            Integer v = appVersionMap.get(key);
            if (null == v || v < version) {
                appVersionMap.put(key, v);
                return true;
            }
        }
        return false;
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
        addWatch(uid, observer, watchElement);
        operationWithLock(keyAppWithEnv(watchElement.getAppId(), watchElement.getEnv()), uid, NEW);
    }

    @Override
    public void resetHeartBeat(String uid) {
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
            if (canUpdate(watcher, watchElement)) {
                watcher.watches().put(watchElement.getAppId(), watchElement);
                return true;
            }
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

        if (null != watcher) {
            watcher.release(() -> {
                watcher.watches().forEach(
                        (k, v) -> {
                            Set<String> uidSet = watchersByApp.get(keyAppWithEnv(k, v.getEnv()));
                            if (null != uidSet && uidSet.size() > 0) {
                                uidSet.remove(uid);
                                if (uidSet.size() == 0) {
                                    operationWithLock(k, uid, RELEASE);
                                }
                            }

                        }
                );
                return true;
            });
        }
    }

    /**
     * 获取关注列表
     *
     * @param watchElement
     */
    @Override
    public List<ResponseWatcher> need(WatchElement watchElement) {
        Set<String> res = watchersByApp.get(keyAppWithEnv(watchElement.getAppId(), watchElement.getEnv()));
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

    @Override
    public Set<String> appWatchers(String appId, String env) {
        return watchersByApp.get(keyAppWithEnv(appId, env));
    }

    private boolean canUpdate(ResponseWatcher watcher, WatchElement watchElement) {
        WatchElement current =
                watcher.watches().get(watchElement.getAppId());

        return null != current && (current.getVersion() < watchElement.getVersion() ||
                                        current.getStatus().ordinal() < watchElement.getStatus().ordinal());
    }

    private synchronized void addWatch(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement) {
        ResponseWatcher watcher = watchers.get(uid);
        if (null == watcher) {
            watcher = new ResponseWatcher(uid, observer);
        }

        watcher.addWatch(watchElement);

        watchers.put(uid, watcher);
    }

    private synchronized void operationWithLock(String key, String value, Operation operation) {
        switch (operation) {
            case NEW:
                watchersByApp.computeIfAbsent(key, k -> new HashSet<>()).add(value);
                break;
            case RELEASE:
                Set<String> v = watchersByApp.get(key);
                if (null != v && v.isEmpty()) {
                    watchersByApp.remove(key);
                }
                break;
        }
    }

    public enum Operation {
        NEW,
        RELEASE
    }

    public static String keyAppWithEnv(String appId, String env) {
        return appId + "_" + env;
    }
}
