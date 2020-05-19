package com.xforceplus.ultraman.oqsengine.storage.transaction.resource;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.SQLException;

/**
 * 所有事务资源的抽像.
 * @author dongbin
 * @version 0.1 2020/5/19 10:35
 * @since 1.8
 */
public abstract class AbstractTransactionResource<T> implements TransactionResource<T> {

    private String key;
    private T value;

    public AbstractTransactionResource(String key, T value) throws SQLException {
        this.key = key;
        this.value = value;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public T value() {
        return value;
    }
}
