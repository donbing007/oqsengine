package com.xforceplus.ultraman.oqsengine.lock;

/**
 * 资源锁定义.
 * 如果是联锁必须保证如下语义.
 * <p></p>
 * locker.locks("k1","k2");      //线程1成功
 * locker.lock("k2");            //线程2阻塞
 * <p></p>
 * locker.lock("k2");            //线程1成功
 * locker.locks("k1","k2");      //线程2阻塞
 * <p></p>
 * locker.lock("k2");            //线程1成功
 * locker.tryLocks("k1","k2");   //线程2加锁失败.
 * <p></p>
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:10
 * @since 1.5
 */
public interface ResourceLocker {

    /**
     * 判断是否被锁定中.
     *
     * @param resource 资源键.
     * @return true 锁定中, false没有锁定.
     */
    public boolean isLocking(String resource);

    /**
     * 锁定资源,如果不能获得资源的锁那么调用线程将一直阻塞到获取锁为止.
     *
     * @param resource 资源的键.
     * @throws InterruptedException 等待时线程被中断,这应该被视为
     */
    public void lock(String resource) throws InterruptedException;

    /**
     * 尝试获取资源的锁,获取成功返回true,否则返回false.
     *
     * @param resource 资源的键
     * @return true 所有资源加锁成功, false 所有资源加锁失败.
     */
    public boolean tryLock(String resource);

    /**
     * 基本功能同tryLock方法,增加了一个等待时间限制.
     * 在指定的时间内还没有成功获取锁将返回false,否则返回true.
     *
     * @param waitTimeoutMs 超时时间.如果小于等于0,将退化成无等待时间.(毫秒)
     * @param resoruce      资源的键.
     * @return true 所有资源加锁成功, false 所有资源加锁失败.
     */
    public boolean tryLock(long waitTimeoutMs, String resoruce) throws InterruptedException;

    /**
     * 解除对于资源的锁占用.解锁者必须为加锁者.
     * 如果是A线程加的锁,那么解锁也必须为A线程来执行.
     * 如果未加锁,或者不是本调用者加锁都将跳过.
     *
     * @param resource 资源的键.
     * @return true成功解锁, false 未解锁.
     */
    public boolean unlock(String resource);

    /**
     * 所有的key都成功加锁才表示加锁成功.
     * 否则持续等待.
     *
     * @param resources 资源键列表.
     */
    public void locks(String... resources) throws InterruptedException;

    /**
     * 尝试为所有键加锁,有任意资源加锁失败即表示都失败.
     *
     * @param resources 资源键列表.
     * @return true 所有资源加锁成功, false 所有资源加锁失败.
     */
    public boolean tryLocks(String... resources);

    /**
     * 尝试为所有键加锁,如果有锁加锁失败将等待指定时间.
     * 如果加锁失败,那么会保证回到加锁之前的状态.
     *
     * @param waitTimeoutMs 等待时间.(毫秒)
     * @param resources     资源键列表.
     * @return true 所有资源加锁成功, false 所有资源加锁失败.
     */
    public boolean tryLocks(long waitTimeoutMs, String... resources) throws InterruptedException;

    /**
     * 解锁.
     * 如果是A线程加的锁,那么解锁也必须为A线程来执行.
     * 未加锁的锁将认为解锁成功.
     *
     * @param resources 资源键列表.
     * @return 未解锁的资源.
     */
    public String[] unlocks(String... resources);
}
