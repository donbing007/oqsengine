package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/11/11 15:59
 * @since 1.8
 */
public class ConnectionTransactionResourceFactory implements TransactionResourceFactory<Connection> {

    private String tableName;

    private StatusService statusService;

    public ConnectionTransactionResourceFactory(String tableName, StatusService statusService) {
        this.tableName = tableName;
        this.statusService = statusService;
    }

    @Override
    public TransactionResource build(String key, Connection resource,  boolean autocommit) throws SQLException {
        return new ConnectionTransactionResource(key, resource, autocommit, tableName, statusService);
    }
}
