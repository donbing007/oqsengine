package com.xforceplus.ultraman.oqsengine.storage.transaction;


import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimeoutNotification;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimerWheel;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The main responsibilities are as follows.
 * <p>
 * 1. Keep running transactions.
 * <p>
 * 2. Restore the transaction to the thread context.
 *
 * @author dongbin
 * @version 0.1 2020/2/14 11:47
 * @since 1.8
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    final Logger logger = LoggerFactory.getLogger(AbstractTransactionManager.class);

    private AtomicLong transactionNumber = Metrics.gauge(MetricsDefine.TRANSACTION_COUNT, new AtomicLong(0));

    /**
     * 当前是否处于冻结状态.true 是, false 不是.
     */
    private AtomicBoolean frozenness = new AtomicBoolean(false);

    /**
     * 允许的早小事务超时,毫秒.
     */
    private static final int MIN_TRANSACTION_LIVE_TIME_MS = 200;

    /**
     * 事务存活持有,当事务被创建后将在这持有.
     * key 为 事务 id.
     */
    private ConcurrentMap<Long, Transaction> survival;

    /**
     * 当前线程绑定持有,key 为线程 id.
     * 在 bind/unbind/rebind,会操作此 map.
     */
    private ConcurrentMap<Long, Transaction> using;

    /**
     * 记录事务数量.基数量等于survival中存在的事务数量.
     */
    private AtomicInteger size = new AtomicInteger(0);

    /**
     * 事务的最大存活时间.(毫秒)
     */
    private int survivalTimeMs;

    /**
     * 时间轮.
     */
    private TimerWheel timerWheel;

    public AbstractTransactionManager() {
        this(3000);
    }

    public AbstractTransactionManager(int survivalTimeMs) {

        checkTimeout(survivalTimeMs);

        this.survivalTimeMs = survivalTimeMs;
        survival = new ConcurrentHashMap<>();
        using = new ConcurrentHashMap<>();

        timerWheel = new TimerWheel(new TransaxtionTimeoutNotification());
    }

    @Override
    public Transaction create() {
        return create(this.survivalTimeMs);
    }

    @Override
    public Transaction create(long timeoutMs) {
        if (frozenness.get()) {
            throw new IllegalStateException("Unable to create transaction, frozen.");
        }

        checkTimeout(timeoutMs);

        Transaction transaction = null;

        try {
            transaction = doCreate();

            survival.put(transaction.id(), transaction);

            timerWheel.add(transaction.id(), timeoutMs);

            transactionNumber.incrementAndGet();

            if (logger.isDebugEnabled()) {
                logger.debug("Start new Transaction({}),timeout will occur in {} milliseconds.", transaction.id(), timeoutMs);
            }

            this.bind(transaction);

        } catch (Exception ex) {

            /**
             * 在创建事务时发生意外错误时进行清理.
             */
            if (transaction != null) {
                try {
                    survival.remove(transaction.id());
                    timerWheel.remove(transaction.id());
                    this.unbind();
                } catch (Exception e) {
                    logger.warn("An error occurred in creating the transaction, as well as in cleaning it up.", e);
                }
            }

            if (RuntimeException.class.isInstance(ex)) {
                throw (RuntimeException) ex;
            }

        }

        try {

            return transaction;

        } finally {

            size.incrementAndGet();

        }
    }

    protected abstract Transaction doCreate();

    @Override
    public Optional<Transaction> getCurrent() {
        Transaction t = using.get(Thread.currentThread().getId());
        return Optional.ofNullable(t);
    }

    @Override
    public void rebind(long id) {
        Transaction tx = survival.get(id);
        if (tx == null) {
            throw new RuntimeException(String.format("Invalid transaction ID(%d), unable to bind the transaction.", id));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Try rebind transaction({}).", tx.id());
        }

        bind(tx);
    }

    @Override
    public void bind(Transaction tx) {

        long threadId = Thread.currentThread().getId();
        tx.attach(threadId);

        using.put(threadId, tx);

        if (logger.isDebugEnabled()) {
            logger.debug("Bind transaction({})", tx.id());
        }

    }

    @Override
    public void unbind() {

        long threadId = Thread.currentThread().getId();

        Transaction tx = using.remove(threadId);

        if (tx != null) {
            tx.attach(Transaction.NOT_ATTACHMENT);

            if (logger.isDebugEnabled()) {
                logger.debug("Unbound transaction({}).", tx.id());
            }
        }
    }

    @Override
    public void finish(Transaction tx) throws SQLException {

        if (!tx.isCompleted()) {
            tx.rollback();
        }

        survival.remove(tx.id());

        size.decrementAndGet();

        using.remove(tx.attachment());
        tx.attach(Transaction.NOT_ATTACHMENT);

        timerWheel.remove(tx.id());

        transactionNumber.decrementAndGet();

        if (logger.isDebugEnabled()) {
            logger.debug("End of transaction({}) and unbound.", tx.id());
        }

    }

    @Override
    public void finish() throws SQLException {
        Optional<Transaction> current = getCurrent();
        if (current.isPresent()) {
            finish(current.get());
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public void freeze() {
        frozenness.set(true);
    }

    @Override
    public void unfreeze() {
        frozenness.set(false);
    }

    class TransaxtionTimeoutNotification implements TimeoutNotification<Long> {

        @Override
        public long notice(Long transactionId) {
            Transaction transaction = survival.get(transactionId);
            if (transaction != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction ({}) timed out, so rollback.", transaction.id());
                }

                try {
                    finish(transaction);
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            return 0;
        }
    }

    private void checkTimeout(long timeout) {
        // 允许的最小事务存活时间.
        if (timeout < MIN_TRANSACTION_LIVE_TIME_MS) {
            throw new IllegalArgumentException(
                String.format("The transaction lifetime cannot be less than %d.", MIN_TRANSACTION_LIVE_TIME_MS));
        }
    }

}
