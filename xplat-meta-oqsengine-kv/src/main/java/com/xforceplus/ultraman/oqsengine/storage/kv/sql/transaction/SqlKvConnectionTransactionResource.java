package com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * sql kv 事务资源.
 *
 * @author dongbin
 * @version 0.1 2021/07/28 14:16
 * @since 1.8
 */
public class SqlKvConnectionTransactionResource extends AbstractConnectionTransactionResource {


    /**
     * 初始化基于 java.sql.Connection的资源.
     *
     * @param key        资源key.
     * @param res        资源.
     * @param autoCommit 是否自动提交.
     */
    public SqlKvConnectionTransactionResource(String key, Connection res, boolean autoCommit) throws SQLException {
        super(key, res, autoCommit);
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.KV;
    }
}
