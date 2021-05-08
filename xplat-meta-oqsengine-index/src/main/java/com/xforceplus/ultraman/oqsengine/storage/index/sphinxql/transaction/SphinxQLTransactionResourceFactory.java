package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 创建一个事务资源.
 *
 * @author dongbin
 * @version 0.1 2020/11/11 16:03
 * @since 1.8
 */
public class SphinxQLTransactionResourceFactory implements TransactionResourceFactory<Connection> {
    @Override
    public TransactionResource build(String key, Connection resource, boolean autocommit) throws SQLException {
        return new SphinxQLTransactionResource(key, resource, autocommit);
    }
}
