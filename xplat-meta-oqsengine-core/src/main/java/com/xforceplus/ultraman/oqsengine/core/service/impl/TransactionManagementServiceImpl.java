package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    final Logger logger = LoggerFactory.getLogger(TransactionManagementServiceImpl.class);

    @Resource
    private TransactionManager transactionManager;

    @Override
    public long begin() throws SQLException {
        return begin(DEFAULT_TRANSACTION_TIMEOUT);
    }

    @Override
    public long begin(long timeoutMs) throws SQLException {
        return begin(timeoutMs, null);
    }

    @Override
    public long begin(long timeoutMs, String msg) throws SQLException {
        long txId;

        try {
            if (DEFAULT_TRANSACTION_TIMEOUT == timeoutMs) {
                txId = transactionManager.create(msg).id();
            } else if (timeoutMs > DEFAULT_TRANSACTION_TIMEOUT) {
                txId = transactionManager.create(timeoutMs, msg).id();
            } else {
                throw new SQLException(
                    String.format("%d is an invalid transaction timeout and must be an integer greater than 0.", timeoutMs));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }

        return txId;
    }

    @Override
    public void restore(long id) throws SQLException {
        try {
            transactionManager.bind(id);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void commit() throws SQLException {

        doFinish(false);
    }

    @Override
    public void rollback() throws SQLException {

        doFinish(true);

    }

    /**
     * 事务的结束操作,需要保证此操作之间不能中断和原子性.
     * 因为实际会有两步操作,主要 commit/rollback中间或者和最后清理中间被认为超时清理后会产生错误的事务状态.
     * 比如数据无法回滚等严重问题.
     */
    private void doFinish(boolean rollback) throws SQLException {
        Optional<Transaction> tx = transactionManager.getCurrent();
        try {
            if (tx.isPresent()) {
                Transaction t = tx.get();
                t.exclusiveAction(() -> {
                    try {
                        if (!t.isCompleted()) {
                            if (rollback) {
                                t.rollback();
                            } else {
                                t.commit();
                            }
                        } else {
                            throw new SQLException(String.format("Transaction %d has completed.", t.id()));
                        }
                    } finally {
                        transactionManager.finish(t);
                    }
                });

            } else {
                throw new SQLException("There are no transactions currently.");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
