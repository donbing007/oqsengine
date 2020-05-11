package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于普通 JDBC connection 规范的资源实现.
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class ConnectionTransactionResource extends UndoTransactionResource<Connection> {

    private String key;
    private Connection conn;

    public ConnectionTransactionResource(String key, Connection conn, boolean autocommit) throws SQLException {
        this.key = key;
        this.conn = conn;
        if (autocommit) {
            this.conn.setAutoCommit(true);
        } else {
            this.conn.setAutoCommit(false);
        }
    }

    @Override
    public DbType dbType() {
        return DbType.MASTER;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Connection value() {
        return conn;
    }

    @Override
    public void commit() throws SQLException {
        conn.commit();
        saveCommitStatus();
    }

    @Override
    public void rollback() throws SQLException {
        conn.rollback();
    }

    @Override
    public void destroy() throws SQLException {
        conn.close();
    }

    @Override
    public boolean isDestroyed() throws SQLException {
        return conn.isClosed();
    }

}
