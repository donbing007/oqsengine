package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 自动事务处理的执行器实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 15:41
 * @since 1.8
 */
public class AutoShardTransactionExecutor implements TransactionExecutor {

    private TransactionManager transactionManager;

    /**
     * 构造一个事务执行器,需要一个事务管理器.
     *
     * @param transactionManager 事务管理器.
     */
    public AutoShardTransactionExecutor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object execute(Task task) throws SQLException {
        DataSourceShardingTask shardTask = null;
        if (DataSourceShardingTask.class.isInstance(task)) {
            shardTask = (DataSourceShardingTask) task;
        } else {
            throw new SQLException("Task types other than DataSourceShardingTask are not supported.");
        }

        DataSource targetDataSource = shardTask.getDataSourceSelector().select(shardTask.getShardKey());

        TransactionResource resource;
        Transaction tx = transactionManager.getCurrent();
        if (tx != null) {
            Optional<TransactionResource> currentRes = tx.query(targetDataSource);
            if (currentRes.isPresent()) {
                /**
                 * 已经存在资源,重用.
                 */
                resource = currentRes.get();
            } else {
                /**
                 * 资源不存在,重新创建.
                 */
                Connection conn = targetDataSource.getConnection();
                resource = new ConnectionTransactionResource(targetDataSource, conn);
                tx.join(resource);
            }
        } else {
            // 无事务运行.
            Connection conn = targetDataSource.getConnection();
            conn.setAutoCommit(true);
            resource = new ConnectionTransactionResource(targetDataSource, conn);
        }

        try {
            return task.run(resource);
        } finally {
            if (tx == null) {
                resource.destroy();
            }
        }
    }
}
