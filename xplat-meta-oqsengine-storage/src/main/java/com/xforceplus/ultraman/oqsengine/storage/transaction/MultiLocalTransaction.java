package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Simple multi-local transaction implementation ensures atomicity before the commit,
 * but there is no guarantee of atomicity in the event that a commit produces an error.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:47
 * @since 1.8
 */
public class MultiLocalTransaction implements Transaction {

    private long id;
    private List<TransactionResource> transactionResourceHolder;
    private boolean committed;
    private boolean rollback;

    public MultiLocalTransaction(long id) {
        transactionResourceHolder = new LinkedList<>();
        committed = false;
        rollback = false;
        this.id = id;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public void commit() throws SQLException {
        doEnd(true);
    }

    @Override
    public void rollback() throws SQLException {

        doEnd(false);
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
        return (committed || rollback) ? true : false;
    }

    @Override
    public void join(TransactionResource transactionResource) {


        transactionResourceHolder.add(0, transactionResource);
    }

    @Override
    public Optional<TransactionResource> query(Object key) {
        for (TransactionResource res : transactionResourceHolder) {
            if (res.key().equals(key)) {
                return Optional.of(res);
            }
        }

        return Optional.empty();
    }

    private void doEnd(boolean commit) throws SQLException {
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

            commit = true;

        } else {

            rollback = true;

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
}
