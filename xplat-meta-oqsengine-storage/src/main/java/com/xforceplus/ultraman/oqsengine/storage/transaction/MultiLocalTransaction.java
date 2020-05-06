package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Simple multi-local transaction implementation ensures atomicity before the commit,
 * but there is no guarantee of atomicity in the event that a commit produces an error.
 * Thread safety.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:47
 * @since 1.8
 */
public class MultiLocalTransaction implements Transaction {

    final Logger logger = LoggerFactory.getLogger(MultiLocalTransaction.class);

    private Timer.Sample timerSample;

    private long id;
    private long attachment;
    private List<TransactionResource> transactionResourceHolder;
    private boolean committed;
    private boolean rollback;

    public MultiLocalTransaction(long id) {
        transactionResourceHolder = new LinkedList<>();
        committed = false;
        rollback = false;
        this.id = id;

        timerSample = Timer.start(Metrics.globalRegistry);
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public synchronized void commit() throws SQLException {
        check();

//        doEnd(true);

        doEndWithUndo(true);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), commit.", id);
        }
    }

    @Override
    public synchronized void rollback() throws SQLException {
        check();

//            doEnd(false);
        doEndWithUndo(false);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), rollback.", id);
        }
    }

    @Override
    public synchronized boolean isCommitted() {
        return committed;
    }

    @Override
    public synchronized boolean isRollback() {
        return rollback;
    }

    @Override
    public synchronized boolean isCompleted() {
        if (this.committed || this.rollback) {
            return true;
        }

        return false;
    }

    @Override
    public synchronized void join(TransactionResource transactionResource) throws SQLException {
        check();

        transactionResourceHolder.add(0, transactionResource);
    }

    @Override
    public Optional<TransactionResource> query(Object key) {

        if (isCompleted()) {
            return Optional.empty();
        }

        for (TransactionResource res : transactionResourceHolder) {
            if (res.key().equals(key)) {
                return Optional.of(res);
            }
        }

        return Optional.empty();
    }

    @Override
    public long attachment() {
        return attachment;
    }

    @Override
    public void attach(long id) {
        this.attachment = id;
    }

    private void doEnd(boolean commit) throws SQLException {
        try {
            List<SQLException> exHolder = new LinkedList<>();
            for (TransactionResource transactionResource : transactionResourceHolder) {
                try {
                    if (commit) {
                        transactionResource.commit();
                    } else {
                        transactionResource.rollback();
                    }

                    transactionResource.destroy();
                } catch (SQLException ex) {
                    exHolder.add(0, ex);

                    //TODO: 发生了异常,需要 rollback, 这里需要 undo 日志.by dongbin 2020/02/17

                }

            }

            throwSQLExceptionIfNecessary(exHolder);

            if (commit) {

                committed = true;

            } else {

                rollback = true;

            }
        } finally {
            timerSample.stop(Metrics.globalRegistry.timer(MetricsDefine.TRANSACTION_DURATION_SECONDS));
        }

    }

    private void throwSQLExceptionIfNecessary(List<SQLException> exHolder) throws SQLException {
        if (!exHolder.isEmpty()) {
            StringBuilder sqlStatue = new StringBuilder();
            StringBuilder message = new StringBuilder();
            for (SQLException ex : exHolder) {
                sqlStatue.append("\"").append(ex.getSQLState()).append("\" ");
                message.append("\"").append(ex.getMessage()).append("\" ");

            }

            // commit 或者 rollback 的异常都将标示为 rollback 状态.
            rollback = true;

            throw new SQLException(message.toString(), sqlStatue.toString());
        }
    }

    private void check() throws SQLException {
        if (isCompleted()) {
            throw new SQLException(
                    String.format("The transaction has completed.[commit=%b, rollback=%b]", isCommitted(), isRollback()));
        }
    }

    private void doEndWithUndo(boolean commit) throws SQLException {
        try {
            List<SQLException> exHolder = new LinkedList<>();
            try {
                saveTransactionResourcesUndoLog(transactionResourceHolder);

                for (TransactionResource transactionResource : transactionResourceHolder) {
                    if (commit) {
                        transactionResource.commit();
                    } else {
                        transactionResource.rollback();
                    }
                }
            } catch (SQLException ex) {
                exHolder.add(0, ex);

                undoTransactionResources(transactionResourceHolder, commit);
            } finally {
                destroyTransactionResources(transactionResourceHolder);
            }

            throwSQLExceptionIfNecessary(exHolder);

            if (commit) {

                committed = true;

            } else {

                rollback = true;

            }
        } finally {
            timerSample.stop(Metrics.globalRegistry.timer(MetricsDefine.TRANSACTION_DURATION_SECONDS));
        }
    }

    private void saveTransactionResourcesUndoLog(List<TransactionResource> transactionResourceHolder) {
        for (TransactionResource transactionResource : transactionResourceHolder) {
            ((UndoTransactionResource) transactionResource).createUndoLog(id);
        }
    }

    private void undoTransactionResources(List<TransactionResource> transactionResourceHolder, boolean commit) throws SQLException {
        logger.debug("start to rollback or undo commit");
        for (TransactionResource transactionResource : transactionResourceHolder) {
            logger.debug("transacitonResource {} undo", transactionResource.key());
            transactionResource.undo(commit);
        }
        logger.debug("finish to rollback or undo commit");
    }

    private void destroyTransactionResources(List<TransactionResource> transactionResourceHolder) {
        logger.debug("clear transactionResource");
        for (TransactionResource transactionResource : transactionResourceHolder) {
            try {
                if (!transactionResource.isDestroyed()) {
                    transactionResource.destroy();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
