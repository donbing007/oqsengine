package com.xforceplus.ultraman.oqsengine.storage.transaction;


import java.sql.SQLException;
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
     * 创建一个事务,在指定时间后超时.
     *
     * @param timeoutMs 超时时间.(毫秒)
     * @return 新事务.
     */
    Transaction create(long timeoutMs);

    /**
     * 创建一个事务,指定了事务消息,并使用默认的超时时间.
     *
     * @param message 事务说明消息.
     * @return 新事务.
     */
    Transaction create(String message);

    /**
     * 创建一个事务,在指定时间后超时.
     * 并指定了事务消息.
     *
     * @param timeoutMs 超时时间.(毫秒)
     * @param message   事务说明消息.
     * @return 新事务实例.
     */
    Transaction create(long timeoutMs, String message);

    /**
     * 获取当前上下文绑定的事务.
     * @return 事务.
     */
    Optional<Transaction> getCurrent();

    /**
     * 绑定一个非受管事务.
     * @param id 事务id.
     */
    void bind(long id);

    /**
     * 让事务不再绑定到当前上下文.
     */
    void unbind();

    /**
     * 结束某个事务.
     * @param tx 目标事务.
     */
    void finish(Transaction tx) throws SQLException;

    /**
     * 结束当前的事务.
     */
    void finish() throws SQLException;

    /**
     * 获取运行中的事务数量.
     *
     * @return 事务数量.
     */
    int size();

    /**
     * 冻结,当前事务管理器不能再产生新的事务.
     * 但已有的事务仍旧可以工作.
     */
    void freeze();

    /**
     * 斛除冻结,恢复正常.
     */
    void unfreeze();

}
