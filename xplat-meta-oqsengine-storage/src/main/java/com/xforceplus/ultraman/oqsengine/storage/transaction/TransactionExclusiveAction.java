package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;

/**
 * 事务的排他任务执行器.
 *
 * @author dongbin
 * @version 0.1 2020/8/14 17:36
 * @since 1.8
 */
public interface TransactionExclusiveAction {

    /**
     * 实际动作.
     *
     * @throws SQLException
     */
    void act() throws SQLException;
}
