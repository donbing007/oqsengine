package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
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
    private static Map<String, Integer> appVersions = new HashMap<>();

    /**
     * 记录app + env的UIDs
     */
    private static Map<String, Set<String>> appWatchers = new HashMap<>();

    /**
     * 记录UID与Watcher的映射关系
     */
    private static Map<String, ResponseWatcher> uidWatchers = new ConcurrentHashMap<>();



    @Override
    public void start() {
        logger.debug("responseWatchExecutor start.");
    }

    @Override
    public void stop() {
        uidWatchers.forEach(
                (k, v) -> {
                    if (v.isActive()) {
                        v.release();
                    }
                });
        logger.debug("responseWatchExecutor stop.");
    }

    @Override
    public void keepAliveCheck(long heartbeatTimeout) {
        long current = System.currentTimeMillis();
        uidWatchers.forEach(
                (k, v) -> {
                    /**
                     * 当最后获得的心跳时间与当前时间差值大于可容忍值时，对该watcher进行释放清理
                     * 可容忍值 = 最大超时时间(heartbeatTimeout) - 1000ms(monitorSleepDuration);
                     */
                    if (current - v.heartBeat() >= heartbeatTimeout) {
                        release(k);
                        logger.warn("heart-beat check error, watcher has been released,  uid [{}]", k);
                    }
                }
        );
    }

    @Override
    public Integer version(String appId, String env) {
        return appVersions.get(keyAppWithEnv(appId, env));
    }

    @Override
    public boolean addVersion(String appId, String env, int version) {
        return addVersionWithLock(keyAppWithEnv(appId, env), version);
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
        uidWatchers.computeIfAbsent(uid, v -> new ResponseWatcher(uid, observer)).addWatch(watchElement);
        operationWithLock(keyAppWithEnv(watchElement.getAppId(), watchElement.getEnv()), uid, NEW);
    }

    @Override
    public void resetHeartBeat(String uid) {
        ResponseWatcher w = uidWatchers.get(uid);
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
        ResponseWatcher watcher = uidWatchers.get(uid);
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
        ResponseWatcher watcher = uidWatchers.remove(uid);

        if (null != watcher && watcher.isActive()) {

            watcher.inActive();

            watcher.release(() -> {
                watcher.watches().forEach(
                        (k, v) -> {
                            operationWithLock(keyAppWithEnv(k, v.getEnv()), uid, RELEASE);
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
        Set<String> res = appWatchers.get(keyAppWithEnv(watchElement.getAppId(), watchElement.getEnv()));
        List<ResponseWatcher> needList = new ArrayList<>();
        if (null != res) {
            res.forEach(
                    r -> {
                        ResponseWatcher watcher = uidWatchers.get(r);
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
        return uidWatchers.get(uid);
    }

    @Override
    public Set<String> appWatchers(String appId, String env) {
        return appWatchers.get(keyAppWithEnv(appId, env));
    }

    private boolean canUpdate(ResponseWatcher watcher, WatchElement watchElement) {
        WatchElement current =
                watcher.watches().get(watchElement.getAppId());

        return null != current && (current.getVersion() < watchElement.getVersion() ||
                                        current.getStatus().ordinal() < watchElement.getStatus().ordinal());
    }

    private synchronized boolean addVersionWithLock(String key, int version ) {
        Integer v = appVersions.get(key);
        if (null == v || v < version) {
            appVersions.put(key, version);
            return true;
        }
        return false;
    }

    private synchronized void operationWithLock(String key, String value, Operation operation) {
        logger.debug("appWatcher [{}], key [{}], value [{}]", operation, key, value);
        switch (operation) {
            case NEW:
                appWatchers.computeIfAbsent(key, k -> new HashSet<>()).add(value);
                break;
            case RELEASE:
                Set<String> v = appWatchers.get(key);
                if (null != v) {
                    if (!v.isEmpty()) {
                        v.remove(value);
                    }

                    if(v.isEmpty()) {
                        appWatchers.remove(key);
                    }

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
