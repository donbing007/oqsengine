package com.xforceplus.ultraman.oqsengine.lock;

/**
 * 资源锁的连锁定义.
 * 必须保证如下语义.
 * <p></p>
 * locker.locks("k1","k2");      //成功
 * locker.lock("k2");            //阻塞
 * <p></p>
 * locker.lock("k2");            //成功
 * locker.locks("k1","k2");      //阻塞
 * <p></p>
 * locker.lock("k2");            //成功
 * locker.tryLocks("k1","k2");   //加锁失败.
 * <p></p>
 * 注意: 下面描述一个在同一线程中出现的情况.
 *
 * @author dongbin
 * @version 0.1 2021/12/9 13:59
 * @since 1.8
 */
public interface MultiResourceLocker extends ResourceLocker {

    /**
     * 所有的key都成功加锁才表示加锁成功.
     * 否则持续等待.
     *
     * @param resources 资源键列表.
     */
    public void locks(String... resources);

    /**
     * 尝试为所有键加锁,有任意资源加锁失败即表示都失败.
     *
     * @param resources 资源键列表.
     * @return true 加锁成功, false 加锁失败.
     */
    public boolean tryLocks(String... resources);

    /**
     * 尝试为所有键加锁,如果有锁加锁失败将等待指定时间.
     *
     * @param waitTimeoutMs 等待时间.(毫秒)
     * @param resources     资源键列表.
     * @return true 加锁成功, false 加锁失败.
     */
    public boolean tryLocks(long waitTimeoutMs, String... resources);

    /**
     * 解锁.
     *
     * @param resources 资源键列表.
     * @return true 所有锁都解锁成功,false 解锁失败.
     */
    public boolean unlocks(String... resources);
}
