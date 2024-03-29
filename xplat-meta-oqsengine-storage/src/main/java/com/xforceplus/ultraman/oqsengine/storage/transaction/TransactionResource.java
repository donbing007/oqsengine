package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 表示一个事务资源.这是事务管理的最小单元.
 *
 * @param <V> 资源类型.
 * @author dongbin
 * @version 0.1 2020/2/15 21:51
 * @since 1.8
 */
public interface TransactionResource<V> {

    /**
     * 资源和事务绑定.
     *
     * @param transaction 事务.
     */
    void bind(Transaction transaction);

    /**
     * 返回绑定的事务.
     *
     * @return 事务
     */
    Optional<Transaction> getTransaction();

    /**
     * 资源类型的标记.
     *
     * @return 资源类型.
     */
    TransactionResourceType type();

    /**
     * 资源的标识.
     *
     * @return 资源的标识.
     */
    String key();

    /**
     * 承载的资源实体.
     *
     * @return 资源.
     */
    V value();

    /**
     * 提交资源.
     *
     * @param commitId 提交号.
     * @throws SQLException 异常.
     */
    void commit(long commitId) throws SQLException;

    /**
     * 没有提交号的提交,一般是只读事务状态.
     *
     * @throws SQLException 异常.
     */
    void commit() throws SQLException;

    /**
     * 回滚资源.
     *
     * @throws SQLException 异常.
     */
    void rollback() throws SQLException;

    /**
     * 资源的销毁或者回收.
     *
     * @throws SQLException 异常.
     */
    void destroy() throws SQLException;

    /**
     * 资源是否销毁或者回收.
     */
    boolean isDestroyed() throws SQLException;
}
