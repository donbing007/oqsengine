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
        // SphinxQL 只有在 autocommit = true 情况下才工作.
        super(key, conn, true);

        /**
         * 重载 autocommit,来决定是否启用自动提交.
         */
        this.autocommit = autocommit;

        if (!isAutoCommit()) {
            execute("begin");
        }
    }

    @Override
    public boolean isAutoCommit() {
        return autocommit;
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.INDEX;
    }

    @Override
    public void commit() throws SQLException {
        execute("commit");
    }

    @Override
    public void rollback() throws SQLException {
        execute("rollback");
    }

    private void execute(String command) throws SQLException {
        Statement st = value().createStatement();
        try {
            st.execute(command);
        } finally {
            st.close();
        }
    }
}
