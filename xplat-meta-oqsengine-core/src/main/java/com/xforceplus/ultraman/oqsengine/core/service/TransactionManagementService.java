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
     * 开始一个事务.
     * @return 事务 id.
     */
    long begin() throws SQLException;

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
