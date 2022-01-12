package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.lock.utils.Locker;
import com.xforceplus.ultraman.oqsengine.lock.utils.LockerSupport;
import com.xforceplus.ultraman.oqsengine.lock.utils.StateKeys;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 还未实现完成.
 *
 * @author dongbin
 * @version 0.1 2022/1/4 11:03
 * @since 1.8
 */
public abstract class AbstractNotificationResourceLocker implements ResourceLocker {

    // 同步器映射 key为锁定key,value为线程等待的同步器.
    private ConcurrentMap<String, Sync> syncs = new ConcurrentHashMap<>();

    @Override
    public boolean isLocking(String resource) {
        return doIsLocking(LockerSupport.buildLockKey(resource));
    }

    @Override
    public void lock(String resource) throws InterruptedException {
        tryLocks(Long.MAX_VALUE, resource);
    }

    @Override
    public boolean tryLock(String resource) {
        boolean result = false;
        try {
            result = tryLocks(-1, resource);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean tryLock(long waitTimeoutMs, String resoruce) throws InterruptedException {
        return tryLocks(waitTimeoutMs, resoruce);
    }

    @Override
    public boolean unlock(String resource) {
        return unlocks(resource).length == 1;
    }

    @Override
    public void locks(String... resources) throws InterruptedException {
        tryLocks(Long.MAX_VALUE, resources);
    }

    @Override
    public boolean tryLocks(String... resources) {
        boolean result = false;
        try {
            result = tryLocks(-1, resources);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean tryLocks(long waitTimeoutMs, String... resources) throws InterruptedException {
        StateKeys keys = new StateKeys(buildKeys(resources));
        Locker locker = LockerSupport.getLocker();

        doLocks(locker, keys);

        if (keys.isComplete()) {
            return true;
        } else {
            if (waitTimeoutMs <= 0) {
                unlocks(resources);
                return false;
            }
        }

        long timePassMs = 0;
        while (!keys.isComplete()) {
            /*
            加锁有失败,根据 waitTimeoutMs来决定如下分支.
            1. 等待指定毫秒.
            2. 不等待直接返回失败.
             */
            String failKey = keys.getCurrentKey();

            // 等待解锁.
            long start = System.currentTimeMillis();
            syncs.computeIfAbsent(failKey, (k) -> new Sync())
                .tryAcquireNanos(1, TimeUnit.MICROSECONDS.toNanos(waitTimeoutMs));
            timePassMs += System.currentTimeMillis() - start;

            if (timePassMs > waitTimeoutMs) {
                unlocks(resources);
                return false;
            }

            doLocks(locker, keys);
        }
        return true;
    }

    @Override
    public String[] unlocks(String... resources) {
        StateKeys keys = new StateKeys(buildKeys(resources));
        Locker locker = LockerSupport.getLocker();

        boolean ok = false;
        try {
            doUnLocks(locker, keys);

            if (ok) {
                locker.decrSuccess(keys.size());
            }

        } finally {
            if (ok) {
                LockerSupport.cleanLockerIfCan();
            }
        }

        return null;
    }

    private String[] buildKeys(String... resources) {
        return Arrays.stream(resources)
            .distinct()
            .sorted()
            .map(r -> LockerSupport.buildLockKey(r))
            .toArray(String[]::new);
    }

    // state 0表示未占用,大于0表示被占用.
    private static class Sync extends AbstractQueuedSynchronizer {

        private static final int STATE_FREE = 0;
        private static final int STATE_LOCKER = 1;

        public Sync() {
            setState(STATE_LOCKER);
        }

        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(STATE_FREE, STATE_LOCKER)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == STATE_FREE) {
                throw new IllegalMonitorStateException();
            }

            setExclusiveOwnerThread(null);
            setState(STATE_FREE);
            return true;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == STATE_LOCKER;
        }

        public boolean hasThreads() {
            return this.hasQueuedThreads();
        }
    }

    /**
     * 通知指定key被解锁.
     *
     * @param key 被解锁的key.
     */
    protected void noticeUnlock(String key) {
        Sync sync = syncs.get(key);
        if (sync != null) {
            sync.tryRelease(1);
        }
    }

    /**
     * 子类需要实现的批量加锁方法.
     * 试图加锁.如果失败会返回失败的key序号.从0开始.
     * 注意: 已经增加的锁必须保持.
     * 全部成功返回值小于0.
     */
    protected abstract void doLocks(Locker locker, StateKeys keys);

    /**
     * 子类需要实现的批量解锁方法.
     * 子类需要保证如果加锁失败不会产生额外的副作用.
     * 比如部份解锁成功,具体看子类的实现来决定是否允许这个副作用.
     */
    protected abstract void doUnLocks(Locker locker, StateKeys keys);

    /**
     * 判断指定的key是否被锁定中.
     */
    protected abstract boolean doIsLocking(String key);
}
