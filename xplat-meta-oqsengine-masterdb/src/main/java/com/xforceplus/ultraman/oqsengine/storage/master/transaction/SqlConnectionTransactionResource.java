package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SqlConnectionTransactionResource extends AbstractConnectionTransactionResource {

    final Logger logger = LoggerFactory.getLogger(SqlConnectionTransactionResource.class);

    private static final String UPDATE_COMMITID_SQL =
        "update %s set " + FieldDefine.COMMITID + " = ? where " + FieldDefine.TX + " = ?";
    private String updateCommitIdSql;

    private CommitIdStatusService commitIdStatusService;

    public SqlConnectionTransactionResource(
        String key,
        Connection conn,
        boolean autocommit,
        String tableName,
        CommitIdStatusService commitIdStatusService) throws SQLException {
        super(key, conn, autocommit);
        updateCommitIdSql = String.format(UPDATE_COMMITID_SQL, tableName);

        this.commitIdStatusService = commitIdStatusService;
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.MASTER;
    }

    @Override
    public void commit(long commitId) throws SQLException {
        Optional<Transaction> transactionOp = getTransaction();
        if (transactionOp.isPresent()) {
            Transaction tx = transactionOp.get();
            updateCommitId(tx.id(), commitId);

            /**
             * 数据库提交会在写入commitId之前,在保证之前有可能同步已经完成造成提交号实际执行顺序如下.
             *  1. 淘汰提交号.
             *  2. 保存提交号.
             *  最终结果为提交号没有被正确淘汰.
             *  这里由commitIdStatusService的实现来保证保存和事务提交是在一个锁保护内,以防这期间被淘汰.
             */
            try {
                commitIdStatusService.save(commitId, () -> {
                    try {
                        super.commit(commitId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }

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

            if (logger.isDebugEnabled()) {
                logger.debug("Update the commit number in the new change data in the transaction ({}) to {}.",
                    txId, commitId);
            }
        } finally {
            if (stat != null) {
                stat.close();
            }
        }

    }
}
