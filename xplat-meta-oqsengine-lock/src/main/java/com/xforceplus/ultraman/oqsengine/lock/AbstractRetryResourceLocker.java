package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.lock.utils.Locker;
import com.xforceplus.ultraman.oqsengine.lock.utils.LockerSupport;
import com.xforceplus.ultraman.oqsengine.lock.utils.StateKeys;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
public abstract class AbstractRetryResourceLocker implements ResourceLocker {

    /*
     * 重试的默认间隔,{@value}毫秒.
     */
    private static final long RETRY_DELAY = 50;
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
        String key = LockerSupport.buildLockKey(resource);
        return doIsLocking(key);
    }

    /**
     * 锁定资源,如果不能获得资源的锁那么调用线程将一直阻塞到获取锁为止.
     *
     * @param resource 资源的键.
     */
    @Override
    public void lock(String resource) throws InterruptedException {
        doTryLocks(Long.MAX_VALUE, resource);
    }

    @Override
    public void locks(String... resources) throws InterruptedException {
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
        boolean result = false;
        try {
            result = doTryLocks(-1, resource);
        } catch (InterruptedException e) {
            // donothing.
        }
        return result;
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
    public boolean tryLock(long waitTimeoutMs, String resoruce) throws InterruptedException {
        return doTryLocks(waitTimeoutMs, resoruce);
    }

    @Override
    public boolean tryLocks(String... resources) {
        boolean result = false;
        try {
            result = doTryLocks(-1, resources);
        } catch (InterruptedException e) {
            // donoting
        }
        return result;
    }

    @Override
    public boolean tryLocks(long waitTimeoutMs, String... resources) throws InterruptedException {
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
        return unlocks(resource).length == 0;
    }

    @Override
    public String[] unlocks(String... resources) {
        StateKeys stateKeys = buildKeys(resources);
        Locker locker = LockerSupport.getLocker();

        int[] failKeyIndex = doUnLocks(locker, stateKeys);
        String[] keys = stateKeys.getKeys();
        return Arrays.stream(failKeyIndex)
            .mapToObj(i -> LockerSupport.parseResourceFormLockKey(keys[i])).toArray(String[]::new);

    }

    /**
     * 尝试加锁的实际实现.
     *
     * @param waitTimeoutMs 加锁失败后的等待解锁时间,-1表示不等待.
     * @param resources     需要加锁的资源.
     */
    private boolean doTryLocks(long waitTimeoutMs, String... resources) throws InterruptedException {
        StateKeys keys = buildKeys(resources);
        Locker locker = LockerSupport.getLocker();

        long delay = getRetryDelay();

        try {
            long timePass = 0;
            while (true) {

                doLocks(locker, keys);

                if (keys.isComplete()) {
                    locker.incrSuccess(keys.size());
                    return true;
                }

                // 无需等待
                if (waitTimeoutMs <= 0) {
                    return false;
                }

                if (!keys.isComplete()) {

                    await(delay);

                    timePass += delay;
                    if (timePass >= waitTimeoutMs) {
                        return false;
                    }
                }
            }
        } finally {
            if (!keys.isComplete()) {

                doUnLocks(locker, new StateKeys(keys.getCompleteKeys()));

                // 如果加锁失败,那么尝试清除当前的加锁者信息.
                LockerSupport.cleanLockerIfCan();
            }
        }
    }

    private StateKeys buildKeys(String... resources) {
        return new StateKeys(Arrays.stream(resources)
            .distinct()
            .sorted()
            .map(r -> LockerSupport.buildLockKey(r))
            .toArray(String[]::new));
    }

    /**
     * 当前线程睡眠指定毫秒数.
     *
     * @param time 睡眠时间.
     */
    private void await(long time) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(time);
    }

    /**
     * 子类需要实现的批量锁定方法.
     * 如果产生部份加锁的情况,子类应该保持已经加锁不能被解锁.
     */
    protected abstract void doLocks(Locker locker, StateKeys keys);

    /**
     * 子类需要实现的批量解锁方法.
     * 子类需要保证如果加锁失败不会产生额外的副作用.
     * 比如部份解锁成功,具体看子类的实现来决定是否允许这个副作用.
     */
    protected abstract int[] doUnLocks(Locker locker, StateKeys keys);

    /**
     * 判断是否锁定中.
     */
    protected abstract boolean doIsLocking(String key);
}
