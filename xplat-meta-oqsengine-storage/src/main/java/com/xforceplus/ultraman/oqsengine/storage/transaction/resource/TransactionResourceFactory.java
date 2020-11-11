package com.xforceplus.ultraman.oqsengine.storage.transaction.resource;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.SQLException;

/**
 * 事务资源创建工厂.
 * @author dongbin
 * @version 0.1 2020/11/11 15:53
 * @since 1.8
 */
public interface TransactionResourceFactory<T> {

    /**
     * 构造一个事务资源.
     * @param key        资源key.
     * @param resource   持有的资源.
     * @param autocommit 是否自动提交.
     * @return 创建的事务资源.
     * @throws SQLException 创建异常.
     */
    TransactionResource build(String key, T resource, boolean autocommit) throws SQLException;
}
