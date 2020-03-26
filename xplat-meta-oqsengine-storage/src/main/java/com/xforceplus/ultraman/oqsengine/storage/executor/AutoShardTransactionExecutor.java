package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

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

    private UndoExecutor undoExecutor;

    /**
     * 构造一个事务执行器,需要一个事务管理器.
     *
     * @param transactionManager 事务管理器.
     */
    public AutoShardTransactionExecutor(TransactionManager transactionManager, Class resourceClass) {
        this.transactionManager = transactionManager;
        this.resourceClass = resourceClass;
    }

    public AutoShardTransactionExecutor(TransactionManager transactionManager, Class resourceClass, UndoExecutor undoExecutor) {
        this.transactionManager = transactionManager;
        this.resourceClass = resourceClass;
        this.undoExecutor = undoExecutor;
    }

    @Override
    public Object execute(Task task) throws SQLException {
        DataSourceShardingTask shardTask = null;
        if (DataSourceShardingTask.class.isInstance(task)) {
            shardTask = (DataSourceShardingTask) task;
        } else {
            throw new SQLException("Task types other than DataSourceShardingTask are not supported.");
        }

        DbTypeEnum dbType = resourceClass.isInstance(SphinxQLTransactionResource.class) ? DbTypeEnum.INDEX :
                resourceClass.isInstance(ConnectionTransactionResource.class) ? DbTypeEnum.MASTOR : null;

        DataSource targetDataSource = shardTask.getDataSourceSelector().select(shardTask.getShardKey());

        TransactionResource resource;
        Optional<Transaction> tx = transactionManager.getCurrent();
        if (tx.isPresent()) {
            Optional<TransactionResource> currentRes = tx.get().query(targetDataSource);
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
                    resource = buildResource(targetDataSource, conn, false);
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }

                tx.get().join(resource);
            }

            //-------------------------------------------
            this.undoExecutor.setDbType(dbType);
            this.undoExecutor.setTxId(tx.get().id());
            resource.setUndoExecutor(this.undoExecutor);
        } else {
            // 无事务运行.
            Connection conn = targetDataSource.getConnection();
            try {
                resource = buildResource(targetDataSource, conn, true);
            } catch (Exception ex) {
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        try {
            Object res =  task.run(resource);

            //--------------------------------------------------------------------------------------------

            OpTypeEnum opType = ((DataSourceShardingTask) task).getOpType();
            undoExecutor.saveUndoLog(dbType, opType, tx.get().id(), (IEntity) res);

            return res;
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
}
