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
import com.xforceplus.ultraman.oqsengine.storage.transaction.hint.DefaultTransactionHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.hint.TransactionHint;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static AtomicLong COMMIT_ID_NUMBER = Metrics.gauge(MetricsDefine.NOW_COMMITID, new AtomicLong(0));

    // 每一次检查不通过的等待时间.
    private final long checkCommitIdSyncMs = 5;

    /**
     * 提交号生成时的命名空间.
     * 此值用以和其他生成的ID序列区分开.
     */
    private static String COMMIT_ID_NS = "com.xforceplus.ultraman.oqsengine.common.id";

    /*
    如果此值为true表示即使累加器中没有数据写入也认为当前事务非只读事务.
    主要用以后台任务中,不需要真的对累加器进行更新.
     */
    private boolean notReadOnly;
    private boolean committed;
    private boolean rollback;
    private long id;
    private long attachment;
    private long maxWaitCommitIdSyncMs;
    private String msg;
    private Lock lock = new ReentrantLock();
    private LongIdGenerator longIdGenerator;
    private CommitIdStatusService commitIdStatusService;
    private TransactionAccumulator accumulator;
    private TransactionHint hint;
    private EventBus eventBus;
    private Collection<Consumer<Transaction>> commitHooks;
    private Collection<Consumer<Transaction>> rollbackHooks;
    private List<TransactionResource> transactionResourceHolder;
    /**
     * 一个标记,最终提交时是否进行过等待提交号同步.
     */
    private boolean waitedSync = false;
    /**
     * 记录事务开始的时间.
     */
    private long startMs;

    private MultiLocalTransaction() {
    }

    private void init() {
        startMs = System.currentTimeMillis();
        transactionResourceHolder = new LinkedList<>();
        this.accumulator = new DefaultTransactionAccumulator(id);
        this.hint = new DefaultTransactionHint();

        if (eventBus == null) {
            eventBus = DoNothingEventBus.getInstance();
        } else {
            eventBus.notify(
                new ActualEvent(
                    EventType.TX_BEGIN,
                    new BeginPayload(id, msg)));
        }
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
                commitId = longIdGenerator.next(COMMIT_ID_NS);
                if (!CommitHelper.isLegal(commitId)) {
                    throw new SQLException(String.format("The submission number obtained is invalid.[%d]", commitId));
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("To commit the transaction ({}), a new commit id ({}) is prepared.", id, commitId);
                }

                // 当前提交号记录.
                COMMIT_ID_NUMBER.set(commitId);

                eventBus.notify(
                    new ActualEvent(EventType.TX_PREPAREDNESS_COMMIT,
                        new CommitPayload(id, commitId, msg, false, this.getAccumulator().operationNumber())));

                /*
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

                    /*
                    以下会对提交号进行操作,要尽可能保证不能阻塞.
                    因为主库已经提交,对象信息已经生效.单个对象的查询已经可以找到最新值.
                    */
                    commitIdStatusService.save(commitId, true);

                    /*
                    判断当前是否需要等待CDC同步当前提交号.
                     */
                    awitCommitSync(commitId);

                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }

                eventBus.notify(
                    new ActualEvent(EventType.TX_COMMITED,
                        new CommitPayload(id, commitId, msg, false, this.getAccumulator().operationNumber())));
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction {} is a read-only transaction and does not require a commit.", id);
                }

                eventBus.notify(
                    new ActualEvent(EventType.TX_PREPAREDNESS_COMMIT,
                        new CommitPayload(id, commitId, msg, true, this.getAccumulator().operationNumber())));

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
                new RollbackPayload(id, getAccumulator().operationNumber(), msg)));

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
                    new RollbackPayload(id, getAccumulator().operationNumber(), msg)));

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
    public Optional<TransactionResource> queryTransactionResource(String key) {

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
    public Collection<TransactionResource> listTransactionResource(TransactionResourceType type) {
        return transactionResourceHolder.stream().filter(r -> r.type() == type).collect(Collectors.toList());
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
        return notReadOnly
            ? false
            : (accumulator.getBuildNumbers() + accumulator.getReplaceNumbers() + accumulator.getDeleteNumbers()) == 0;
    }

    @Override
    public void focusNotReadOnly() {
        this.notReadOnly = true;
    }

    @Override
    public TransactionAccumulator getAccumulator() {
        return this.accumulator;
    }

    @Override
    public TransactionHint getHint() {
        return this.hint;
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

    @Override
    public synchronized void registerCommitHook(Consumer<Transaction> hook) {
        if (commitHooks == null) {
            commitHooks = new LinkedList<>();
        }

        commitHooks.add(hook);
    }

    @Override
    public synchronized void registerRollbackHook(Consumer<Transaction> hook) {
        if (rollbackHooks == null) {
            rollbackHooks = new LinkedList<>();
        }

        rollbackHooks.add(hook);
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
        final StringBuffer sb = new StringBuffer("MultiLocalTransaction{");
        sb.append("id=").append(id);
        sb.append(", attachment=").append(attachment);
        sb.append(", committed=").append(committed);
        sb.append(", rollback=").append(rollback);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private void check() throws SQLException {
        if (isCompleted()) {
            throw new SQLException(
                String.format("The transaction has completed.[id=%d, commit=%b, rollback=%b]",
                    id(), isCommitted(), isRollback()));
        }
    }

    private void doEnd(boolean commit) throws SQLException {

        if (commit) {

            committed = true;

            doHooks(commitHooks);

        } else {

            rollback = true;

            doHooks(rollbackHooks);
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

    // 执行hook.
    private void doHooks(Collection<Consumer<Transaction>> hooks) {
        if (hooks != null && !hooks.isEmpty()) {
            hooks.forEach(a -> {
                try {
                    a.accept(this);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            });
        }
    }

    // 等待提交号被同步成功或者超时.
    private void awitCommitSync(long commitId) {

        Timer.Sample sample = Timer.start(Metrics.globalRegistry);

        try {

            /*
            如果外部指定最大等待时间小于等于0,表示需要提交号CDC等待.
            这时即使当前事务声明需要等待也不会进行等待.
            只有全局设定为需要等待,同时当前事务也提示需要等待时才会进行等待.
            最多等待 maxWaitCommitIdSyncMs 毫秒.
            */
            if (!hint.isCanWaitCommitSync() || maxWaitCommitIdSyncMs <= 0) {
                return;
            } else {
                this.waitedSync = true;
            }

            int maxLoop = 1;
            if (maxWaitCommitIdSyncMs > checkCommitIdSyncMs) {
                maxLoop = (int) (maxWaitCommitIdSyncMs / checkCommitIdSyncMs);
            }

            for (int i = 0; i < maxLoop; i++) {

                if (commitIdStatusService.isObsolete(commitId)) {

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "The transaction {} contains an update operation, the wait commit number {} synchronizes successfully. Wait {} milliseconds.",
                            id, commitId, i * checkCommitIdSyncMs);
                    }
                    return;

                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("The commit number {} has not been phased out, wait {} milliseconds.",
                            commitId, checkCommitIdSyncMs);
                    }

                    LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(checkCommitIdSyncMs));
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "The transaction {} contains an update operation, the wait commit number {} synchronizes successfully. Wait {} milliseconds.",
                    id, commitId, maxWaitCommitIdSyncMs);
            }
        } finally {

            sample.stop(Timer.builder(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS)
                .tags(
                    "initiator", "transaction",
                    "action", "wait",
                    "exception", "none"
                )
                .publishPercentileHistogram(false)
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(Metrics.globalRegistry));

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

            throw new SQLException(message.toString(), sqlStatue.toString());
        }
    }

    /**
     * builder.
     */
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

        public static Builder anMultiLocalTransaction() {
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

        /**
         * 构造实例.
         *
         * @return 实例.
         */
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
