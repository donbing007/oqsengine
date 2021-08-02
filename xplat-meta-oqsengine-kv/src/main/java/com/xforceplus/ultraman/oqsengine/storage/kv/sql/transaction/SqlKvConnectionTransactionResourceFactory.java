package com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于sql的kv事务构造工厂.
 *
 * @author dongbin
 * @version 0.1 2021/07/28 13:55
 * @since 1.8
 */
public class SqlKvConnectionTransactionResourceFactory implements TransactionResourceFactory<Connection> {

    @Override
    public TransactionResource build(String key, Connection resource, boolean autocommit) throws SQLException {
        return new SqlKvConnectionTransactionResource(key, resource, autocommit);
    }
}
