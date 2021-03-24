package com.xforceplus.ultraman.oqsengine.core.service;

import java.sql.SQLException;

/**
 * 事务管理服务.
 * @author dongbin
 * @version 0.1 2020/2/17 20:43
 * @since 1.8
 */
public interface TransactionManagementService {

    /**
     * 表示使用全局默认事务超时时间.
     */
    static final long DEFAULT_TRANSACTION_TIMEOUT = 0;

    /**
     * 开始一个事务.
     * @return 事务 id.
     * @throws SQLException 创建事务失败.
     */
    long begin() throws SQLException;

    /**
     * 开始一个事务,并指定事务超时时间.
     * @param timeoutMs 超时毫秒数,不可为负数.0表示无限制.
     * @return 事务 id.
     * @throws SQLException 创建事务失败.
     */
    long begin(long timeoutMs) throws SQLException;

    /**
     * 开始一个新的事务,并指定事务超时时间和事务消息.
     *
     * @param timeoutMs 超时毫秒数,不可为负数.0表示无限制.
     * @param msg       事务消息.
     * @return 事务id.
     * @throws SQLException 创建事务失败.
     */
    long begin(long timeoutMs, String msg) throws SQLException;

    /**
     * 恢复事务.
     *
     * @param id 事务 id.
     * @throws SQLException 恢复失败.
     */
    void restore(long id) throws SQLException;

    /**
     * 提交一个事务.
     */
    void commit() throws SQLException;

    /**
     * 回滚一个事务.
     */
    void rollback() throws SQLException;
}
