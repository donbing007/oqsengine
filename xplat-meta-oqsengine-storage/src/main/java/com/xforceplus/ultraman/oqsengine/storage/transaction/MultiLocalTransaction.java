package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.DoNothingEventBus;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.RollbackPayload;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.DefaultTransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
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
    private CommitIdStatusService commitIdStatusService;
    private long maxWaitCommitIdSyncMs;
    private TransactionAccumulator accumulator;
    private String msg;
    private EventBus eventBus;
    // 每一次检查不通过的等待时间.
    private final long checkCommitIdSyncMs = 5;
    /**
     * 一个标记,最终提交时是否进行过等待提交号同步.
     */
    private boolean waitedSync = false;

    private long startMs;

    private MultiLocalTransaction() {
    }

    private void init() {
        startMs = System.currentTimeMillis();
        transactionResourceHolder = new LinkedList<>();
        this.accumulator = new DefaultTransactionAccumulator();

        eventBus.notify(
            new ActualEvent(
                EventType.TX_BEGIN,
                new BeginPayload(id, msg)));
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Optional<String> message() {
        return Optional.ofNullable(msg);
    }

    @Override
    public synchronized void commit() throws SQLException {
        check();

        try {
            long commitId = 0;
            if (!isReadyOnly()) {
                commitId = longIdGenerator.next();
                if (!CommitHelper.isLegal(commitId)) {
                    throw new SQLException(String.format("The submission number obtained is invalid.[%d]", commitId));
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("To commit the transaction ({}), a new commit id ({}) is prepared.", id, commitId);
                }

                eventBus.notify(
                    new ActualEvent(EventType.TX_PREPAREDNESS_COMMIT,
                        new CommitPayload(id, commitId, msg, false)));

                /**
                 * 主库事务为主事务,成功与否决定了OQS事务是否成功.
                 * 索引事务不会影响OQS事务的成功与否,如果索引事务发生错误最终由CDC来最终达成一致.
                 */

                // 主库提交.
                for (TransactionResource tr : transactionResourceHolder) {
                    if (tr.type() == TransactionResourceType.MASTER) {
                        tr.commit(commitId);
                    }
                }

                // ===========之后的操作不能影响最终OQS事务的成功返回事实.===================
                try {
                    // 索引提交,如果提交错误也不影响事务成功.
                    for (TransactionResource tr : transactionResourceHolder) {
                        if (tr.type() == TransactionResourceType.INDEX) {
                            try {
                                tr.commit(commitId);
                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }
                    }

                    commitIdStatusService.save(commitId, true);

                    /**
                     * 事务中存在更新操作,需要等待提交号同步.
                     */
                    if (accumulator.getReplaceNumbers() > 0 || accumulator.getDeleteNumbers() > 0) {
                        waitedSync = true;
                        long waitMs = awitCommitSync(commitId);

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "The transaction {} contains an update operation, the wait commit number {} " +
                                    "synchronizes successfully. Wait {} milliseconds.",
                                id, commitId, waitMs);
                        }
                    } else {

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "Transaction {} has no update operation, no need to wait for " +
                                    "the commit number {} to synchronize successfully.",
                                id, commitId
                            );
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }

                eventBus.notify(
                    new ActualEvent(EventType.TX_COMMITED,
                        new CommitPayload(id, commitId, msg, false)));
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction {} is a read-only transaction and does not require a commit.", id);
                }

                eventBus.notify(
                    new ActualEvent(EventType.TX_PREPAREDNESS_COMMIT,
                        new CommitPayload(id, commitId, msg, true)));

            }
        } finally {
            doEnd(true);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ({}), commit.", id);
        }
    }

    @Override
    public synchronized void rollback() throws SQLException {
        check();

        eventBus.notify(
            new ActualEvent(EventType.TX_PREPAREDNESS_ROLLBACK,
                new RollbackPayload(id, msg)));

        try {
            List<SQLException> exHolder = new ArrayList<>(transactionResourceHolder.size());
            for (TransactionResource transactionResource : transactionResourceHolder) {
                try {
                    transactionResource.rollback();
                } catch (SQLException ex) {
                    exHolder.add(ex);
                }
            }

            throwSQLExceptionIfNecessary(exHolder);

            eventBus.notify(
                new ActualEvent(EventType.TX_ROLLBACKED,
                    new RollbackPayload(id, msg)));
        } finally {
            doEnd(false);
        }

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
        return (accumulator.getBuildNumbers() + accumulator.getReplaceNumbers() + accumulator.getDeleteNumbers()) == 0;
    }

    @Override
    public TransactionAccumulator getAccumulator() {
        return this.accumulator;
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

    public boolean isWaitedSync() {
        return waitedSync;
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

        for (TransactionResource tr : transactionResourceHolder) {
            try {
                tr.destroy();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        this.accumulator.reset();

        Metrics.timer(MetricsDefine.TRANSACTION_DURATION_SECONDS).record(
            System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
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

            throw new SQLException(message.toString(), sqlStatue.toString());
        }
    }


    public static final class Builder {
        private long id;
        private boolean committed = false;
        private boolean rollback = false;
        private LongIdGenerator longIdGenerator;
        private CommitIdStatusService commitIdStatusService;
        private long maxWaitCommitIdSyncMs;
        private String msg;
        private EventBus eventBus = DoNothingEventBus.getInstance();

        private Builder() {
        }

        public static Builder aMultiLocalTransaction() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withCommitted(boolean committed) {
            this.committed = committed;
            return this;
        }

        public Builder withRollback(boolean rollback) {
            this.rollback = rollback;
            return this;
        }

        public Builder withLongIdGenerator(LongIdGenerator longIdGenerator) {
            this.longIdGenerator = longIdGenerator;
            return this;
        }

        public Builder withCommitIdStatusService(CommitIdStatusService commitIdStatusService) {
            this.commitIdStatusService = commitIdStatusService;
            return this;
        }

        public Builder withMaxWaitCommitIdSyncMs(long maxWaitCommitIdSyncMs) {
            this.maxWaitCommitIdSyncMs = maxWaitCommitIdSyncMs;
            return this;
        }

        public Builder withMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public MultiLocalTransaction build() {
            MultiLocalTransaction multiLocalTransaction = new MultiLocalTransaction();
            multiLocalTransaction.committed = this.committed;
            multiLocalTransaction.msg = this.msg;
            multiLocalTransaction.rollback = this.rollback;
            multiLocalTransaction.longIdGenerator = this.longIdGenerator;
            multiLocalTransaction.id = this.id;
            multiLocalTransaction.commitIdStatusService = this.commitIdStatusService;
            multiLocalTransaction.eventBus = this.eventBus;
            multiLocalTransaction.maxWaitCommitIdSyncMs = this.maxWaitCommitIdSyncMs;
            multiLocalTransaction.init();
            return multiLocalTransaction;
        }
    }
}
