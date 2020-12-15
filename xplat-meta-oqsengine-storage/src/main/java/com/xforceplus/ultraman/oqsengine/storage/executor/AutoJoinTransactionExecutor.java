package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 自动事务处理的执行器实现.
 * 不会创建事务,只会加入事务.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 15:41
 * @since 1.8
 */
public class AutoJoinTransactionExecutor implements TransactionExecutor {

    private TransactionManager transactionManager;

    private TransactionResourceFactory transactionResourceFactory;

    private Selector<DataSource> dataSourceSelector;
    private Selector<String> tableSelector;

    /**
     * 构造一个事务执行器,需要一个事务管理器.
     *
     * @param transactionManager 事务管理器.
     */
    public AutoJoinTransactionExecutor(
        TransactionManager transactionManager,
        TransactionResourceFactory transactionResourceFactory,
        Selector<DataSource> dataSourceSelector,
        Selector<String> tableSelector) {
        this.transactionManager = transactionManager;
        this.transactionResourceFactory = transactionResourceFactory;
        this.dataSourceSelector = dataSourceSelector;
        this.tableSelector = tableSelector;
    }

    @Override
    public Object execute(ResourceTask resourceTask) throws SQLException {
        DataSource targetDataSource = dataSourceSelector.select(resourceTask.key());
        String tableName = tableSelector.select(resourceTask.key());

        String dbKey = buildResourceKey(targetDataSource, tableName);

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
                    resource = this.transactionResourceFactory.build(dbKey, conn, false);
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }

                tx.get().join(resource);
            }
        } else {

            Connection conn = targetDataSource.getConnection();
            try {
                resource = this.transactionResourceFactory.build(dbKey, conn, true);
            } catch (Exception ex) {
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        ExecutorHint hint;
        if (tx.isPresent()) {
            hint = new DefaultExecutorHint(tx.get().getAccumulator());
        } else {
            hint = new DefaultExecutorHint();
        }
        try {
            return resourceTask.run(resource, hint);
        } finally {

            if (!tx.isPresent()) {
                resource.destroy();
            }
        }
    }

    private String buildResourceKey(DataSource dataSource, String tableName) {
        return dataSource.toString() + "." + tableName;
    }

}
