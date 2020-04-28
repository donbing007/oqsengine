package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
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

    private Class resourceClass;

    /**
     * 构造一个事务执行器,需要一个事务管理器.
     *
     * @param transactionManager 事务管理器.
     */
    public AutoShardTransactionExecutor(TransactionManager transactionManager, Class resourceClass) {
        this.transactionManager = transactionManager;
        this.resourceClass = resourceClass;
    }

    @Override
    public Object execute(Task task) throws SQLException {
        DataSourceShardingTask shardTask;
        if (DataSourceShardingTask.class.isInstance(task)) {
            shardTask = (DataSourceShardingTask) task;
        } else {
            throw new SQLException("Task types other than DataSourceShardingTask are not supported.");
        }

        DataSource targetDataSource = shardTask.getDataSourceSelector().select(shardTask.getShardKey());

        String dbKey = createDbKey(targetDataSource);

        TransactionResource resource;
        Optional<Transaction> tx = transactionManager.getCurrent();
        if (tx.isPresent()) {
            Optional<TransactionResource> currentRes = tx.get().query(dbKey);
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

                try {
                    resource = buildResource(dbKey, conn, false, shardTask.getShardKey());
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }

                tx.get().join(resource);
            }
        } else {
            // 无事务运行.
            Connection conn = targetDataSource.getConnection();
            try {
                resource = buildResource(dbKey, conn, true, shardTask.getShardKey());
            } catch (Exception ex) {
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        try {
            return task.run(resource);
        } finally {
            if (!tx.isPresent()) {
                resource.destroy();
            }
        }
    }

    private TransactionResource buildResource(DataSource key, Connection value, boolean autocommit)
            throws Exception {

        Constructor<TransactionResource> constructor =
                resourceClass.getConstructor(DataSource.class, Connection.class, Boolean.TYPE);
        return constructor.newInstance(key, value, autocommit);
    }

    private TransactionResource buildResource(String key, Connection value, boolean autocommit, String shardKey)
            throws Exception {

        Constructor<TransactionResource> constructor =
                resourceClass.getConstructor(String.class, Connection.class, Boolean.TYPE);
        TransactionResource resource = constructor.newInstance(key, value, autocommit);
        ((UndoTransactionResource) resource).undoLog().setShardKey(shardKey);

        return resource;
    }

    private String createDbKey(DataSource dataSource) {
        return dataSource == null ? null : dataSource.toString();
    }

}
