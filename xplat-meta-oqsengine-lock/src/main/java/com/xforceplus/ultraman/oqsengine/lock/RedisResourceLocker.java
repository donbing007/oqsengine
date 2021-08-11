package com.xforceplus.ultraman.oqsengine.lock;

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
public class RedisResourceLocker implements ResourceLocker {

    @Resource(name = "redissonClientLocker")
    private RedissonClient redissonClient;

    public RedisResourceLocker() {
    }

    public RedisResourceLocker(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
    }

    @Override
    public boolean tryLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(timeout, unit);
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
}
