package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * sphinxQL 相关的资源管理器.
 *
 * @author dongbin
 * @version 0.1 2020/2/28 17:25
 * @since 1.8
 */
public class SphinxQLTransactionResource extends AbstractConnectionTransactionResource {

    private boolean autocommit;

    public SphinxQLTransactionResource(String key, Connection conn, boolean autocommit) throws SQLException {
        super(key, conn, true);

        if (autocommit != true) {
            execute("begin");
        }

        this.autocommit = autocommit;
    }

    @Override
    public void commit(long commitId) throws SQLException {
        if (!this.autocommit) {
            execute("commit");
        } else {
            throw new SQLException("Can't call commit when autocommit=true");
        }
    }

    @Override
    public void commit() throws SQLException {
        if (!this.autocommit) {
            execute("commit");
        } else {
            throw new SQLException("Can't call commit when autocommit=true");
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (!this.autocommit) {
            execute("rollback");
        } else {
            throw new SQLException("Can't call rollback when autocommit=true");
        }
    }

    @Override
    public boolean isAutoCommit() {
        return this.autocommit;
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.INDEX;
    }

    private void execute(String command) throws SQLException {
        try (Statement st = value().createStatement()) {
            st.execute(command);
        }
    }
}
