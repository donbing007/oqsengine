package com.xforceplus.ultraman.oqsengine.lock;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个为了子类实现方便的抽象类.
 * 此抽象类定义了锁定的重试时间和为标识每一个线程的方式.
 * 每一个线程在第一次加锁时都会利用ThreadLocal来记录一个UUID表示本加锁者的标识，
 * 此实现并不是个严格的锁实现，如果当前线程A没有拿到锁并进入等待，在等待时（默认为50ms)
 * 锁解除这时有新的一个线程B试图加锁，那么B线程有可能加锁成功A线程继续等待。
 * 可以设定重试的等待时间来减少这个问题，不过资源操作需要严格的顺序时此实现可能不适用。
 * 子类只需要实现doLock,doUnLock,isLocked三个方法即可.
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:10
 * @since 1.5
 */
public abstract class AbstractResourceLocker implements MultiResourceLocker {

    private static final String LOCK_KEY_PREIFX = "locker.";

    /*
     * 重试的默认间隔,{@value}毫秒.
     */
    private static final long RETRY_DELAY = 50;
    /*
     * 为每个操作的线程记录一个唯一ID号
     */
    private final ThreadLocal<Locker> threadInfo = new ThreadLocal();
    /*
     * 等待的重试时间
     */
    private long retryDelay = 0;

    /**
     * 获取当前的重试等待时间(毫秒).
     *
     * @return 重试等待时间(毫秒)。
     */
    public long getRetryDelay() {
        return retryDelay <= 0 ? RETRY_DELAY : retryDelay;
    }

    /**
     * 设置当前的重试等待时间(毫秒).
     *
     * @param retryDelay 重试等待时间(毫秒)。
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public boolean isLocking(String resource) {
        String key = buildLockKey(resource);
        return doIsLocking(key);
    }

    /**
     * 锁定资源,如果不能获得资源的锁那么调用线程将一直阻塞到获取锁为止.
     *
     * @param resource 资源的键.
     */
    @Override
    public void lock(String resource) {
        doTryLocks(Long.MAX_VALUE, resource);
    }

    @Override
    public void locks(String... resources) {
        doTryLocks(Long.MAX_VALUE, resources);
    }

    /**
     * 尝试获取资源的锁,获取成功返回true,否则返回false.
     *
     * @param resource 资源的键
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    @Override
    public boolean tryLock(String resource) {
        return doTryLocks(-1, resource);
    }

    /**
     * 基本功能同tryLock方法,增加了一个等待时间限制.
     * 在指定的时间内还没有成功获取锁将返回false,否则返回true.
     * 如果设置的等待数值为小于等于0,那么将退化成没有等待时间.
     *
     * @param waitTimeoutMs 最大等待时间.(毫秒)
     * @param resoruce      资源的键.
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    @Override
    public boolean tryLock(long waitTimeoutMs, String resoruce) {
        return doTryLocks(waitTimeoutMs, resoruce);
    }

    @Override
    public boolean tryLocks(String... resources) {
        return doTryLocks(-1, resources);
    }

    @Override
    public boolean tryLocks(long waitTimeoutMs, String... resources) {
        return doTryLocks(waitTimeoutMs, resources);
    }

    /**
     * 解除对于资源的锁占用.如果本身资源并没有锁,那么将无条件的返回true.
     *
     * @param resource 资源的键.
     * @return true解锁成功, false解锁失败(不是加锁者但试图进行解锁).
     */
    @Override
    public boolean unlock(String resource) {
        return unlocks(resource);
    }

    @Override
    public boolean unlocks(String... resources) {
        boolean single = resources.length == 1;
        String key = null;
        String[] keys = null;
        if (single) {
            key = buildLockKey(resources[0]);
        } else {
            keys = Arrays.stream(resources).distinct().sorted().map(r -> buildLockKey(r)).toArray(String[]::new);
        }
        Locker locker = getLocker();

        boolean ok = false;
        try {
            if (single) {
                ok = doUnLock(locker, key);

                if (ok) {
                    locker.decrSuccess();
                }

            } else {
                ok = doUnLocks(locker, keys);

                if (ok) {
                    locker.decrSuccess(keys.length);
                }
            }
        } finally {
            if (ok) {
                cleanLockerIfCan();
            }
        }

        return ok;
    }

