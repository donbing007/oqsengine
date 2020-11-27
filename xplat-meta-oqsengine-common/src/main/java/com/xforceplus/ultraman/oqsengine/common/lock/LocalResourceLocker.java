package com.xforceplus.ultraman.oqsengine.common.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 只能用于同一JVM进程的KEY资源锁.支持重入.
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:10
 * @since 1.5
 */
public class LocalResourceLocker extends AbstractResourceLocker {

    private final ConcurrentMap<String, LockInfo> lockPool = new ConcurrentHashMap();

    /**
     * 返回指定资源被锁定重入了次数.
     *
     * @param key 资源.
     * @return 次数.
     */
    @Override
    protected int doLockNumber(String key) {
        LockInfo info = lockPool.get(key);
        if (info != null) {
            return info.getNumber();
        } else {
            return 0;
        }
    }

    @Override
    protected boolean doLock(String key, String lockingId) {
        LockInfo newLockInfo = new LockInfo(lockingId);
        LockInfo oldLockInfo = lockPool.putIfAbsent(key, newLockInfo);
        if (oldLockInfo == null) {
            return true;
        } else {
            if (oldLockInfo.getLockingId().equals(lockingId)) {
                oldLockInfo.incr();
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    protected int doUnLock(String key) {
        LockInfo lockInfo = lockPool.get(key);
        if (lockInfo != null) {
            int remaining = lockInfo.dec();
            if (remaining == 0) {
                lockPool.remove(key);
                return 0;
            } else {
                return remaining;
            }
        }

        return -1;
    }

    @Override
    protected String isLocked(String key) {
        return lockPool.get(key).getLockingId();
    }

    private static class LockInfo {

        private final String lockingId;
        private final AtomicInteger number;

        public LockInfo(String lockingId) {
            this.lockingId = lockingId;
            number = new AtomicInteger(1);
        }

        public String getLockingId() {
            return lockingId;
        }

        public int incr() {
            return number.incrementAndGet();
        }

        public int dec() {
            return number.decrementAndGet();
        }

        public int getNumber() {
            return number.intValue();
        }

    }
}

