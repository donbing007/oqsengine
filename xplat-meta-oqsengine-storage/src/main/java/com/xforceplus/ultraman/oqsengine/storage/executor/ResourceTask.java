package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.SQLException;

/**
 * 表示一个任务.
 *
 * @param <RES> 返回结果.
 * @author dongbin
 * @version 0.1 2020/2/17 15:22
 * @since 1.8
 */
public interface ResourceTask<RES> {

    /**
     * 执行任务.
     *
     * @param transaction 相关事务.
     * @param resource    相关资源.
     * @param hint        执行器提示器.
     * @return 执行结果.
     * @throws SQLException
     */
    RES run(Transaction transaction, TransactionResource resource, ExecutorHint hint) throws SQLException;

    /**
     * 任务key.
     * @return 任务key.
     */
    default String key() {
        return "";
    }

}
