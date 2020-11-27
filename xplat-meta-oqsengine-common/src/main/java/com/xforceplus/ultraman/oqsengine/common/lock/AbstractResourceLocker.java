package com.xforceplus.ultraman.oqsengine.common.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 一个为了子类实现方便的抽象类。
 * 此抽象类定义了锁定的重试时间和为标识每一个线程的方式。
 * 每一个线程在第一次加锁时都会利用ThreadLocal来记录一个UUID表示本加锁者的标识，
 * 此实现并不是个严格的锁实现，如果当前线程A没有拿到锁并进入等待，在等待时（默认为50ms)
 * 锁解除这时有新的一个线程B试图加锁，那么B线程有可能加锁成功A线程继续等待。
 * 可以设定重试的等待时间来减少这个问题，不过资源操作需要严格的顺序时此实现可能不适用。
 * <p>
 * 子类只需要实现
 * doLock,doUnLock,isLocked三个方法即可。
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:10
 * @since 1.5
 */
public abstract class AbstractResourceLocker implements ResourceLocker {

    /**
     * 重试的默认间隔,{@value}毫秒.
     */
    private static final long RETRY_DELAY = 50;
    /**
     * 为每个操作的线程记录一个唯一ID号
     */
    private final ThreadLocal<Map<String, String>> THREAD_INFO = new ThreadLocal();
    /**
     * 等待的重试时间
     */
    private long retryDelay = 0;

    /**
     * 获取当前的重试等待时间(毫秒)。
     *
     * @return 重试等待时间(毫秒)。
     */
    public long getRetryDelay() {
        return retryDelay <= 0 ? RETRY_DELAY : retryDelay;
    }

    /**
     * 设置当前的重试等待时间(毫秒)。
     *
     * @param retryDelay 重试等待时间(毫秒)。
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * 锁定资源,如果不能获得资源的锁那么调用线程将一直阻塞到获取锁为止.
     *
     * @param key 资源的键.
     */
    @Override
    public void lock(String key) {
        boolean ok = false;
        String storeKey = buildStoreResourceKey(key);
        String lockingId = makeThreadId(key);
        long delay = getRetryDelay();
        while (!ok) {
            ok = doLock(storeKey, lockingId);

            if (!ok) {
                try {
                    await(delay);
                } catch (InterruptedException ex) {
                    if (!ok) {
                        removeThreadId(key);
                    }
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 尝试获取资源的锁,获取成功返回true,否则返回false.
     *
     * @param key 资源的键
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    @Override
    public boolean tryLock(String key) {
        boolean result = doLock(buildStoreResourceKey(key), makeThreadId(key));
        if (!result) {
            //没有加锁成功,去除本次准备的lockingId
            removeThreadId(key);
        }

        return result;
    }

    /**
     * 基本功能同tryLock方法,增加了一个等待时间限制.
     * 在指定的时间内还没有成功获取锁将返回false,否则返回true.
     * 如果设置的等待数值为小于等于0,那么将退化成没有等待时间.
     *
     * @param key  资源的键.
     * @param time 最大等待时间.
     * @param unit 时间单位.
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    @Override
    public boolean tryLock(String key, long time, TimeUnit unit) {
        if (time <= 0) {
            return tryLock(key);
        }

        boolean ok = false;
        long timeout = unit.toMillis(time);
        long timePass = 0;
        String storeKey = buildStoreResourceKey(key);
        String lockingId = makeThreadId(key);
        long delay = getRetryDelay();
        while (!ok) {
            ok = doLock(storeKey, lockingId);

            if (!ok) {
                try {
                    await(delay);
                } catch (InterruptedException ex) {
                    if (!ok) {
                        removeThreadId(key);
                    }
                    throw new RuntimeException(ex.getMessage(), ex);
                }
                timePass += delay;
                if (timePass >= timeout) {
                    break;
                }
            }
        }

        if (!ok) {
            //没有加锁成功,去除本次准备的lockingId
            removeThreadId(key);
        }
        return ok;
    }

    /**
     * 解除对于资源的锁占用.如果本身资源并没有锁,那么将无条件的返回true.
     *
     * @param key 资源的键.
     * @return true解锁成功, false解锁失败(不是加锁者但试图进行解锁).
     */
    @Override
    public boolean unlock(String key) {
        String storeKey = buildStoreResourceKey(key);

        String unLockingId = getThreadId(key);

        String lockingId = isLocked(storeKey);

        if (lockingId != null) {

            if (lockingId.equals(unLockingId)) {

                int result = doUnLock(storeKey);

                if (result == 0) {
                    removeThreadId(key);
                }
                if (result < 0) {
                    return false;
                } else {
                    return true;
                }

            } else {
                //不是加锁者
                return false;

            }
        } else {
            //资源没有锁定
            return true;
        }
    }

    @Override
    public int getLockNumber(String key) {
        return this.doLockNumber(buildStoreResourceKey(key));
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
     * 构造同步的标记key.
     *
     * @param key 原始资源key.
     * @return 新的key.
     */
    private String buildStoreResourceKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Invalid resource lock.");
        }
        StringBuilder keyBuff = new StringBuilder();
        keyBuff.append("lock.mark#");
        keyBuff.append(key);
        return keyBuff.toString();
    }

    private String makeThreadId(String key) {
        Map<String, String> idMap = THREAD_INFO.get();
        if (idMap == null) {
            idMap = new HashMap();
            THREAD_INFO.set(idMap);
        }

        String id = idMap.get(key);
        if (id == null) {
            id = UUID.randomUUID().toString();
            idMap.put(key, id);
        }

        return id;
    }

    /**
     * 返回当前线程的唯一标识,如果没有标识则会生成一个.
     *
     * @return 当前线程的标识.
     */
    private String getThreadId(String key) {
        Map<String, String> idMap = THREAD_INFO.get();
        if (idMap == null) {
            //如果没有找到锁定线程id容器,返回一个新的,这会造成无法解锁
            return UUID.randomUUID().toString();
        }

        return idMap.get(key);
    }

    private void removeThreadId(String key) {
        Map<String, String> idMap = THREAD_INFO.get();
        idMap.remove(key);
        if (idMap.isEmpty()) {
            THREAD_INFO.remove();
        }
    }

    /**
     * 返回当前的线程本地变量.
     *
     * @return 线程本地变量.
     */
    protected ThreadLocal getThreadLocal() {
        return THREAD_INFO;
    }

    /**
     * 子类需要实现的锁定方法。
     *
     * @param key       资源的key.
     * @param lockingId 锁定者的标识。
     * @return true锁定成功，false锁定失败。
     */
    protected abstract boolean doLock(String key, String lockingId);

    /**
     * 子类需要实现的解锁方法。
     *
     * @param key 资源的key.
     * @return true解锁成功，false解锁失败。
     */
    protected abstract int doUnLock(String key);

    /**
     * 判断当前此资源是否锁定中，如果锁定返回加锁者的标识，否则返回null.
     *
     * @param key 资源的键。
     * @return 加锁者的标识，没有锁定返回null.
     */
    protected abstract String isLocked(String key);

    /**
     * 获取当前资源的锁定数量.
     *
     * @param key 资源.
     * @return 锁定数量.
     */
    protected abstract int doLockNumber(String key);
}
