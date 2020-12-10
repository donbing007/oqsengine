package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
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
import java.util.concurrent.locks.LockSupport;
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

    private final Logger logger = LoggerFactory.getLogger(MultiLocalTransaction.class);

    private long id;
    private long attachment;
    private List<TransactionResource> transactionResourceHolder;
    private boolean committed;
    private boolean rollback;
    private Lock lock = new ReentrantLock();
    private LongIdGenerator longIdGenerator;
    private boolean writeTx = false;
    private CommitIdStatusService commitIdStatusService;
    private long maxWaitCommitIdSyncMs;
    // 每一次检查不通过的等待时间.
    private final long checkCommitIdSyncMs = 5;

    private Timer.Sample durationMetrics;

    public MultiLocalTransaction(long id, LongIdGenerator longIdGenerator, CommitIdStatusService commitIdStatusService) {
        this(id, longIdGenerator, commitIdStatusService, TimeUnit.MINUTES.toMillis(1));
    }

    public MultiLocalTransaction(
        long id, LongIdGenerator longIdGenerator, CommitIdStatusService commitIdStatusService, long maxWaitCommitIdSyncMs) {
        transactionResourceHolder = new LinkedList<>();
        committed = false;
        rollback = false;
        this.id = id;
        this.longIdGenerator = longIdGenerator;
        this.commitIdStatusService = commitIdStatusService;
        this.maxWaitCommitIdSyncMs = maxWaitCommitIdSyncMs;

        durationMetrics = Timer.start(Metrics.globalRegistry);
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public synchronized void commit() throws SQLException {
        check();

        doEnd(true);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), commit.", id);
        }
    }

    @Override
    public synchronized void rollback() throws SQLException {
        check();

        doEnd(false);

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
        return this.committed || this.rollback;
    }

    @Override
    public synchronized void join(TransactionResource transactionResource) throws SQLException {
        check();

        transactionResourceHolder.add(transactionResource);
        transactionResource.bind(this);
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

    @Override
    public boolean isReadyOnly() {
        return !writeTx;
    }

    @Override
    public void declareWriteTransaction() {
        writeTx = true;
    }

    @Override
    public void exclusiveAction(TransactionExclusiveAction action) throws SQLException {
        lock.lock();
        try {
            logger.debug("Starts the exclusive operation of transaction({}).", this.id());
            action.act();
        } finally {
            lock.unlock();
            logger.debug("The exclusive operation of the transaction({}) ends.", this.id());
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
            committed = false;

            logger.error(message.toString());

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
        try {
            List<SQLException> exHolder = new LinkedList<>();
            if (commit) {
                long commitId = 0;
                if (!isReadyOnly()) {
                    commitId = longIdGenerator.next();
                    if (!CommitHelper.isLegal(commitId)) {
                        throw new SQLException(String.format("The submission number obtained is invalid.[%d]", commitId));
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("To commit the transaction ({}), a new commit id ({}) is prepared.", id, commitId);
                    }

                    boolean commitEx = false;
                    for (TransactionResource transactionResource : transactionResourceHolder) {
                        try {
                            if (commitEx) {
                                transactionResource.rollback();
                            } else {
                                transactionResource.commit(commitId);
                            }
                        } catch (SQLException ex) {
                            exHolder.add(0, ex);
                            commitEx = true;
                        }
                        transactionResource.destroy();
                    }
                    if (exHolder.isEmpty()) {

                        commitIdStatusService.save(commitId, true);

                        long waitMs = awitCommitSync(commitId);
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "Transaction {}, commit number {} wait for commit number to confirm {} milliseconds.",
                                id, commitId, waitMs);
                        }

                    }

                } else {
                    // 只读事务提交.
                    for (TransactionResource transactionResource : transactionResourceHolder) {
                        try {
                            transactionResource.commit();
                        } catch (SQLException ex) {
                            exHolder.add(0, ex);
                        }
                        transactionResource.destroy();
                    }
                }
            } else {

                // 回滚
                for (TransactionResource transactionResource : transactionResourceHolder) {
                    try {
                        transactionResource.rollback();
                    } catch (SQLException ex) {
                        exHolder.add(0, ex);
                    }
                    transactionResource.destroy();
                }
            }

            throwSQLExceptionIfNecessary(exHolder);
        } finally {
            durationMetrics.stop(Metrics.globalRegistry.timer(MetricsDefine.TRANSACTION_DURATION_SECONDS));
        }
    }

    // 等待提交号被同步成功或者超时.
    private long awitCommitSync(long commitId) {

        if (maxWaitCommitIdSyncMs <= 0) {
            return 0;
        }

        int maxLoop = 1;
        if (maxWaitCommitIdSyncMs > checkCommitIdSyncMs) {
            maxLoop = (int) (maxWaitCommitIdSyncMs / checkCommitIdSyncMs);
        }

        for (int i = 0; i < maxLoop; i++) {

            if (commitIdStatusService.isObsolete(commitId)) {

                return i * checkCommitIdSyncMs;

            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("The commit number {} has not been phased out, wait {} milliseconds.",
                        commitId, checkCommitIdSyncMs);
                }

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkCommitIdSyncMs));
            }
        }

        return maxWaitCommitIdSyncMs;
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
