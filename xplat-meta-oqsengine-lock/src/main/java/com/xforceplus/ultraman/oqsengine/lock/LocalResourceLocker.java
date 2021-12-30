package com.xforceplus.ultraman.oqsengine.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    protected boolean doLock(Locker locker, String key) {
        return doLocks(locker, key);
    }

    @Override
    protected boolean doLocks(Locker locker, String... keys) {

        List<String> hasLockedKeys = new ArrayList<>(keys.length);

        String key;
        LockInfo newLockInfo;
        boolean ok = true;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            newLockInfo = new LockInfo(locker);

            LockInfo oldLockInfo = lockPool.putIfAbsent(key, newLockInfo);
            if (oldLockInfo != null) {

                // 这里保证同一个LockInfo不会被两个线程共享.
                if (!oldLockInfo.getLocker().equals(locker.getName())) {

                    ok = false;
                    break;

                } else {

                    oldLockInfo.incr();

                }
            }

            hasLockedKeys.add(key);
        }

        if (!ok) {
            doUnLocks(locker, hasLockedKeys.toArray(new String[0]));
        }

        return ok;
    }

    @Override
    protected boolean doUnLock(Locker locker, String key) {
        return doUnLocks(locker, key);
    }

    @Override
    protected boolean doUnLocks(Locker locker, String... keys) {
        for (String key : keys) {
            LockInfo lockInfo = lockPool.get(key);
            if (lockInfo != null) {
                if (!lockInfo.getLocker().equals(locker.getName())) {
                    return false;
                }
            }
        }

        for (String key : keys) {
            LockInfo lockInfo = lockPool.get(key);
            if (lockInfo != null) {
                if (lockInfo.decr() == 0) {
                    lockPool.remove(key);
                }
            }
        }

        return true;
    }

    @Override
    protected boolean doIsLocking(String key) {
        return lockPool.containsKey(key);
    }

    private static class LockInfo {
        private String locker;
        private int number;

        public LockInfo(Locker locker) {
            this.locker = locker.getName();
            this.number = 1;
        }

        public String getLocker() {
            return locker;
        }

        public int getNumber() {
            return number;
        }

        public int incr() {
            return ++number;
        }

        public int decr() {
            number--;
            if (number < 0) {
                number = 0;
            }

            return number;
        }
    }

}

