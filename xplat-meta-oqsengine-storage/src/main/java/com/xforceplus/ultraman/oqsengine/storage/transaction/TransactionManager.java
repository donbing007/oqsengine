package com.xforceplus.ultraman.oqsengine.storage.transaction;


import java.util.Optional;

/**
 * transaction manager.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 16:58
 * @since 1.8
 */
public interface TransactionManager {

    /**
     * 创建新的事务.
     * @return 新事务.
     */
    Transaction create();

    /**
     * 获取当前上下文绑定的事务.
     * @return 事务.
     */
    Optional<Transaction> getCurrent();

    /**
     * 重建事务.
     * @param id 事务 id.
     */
    void rebind(long id);

    /**
     * 绑定一个非受管事务.
     * @param tx 事务.
     */
    void bind(Transaction tx);

    /**
     * 让事务不再绑定到当前上下文.
     */
    void unbind();

    /**
     * 结束某个事务.
     * @param tx 目标事务.
     */
    void finish(Transaction tx);

}
