package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于普通 JDBC connection 规范的资源实现.
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class ConnectionTransactionResource implements TransactionResource<Connection> {

    private DataSource key;
    private Connection conn;
    private UndoExecutor undoExecutor;

    public ConnectionTransactionResource(DataSource key, Connection conn, boolean autocommit) throws SQLException {
        this.key = key;
        this.conn = conn;
        if (autocommit) {
            this.conn.setAutoCommit(true);
        } else {
            this.conn.setAutoCommit(false);
        }
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

    @Override
    public void setUndoExecutor(UndoExecutor undoExecutor) {
        this.undoExecutor = undoExecutor;
    }

    @Override
    public void undo(OpTypeEnum opType) throws SQLException {
        this.undoExecutor.run(opType);
    }
}