    /**
     * 尝试加锁的实际实现.
     *
     * @param waitTimeoutMs 加锁失败后的等待解锁时间,-1表示不等待.
     * @param resources     需要加锁的资源.
     */
    private boolean doTryLocks(long waitTimeoutMs, String... resources) {
        // 判断是否只有一个资源.
        boolean single = resources.length == 1;
        String[] keys = null;
        String key = null;
        if (single) {
            key = buildLockKey(resources[0]);
        } else {
            keys = Arrays.stream(resources).distinct().sorted().map(r -> buildLockKey(r)).toArray(String[]::new);
        }

        boolean ok = false;
        Locker locker = getLocker();
        long delay = getRetryDelay();

        try {
            long timePass = 0;
            while (!ok) {
                if (single) {

                    ok = doLock(locker, key);

                    if (ok) {
                        locker.incrSuccess();
                    }

                } else {
                    ok = doLocks(locker, keys);

                    if (ok) {
                        locker.incrSuccess(keys.length);
                    }
                }

                // 无需等待
                if (waitTimeoutMs <= 0) {
                    break;
                }

                if (!ok) {

                    await(delay);

                    timePass += delay;
                    if (timePass >= waitTimeoutMs) {
                        break;
                    }
                }
            }
        } finally {
            // 如果加锁失败,那么尝试清除当前的加锁者信息.
            cleanLockerIfCan();
        }

        return ok;
    }

    /**
     * 当前线程睡眠指定毫秒数.
     *
     * @param time 睡眠时间.
     */
    private void await(long time) {

        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(time));
    }

    // 构造资源的锁定key.
    private String buildLockKey(String resource) {
        return LOCK_KEY_PREIFX.concat(resource);
    }

    /**
     * 返回当前线程的唯一标识,如果没有标识则会生成一个.
     *
     * @return 当前线程的标识.
     */
    private Locker getLocker() {
        Locker locker = threadInfo.get();
        if (locker == null) {
            locker = new Locker();
            threadInfo.set(locker);
        }

        return locker;
    }

    private void cleanLockerIfCan() {
        Locker locker = threadInfo.get();
        if (locker != null) {
            if (locker.getSuccessLockNumber() <= 0) {
                threadInfo.remove();
            }
        }
    }

    /**
     * 加锁者.
     */
    protected static class Locker {
        // 加锁者名称.
        private String name;
        // 当前剩余加锁数量.
        private int successLockNumber;

        public Locker() {
            name = UUID.randomUUID().toString();
            successLockNumber = 0;
        }

        public void incrSuccess() {
            this.incrSuccess(1);
        }

        public void incrSuccess(int size) {
            successLockNumber += size;
        }

        public void decrSuccess() {
            successLockNumber -= 1;
        }

        public void decrSuccess(int size) {
            successLockNumber -= size;
            if (successLockNumber < 0) {
                successLockNumber = 0;
            }
        }

        public String getName() {
            return name;
        }

        public int getSuccessLockNumber() {
            return successLockNumber;
        }
    }

    /**
     * 子类需要实现的锁定方法.
     */
    protected abstract boolean doLock(Locker locker, String key);

    /**
     * 子类需要实现的批量锁定方法.
     * 子类需要保证如果加锁失败不会产生额外的副作用.
     * 比如部份加锁成功,具体看子类的实现来决定是否允许这个副作用.
     */
    protected abstract boolean doLocks(Locker locker, String... keys);

    /**
     * 子类需要实现的解锁方法.
     */
    protected abstract boolean doUnLock(Locker locker, String key);

    /**
     * 子类需要实现的批量解锁方法.
     * 子类需要保证如果加锁失败不会产生额外的副作用.
     * 比如部份解锁成功,具体看子类的实现来决定是否允许这个副作用.
     */
    protected abstract boolean doUnLocks(Locker locker, String... keys);

    /**
     * 判断是否锁定中.
     */
    protected abstract boolean doIsLocking(String key);
}
