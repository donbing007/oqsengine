package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class ConnectionTransactionResource implements TransactionResource<Connection> {

    private DataSource key;
    private Connection conn;

    public ConnectionTransactionResource(DataSource key, Connection conn) {
        this.key = key;
        this.conn = conn;
    }

    @Override
    public Object key() {
        return key;
    }

    @Override
    public Connection value() {
        return conn;
    }

    @Override
    public void commit() throws SQLException {
        conn.commit();
    }

    @Override
    public void rollback() throws SQLException {
        conn.rollback();
    }

    @Override
    public void destroy() throws SQLException {
        conn.close();
    }
}
