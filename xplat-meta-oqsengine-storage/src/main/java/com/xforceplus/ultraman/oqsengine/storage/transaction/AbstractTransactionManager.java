package com.xforceplus.ultraman.oqsengine.storage.transaction;


import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.ITimerWheel;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.MultipleTimerWheel;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimeoutNotification;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务管理器抽像实现.
 * 事务分为创建,绑定,反绑定和提交几步.
 * 其中绑定和反绑定一定要重复出来.
 *
 * <p>TransactionManager tm = ...
 * Transaction tx = tm.create();
 * tm.bind(tx.id());
 * tru {
 * //...逻辑
 * } finally {
 * tm.unbind();
 * }
 *
 * <p>rebind 在持主事务 id 的时候可以还原之前其他线程创建的事务现场.
 *
 * @author dongbin
 * @version 0.2 2020/7/2 13:58 现在事务在创建后不会直接绑定.
 * @since 1.8
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    final Logger logger = LoggerFactory.getLogger(AbstractTransactionManager.class);

    private AtomicLong transactionNumber = Metrics.gauge(MetricsDefine.TRANSACTION_COUNT, new AtomicLong(0));

    /**
     * 当前是否处于冻结状态.true 是, false 不是.
     */
    private volatile boolean frozenness = false;

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
     * 时间轮.处理事务超时.
     */
    private ITimerWheel<Transaction> timerWheel;

    public AbstractTransactionManager() {
        this(3000);
    }

    /**
     * 实例化.
     *
     * @param survivalTimeMs 生存时间.
     */
    public AbstractTransactionManager(int survivalTimeMs) {

        checkTimeout(survivalTimeMs);

        this.survivalTimeMs = survivalTimeMs;
        survival = new ConcurrentHashMap<>();
        using = new ConcurrentHashMap<>();

        timerWheel = new MultipleTimerWheel(new TransaxtionTimeoutNotification());
    }

    @Override
    public Transaction create() {
        return create(this.survivalTimeMs);
    }

    @Override
    public Transaction create(String message) {
        return create(this.survivalTimeMs, message);
    }

    @Override
    public Transaction create(long timeoutMs) {
        return create(timeoutMs, null);
    }

    @Override
    public Transaction create(long timeoutMs, String message) {
        if (frozenness) {
            throw new IllegalStateException("Unable to create transaction, frozen.");
        }

        checkTimeout(timeoutMs);

        Transaction transaction = null;

        try {
            transaction = doCreate(message);

            survival.put(transaction.id(), transaction);

            timerWheel.add(transaction, timeoutMs);

            transactionNumber.incrementAndGet();

            if (logger.isDebugEnabled()) {
                logger.debug("Start new Transaction({}),timeout will occur in {} milliseconds.", transaction.id(),
                    timeoutMs);
            }


        } catch (Exception ex) {

            /*
             * 在创建事务时发生意外错误时进行清理.
             */
            if (transaction != null) {
                try {
                    clean(transaction);
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

    protected abstract Transaction doCreate(String msg);

    @Override
    public Optional<Transaction> getCurrent() {
        Transaction t = using.get(Thread.currentThread().getId());
        return Optional.ofNullable(t);
    }

    @Override
    public void bind(long id) {

        Transaction tx = survival.get(id);
        if (tx == null) {
            throw new RuntimeException(String.format("Invalid transaction(%s), transaction may have timed out.", id));
        }

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
        size.decrementAndGet();
        transactionNumber.decrementAndGet();

        clean(tx);


        if (!tx.isCompleted()) {
            tx.rollback();
        }

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
        frozenness = true;
    }

    @Override
    public void unfreeze() {
        frozenness = false;
    }

    // 清理
    private void clean(Transaction tx) {
        using.remove(tx.attachment());
        survival.remove(tx.id());
        timerWheel.remove(tx);
        tx.attach(Transaction.NOT_ATTACHMENT);
    }

    /**
     * 事务的超时处理逻辑.
     * 需要保证事务正在使用中,即处在 using 中的事务不可清理.
     * 如果事务超时但是处在 using 中,那么会再主动设置下一个检查点,固定为300ms.
     * 这是为了尽量在事务处于非
     */
    class TransaxtionTimeoutNotification implements TimeoutNotification<Transaction> {

        /**
         * 正在使用的过期事务的再次检查毫秒值.
         */
        final int recheckMs = 600;

        @Override
        public long notice(Transaction transaction) {
            /*
             * 进入这里表示事务已经超时,不能再被bind.所以直接从生存列表中移除.
             * 阻止 bind 成功,但是不影响已经 bind 的事务.
             */
            survival.remove(transaction.id());

            /*
             * 当出现在 using 中,那么表示事务正在被使用.
             * 等待处于非使用中的时候进行过期,但是此事务不能再进行bind了.
             */
            if (using.containsKey(transaction.attachment())) {

                logger.warn("The transaction ({}) timed out,but still in use will be checked at the next checkpoint.",
                    transaction.id());

                return recheckMs;

            } else {

                try {

                    transaction.exclusiveAction(() -> {
                        if (!transaction.isCompleted()) {
                            logger.warn("The transaction ({}) timed out, so rollback.", transaction.id());
                            finish(transaction);

                        }
                    });
                } catch (Exception e) {
                    String msg = String.format("%s transaction status: %s", e.getMessage(), transaction.toString());
                    logger.error(msg, e);
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
