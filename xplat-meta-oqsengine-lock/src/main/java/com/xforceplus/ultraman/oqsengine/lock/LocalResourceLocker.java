package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.lock.utils.Locker;
import com.xforceplus.ultraman.oqsengine.lock.utils.StateKeys;
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
public class LocalResourceLocker extends AbstractRetryResourceLocker {

    private final ConcurrentMap<String, LockInfo> lockPool = new ConcurrentHashMap();

    @Override
    protected void doLocks(Locker locker, StateKeys stateKeys) {

        String key;
        String[] keys = stateKeys.getNoCompleteKeys();
        LockInfo newLockInfo;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            newLockInfo = new LockInfo(locker);

            LockInfo oldLockInfo = lockPool.putIfAbsent(key, newLockInfo);
            if (oldLockInfo != null) {

                if (!oldLockInfo.getLocker().equals(locker.getName())) {

                    // 已经加锁,但是不是当前加锁者加锁,不能重入.
                    return;

                } else {

                    // 重入
                    oldLockInfo.incr();
                    stateKeys.move();

                }
            } else {
                // 新建的锁
                stateKeys.move();
            }
        }
    }

    @Override
    protected int[] doUnLocks(Locker locker, StateKeys stateKeys) {
        String[] keys = stateKeys.getNoCompleteKeys();
        List<Integer> failKeyIndex = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            LockInfo lockInfo = lockPool.get(keys[i]);
            if (lockInfo != null) {
                if (lockInfo.getLocker().equals(locker.getName())) {
                    if (lockInfo.decr() == 0) {
                        lockPool.remove(keys[i]);
                    }
                } else {
                    failKeyIndex.add(i);
                }
            }
        }
        return failKeyIndex.stream().mapToInt(i -> i).toArray();
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

