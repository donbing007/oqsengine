package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于普通 JDBC connection 规范的资源实现.
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class ConnectionTransactionResource extends AbstractConnectionTransactionResource {


    public ConnectionTransactionResource(String key, Connection conn, boolean autocommit) throws SQLException {
        super(key, conn, autocommit);
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.MASTER;
    }

}
