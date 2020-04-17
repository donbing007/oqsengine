package com.xforceplus.ultraman.oqsengine.storage.transaction.sql;

import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.AbstractTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;

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
public class SphinxQLTransactionResource extends AbstractTransactionResource<Connection> {

    private Object key;
    private Connection conn;

    public SphinxQLTransactionResource(DataSource key, Connection conn, boolean autocommit) throws SQLException {
        this.key = key;
        this.conn = conn;
        // SphinxQL 只有在 autocommit = true 情况下才工作.
        this.conn.setAutoCommit(true);

        if (!autocommit) {
            execute("begin");
        }
    }

    public SphinxQLTransactionResource(String key, Connection conn, boolean autocommit) throws SQLException {
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

    private void execute(String command) throws SQLException {
        Statement st = conn.createStatement();
        try {
            st.execute(command);
        } finally {
            st.close();
        }
    }
}
