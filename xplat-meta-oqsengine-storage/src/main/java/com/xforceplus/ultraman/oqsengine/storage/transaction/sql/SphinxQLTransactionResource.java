package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;

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
    private UndoInfo undoInfo;

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
    public DbTypeEnum dbType() {
        return DbTypeEnum.INDEX;
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
    public boolean isDestroyed() throws SQLException {
        return conn.isClosed();
    }

    @Override
    public void setUndoInfo(Long txId, String dbKey, OpTypeEnum opType, Object obj){
        this.undoInfo = new UndoInfo(txId, dbKey, dbType(), opType, obj);
    }

    @Override
    public UndoInfo getUndoInfo() {
        return undoInfo;
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
