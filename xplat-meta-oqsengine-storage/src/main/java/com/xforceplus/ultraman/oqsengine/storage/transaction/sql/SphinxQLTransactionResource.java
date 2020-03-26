package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * sphinxQL 相关的资源管理器.
 * @author dongbin
 * @version 0.1 2020/2/28 17:25
 * @since 1.8
 */
public class SphinxQLTransactionResource implements TransactionResource<Connection> {

    private DataSource key;
    private Connection conn;
    private UndoExecutor undoExecutor;

    public SphinxQLTransactionResource(DataSource key, Connection conn, boolean autocommit) throws SQLException {
        this.key = key;
        this.conn = conn;
        // SphinxQL 只有在 autocommit = true 情况下才工作.
        this.conn.setAutoCommit(true);

        if (!autocommit) {
            execute("begin");
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
        execute("commit");
    }

    @Override
    public void rollback() throws SQLException {
        execute("rollback");
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

    private void execute(String command) throws SQLException {
        Statement st = conn.createStatement();
        try {
            st.execute(command);
        } finally {
            st.close();
        }
    }
}
