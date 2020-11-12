package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/11/12 15:26
 * @since 1.8
 */
public class SqlConnectionTransactionResourceFactory implements TransactionResourceFactory<Connection> {

    private String tableName;

    private StatusService statusService;

    public SqlConnectionTransactionResourceFactory(String tableName, StatusService statusService) {
        this.tableName = tableName;
        this.statusService = statusService;
    }

    @Override
    public TransactionResource build(String key, Connection resource, boolean autocommit) throws SQLException {
        return new SqlConnectionTransactionResource(key, resource, autocommit, tableName, statusService);
    }
}
