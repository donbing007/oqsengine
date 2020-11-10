package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;

import java.sql.SQLException;

/**
 * 事务任务执行器.
 * 可以将需要在事务内运行的任务交由执行器来执行,以保证任务运行的事务管理.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 14:18
 * @since 1.8
 */
public interface TransactionExecutor extends Executor<StorageTask, Object> {

    /**
     * 事务执行任务.
     *
     * @param storageTask 目标任务.
     * @return 执行结果.
     * @throws SQLException 执行异常.
     */
    @Override
    Object execute(StorageTask storageTask) throws SQLException;
}
