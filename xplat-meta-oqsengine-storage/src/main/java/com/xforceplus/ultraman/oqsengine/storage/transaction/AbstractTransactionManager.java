package com.xforceplus.ultraman.oqsengine.storage.transaction;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private static final ThreadLocal<Transaction> CURRENT_TRANSACTION = new ThreadLocal<>();

    private ConcurrentMap<Long, Transaction> holder;

    public AbstractTransactionManager() {
        holder = new ConcurrentHashMap<>();
    }

    @Override
    public Transaction create() {
        Transaction newT = doCreate();

        if (logger.isDebugEnabled()) {
            logger.debug("Start new Transaction({}).", newT.id());
        }

        return newT;
    }

    protected abstract Transaction doCreate();

    @Override
    public Optional<Transaction> getCurrent() {
        Transaction t = CURRENT_TRANSACTION.get();
        return Optional.ofNullable(t);
    }

    @Override
    public void rebind(long id) {
        Transaction tx = holder.get(id);
        if (tx == null) {
            throw new RuntimeException(String.format("Invalid transaction ID(%d), unable to bind the transaction.", id));
        }

        CURRENT_TRANSACTION.set(tx);
    }

    @Override
    public void bind(Transaction tx) {

        CURRENT_TRANSACTION.set(tx);

        holder.put(tx.id(), tx);
    }

    @Override
    public void unbind() {
        CURRENT_TRANSACTION.remove();
    }

    @Override
    public void finish(Transaction tx) throws SQLException {

        if (!tx.isCompleted()) {
            tx.rollback();
        }

        holder.remove(tx.id());

        if (logger.isDebugEnabled()) {
            logger.debug("End of transaction({}).", tx.id());
        }


        // 如果完成的本线程事务,那么解除绑定.
        Optional<Transaction> current = getCurrent();
        if (current.isPresent()) {
            if (current.get().id() == tx.id()) {
                unbind();
            }
        }
    }

    @Override
    public void finish() throws SQLException {
        Optional<Transaction> current = getCurrent();
        if (current.isPresent()) {
            finish(current.get());
        }
    }

}
