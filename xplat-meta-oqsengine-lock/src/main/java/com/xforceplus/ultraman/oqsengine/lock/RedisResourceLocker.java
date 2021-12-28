package com.xforceplus.ultraman.oqsengine.lock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * 一个redisson lock锁的包装实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/10 18:41
 * @since 1.8
 */
public class RedisResourceLocker implements MultiResourceLocker {

    @Resource(name = "redissonClient")
    private RedissonClient redissonClient;

    /*
     * 默认的锁存在时间.
     */
    private static long MAX_LOCKER_TIME_MS = 1000 * 60 * 10;

    public RedisResourceLocker() {
    }

    public RedisResourceLocker(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean isLocking(String key) {
        return redissonClient.getLock(key).isLocked();
    }

    @Override
    public void lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(MAX_LOCKER_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void locks(String... keys) {
        RLock mlock = buildRedissonMultiLock(keys);
        mlock.lock(MAX_LOCKER_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(String key) {
        return tryLock(-1, key);
    }

    @Override
    public boolean tryLock(long waitTimeoutMs, String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTimeoutMs, MAX_LOCKER_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLocks(String... keys) {
        return tryLocks(-1, keys);
    }

    @Override
    public boolean tryLocks(long waitTimeoutMs, String... keys) {
        RLock mlock = buildRedissonMultiLock(keys);

        try {
            return mlock.tryLock(waitTimeoutMs, MAX_LOCKER_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
        return true;
    }

    @Override
    public void unlocks(String... keys) {
        RLock mlock = buildRedissonMultiLock(keys);

        mlock.unlock();
    }

    /**
     * 这里对于keys进行了排序,这是为了保证多线程情况加锁的顺序是一致的避免死锁.否则将面临如下情况.
     * T1 加锁的key为  {1, 2, 3}
     * T2 加锁的key为  {3, 2, 9}
     * 两线程并发情况下,T1加锁2成功,T2加锁3成功,然后T1等等T2释放锁3, T2等待T1释放锁2,从而死锁.
     * 这里进行了排序如下.
     * T1 加锁的key为  {1, 2, 3}
     * T2 加锁的key为  {2, 3, 9}
     * 保证不会出现互相等待的情况.
     */
    private RLock buildRedissonMultiLock(String... keys) {
        RLock[] locks =
            Arrays.stream(keys).parallel().distinct().sorted().map(k -> redissonClient.getLock(k)).toArray(RLock[]::new);
        return redissonClient.getMultiLock(locks);
    }
}
