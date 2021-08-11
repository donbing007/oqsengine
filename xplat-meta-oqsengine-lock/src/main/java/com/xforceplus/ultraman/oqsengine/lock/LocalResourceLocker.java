package com.xforceplus.ultraman.oqsengine.lock;

import java.util.Optional;
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

    @Override
    protected boolean doLock(String key, LockInfo newLockInfo) {
        LockInfo oldLockInfo = lockPool.putIfAbsent(key, newLockInfo);
        if (oldLockInfo == null) {
            return true;
        } else {
            if (oldLockInfo.getLockingId().equals(newLockInfo.getLockingId())) {
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
    protected Optional<LockInfo> isLocked(String key) {
        return Optional.ofNullable(lockPool.get(key));
    }

}

