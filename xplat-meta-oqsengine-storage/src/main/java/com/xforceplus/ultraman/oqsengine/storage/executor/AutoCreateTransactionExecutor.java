package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 自动创建事务的执行器.
 * 如果没有全局事务,那么将创建一个局部事务.
 * 如果是局部事务,那么将在方法结束前提交或者回滚.
 * 默认为异常回滚,除非任务中明确提示需要回滚.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 21:30
 * @since 1.8
 */
public class AutoCreateTransactionExecutor implements TransactionExecutor {

    private TransactionManager transactionManager;

    public AutoCreateTransactionExecutor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object execute(Task task) throws SQLException {
        boolean localTx;
        Optional<Transaction> txOptional = transactionManager.getCurrent();
        Transaction tx;
        if (!txOptional.isPresent()) {
            tx = transactionManager.create();
            localTx = true;
        } else {
            tx = txOptional.get();
            localTx = false;
        }

        ExecutorHint hint = new DefaultExecutorHint();
        try {

            Object res = task.run(null, hint);

            if (localTx) {

                if (hint.isRollback()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }

            }

            return res;

        } catch (Exception ex) {

            if (localTx) {
                tx.rollback();
            }

            throw ex;

        } finally {

            if (localTx) {
                transactionManager.finish(tx);
            } else {
                transactionManager.unbind();
            }

        }

    }
}
