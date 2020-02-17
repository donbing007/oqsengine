package com.xforceplus.ultraman.oqsengine.storage.transaction;


import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * The main responsibilities are as follows.
 *
 * 1. Keep running transactions.
 *
 * 2. Restore the transaction to the thread context.
 *
 * @author dongbin
 * @version 0.1 2020/2/14 11:47
 * @since 1.8
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    private static final ThreadLocal<Transaction> CURRENT_TRANSACTION = new ThreadLocal<>();

    private ConcurrentMap<Long, Transaction> holder;

    public AbstractTransactionManager() {
        holder = new ConcurrentHashMap<>();
    }

    @Override
    public Transaction getCurrent() {
        return CURRENT_TRANSACTION.get();
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
    public void unbind(Transaction tx) {
        CURRENT_TRANSACTION.remove();
    }

}
