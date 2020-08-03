package com.xforceplus.ultraman.oqsengine.storage.transaction;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

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
    private volatile boolean committed;
    private volatile boolean rollback;


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
    public void commit() throws SQLException {
        check();

        doEnd(true);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), commit.", id);
        }
    }

    @Override
    public void rollback() throws SQLException {
        check();

        doEnd(false);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), rollback.", id);
        }
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public boolean isRollback() {
        return rollback;
    }

    @Override
    public boolean isCompleted() {
        return this.committed || this.rollback;
    }

    @Override
    public void join(TransactionResource transactionResource) throws SQLException {
        check();

        transactionResourceHolder.add(0, transactionResource);
    }

    @Override
    public Optional<TransactionResource> query(String key) {

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

    private void doEnd(boolean commit) throws SQLException {
        if (commit) {

            committed = true;

        } else {

            rollback = true;

        }
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MultiLocalTransaction)) {
            return false;
        }
        MultiLocalTransaction that = (MultiLocalTransaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MultiLocalTransaction{" +
            "id=" + id +
            ", attachment=" + attachment +
            ", committed=" + committed +
            ", rollback=" + rollback +
            '}';
    }
}
