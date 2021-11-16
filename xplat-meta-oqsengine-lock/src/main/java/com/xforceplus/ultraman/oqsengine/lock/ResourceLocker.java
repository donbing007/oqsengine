package com.xforceplus.ultraman.oqsengine.lock;

import java.util.concurrent.TimeUnit;

/**
 * 资源锁定义.
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:10
 * @since 1.5
 */
public interface ResourceLocker {

    /**
     * 锁定资源,如果不能获得资源的锁那么调用线程将一直阻塞到获取锁为止.
     *
     * @param key 资源的键.
     */
    public void lock(String key);

    /**
     * 尝试获取资源的锁,获取成功返回true,否则返回false.
     *
     * @param key 资源的键
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    public boolean tryLock(String key);

    /**
     * 基本功能同tryLock方法,增加了一个等待时间限制.
     * 在指定的时间内还没有成功获取锁将返回false,否则返回true.
     *
     * @param key      资源的键.
     * @param timeout 超时时间.如果小于等于0,将退化成无等待时间.
     * @param unit     时间单位.
     * @return true表示成功获取锁, false表示没有获取到锁.
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit);

    /**
     * 解除对于资源的锁占用.解锁者必须为加锁者.
     * 如果是A线程加的锁,那么解锁也必须为A线程来执行.
     *
     * @param key 资源的键.
     * @return true表示解锁成功, false表示解锁失败.
     */
    public boolean unlock(String key);


}
