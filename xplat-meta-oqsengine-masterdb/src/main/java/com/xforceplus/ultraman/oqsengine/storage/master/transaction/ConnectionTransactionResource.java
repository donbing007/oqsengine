package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 基于普通 JDBC connection 规范的资源实现.
 * 强制事务以READ_COMMITTED运行.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class ConnectionTransactionResource extends AbstractConnectionTransactionResource {

    private static final String UPDATE_COMMITID_SQL =
        "update %s set " + FieldDefine.COMMITID + " = ? where " + FieldDefine.TX + " = ?";
    private String updateCommitIdSql;

    public ConnectionTransactionResource(String key, Connection conn, boolean autocommit, String tableName) throws SQLException {
        super(key, conn, autocommit);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        updateCommitIdSql = String.format(UPDATE_COMMITID_SQL, tableName);
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.MASTER;
    }

    @Override
    public void commit(long commitId) throws SQLException {
        Optional<Transaction> transactionOp = getTransaction();
        if (transactionOp.isPresent()) {
            updateCommitId(transactionOp.get().id(), commitId);
            super.commit(commitId);
        } else {
            throw new SQLException("Is not bound to any transaction.");
        }
    }

    // 当前所有事务的所有写记录都更新成最新的提交号.
    private void updateCommitId(long txId, long commitId) throws SQLException {
        PreparedStatement stat = null;
        try {
            stat = value().prepareStatement(updateCommitIdSql);
            stat.setLong(1, commitId);
            stat.setLong(2, txId);

            stat.executeUpdate();
        } finally {
            if (stat != null) {
                stat.close();
            }
        }

    }
}
