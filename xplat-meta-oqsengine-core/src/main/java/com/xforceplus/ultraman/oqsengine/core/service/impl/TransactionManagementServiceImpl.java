package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 事务管理.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:34
 * @since 1.8
 */
public class TransactionManagementServiceImpl implements TransactionManagementService {

    @Resource
    private TransactionManager transactionManager;

    @Override
    public long begin() throws SQLException {
        return begin(DEFAULT_TRANSACTION_TIMEOUT);
    }

    @Override
    public long begin(long timeoutMs) throws SQLException {
        long txId;

        if (DEFAULT_TRANSACTION_TIMEOUT == timeoutMs) {
            txId = transactionManager.create().id();
        } else if (timeoutMs > DEFAULT_TRANSACTION_TIMEOUT) {
            txId = transactionManager.create(timeoutMs).id();
        } else {
            throw new SQLException(
                String.format("%d is an invalid transaction timeout and must be an integer greater than 0.", timeoutMs));
        }

        return txId;
    }

    @Override
    public void restore(long id) throws SQLException {
        transactionManager.rebind(id);
    }

    @Override
    public void commit() throws SQLException {

        doFinish(false);
    }

    @Override
    public void rollback() throws SQLException {

        doFinish(true);

    }

    private void doFinish(boolean rollback) throws SQLException {
        Optional<Transaction> tx = transactionManager.getCurrent();
        if (tx.isPresent()) {

            try {
                if (!tx.get().isCompleted()) {
                    if (rollback) {
                        tx.get().rollback();
                    } else {
                        tx.get().commit();
                    }
                } else {
                    throw new SQLException(String.format("Transaction %d has completed.", tx.get().id()));
                }
            } finally {
                transactionManager.finish(tx.get());
            }
        } else {
            throw new SQLException("There are no transactions currently.");
        }
    }
}
