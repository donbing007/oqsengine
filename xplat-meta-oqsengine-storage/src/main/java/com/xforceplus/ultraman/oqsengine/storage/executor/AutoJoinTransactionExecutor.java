package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import javax.sql.DataSource;

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
        Optional<Transaction> txOp = transactionManager.getCurrent();
        if (txOp.isPresent()) {

            Transaction tx = txOp.get();
            Optional<TransactionResource> currentRes = tx.queryTransactionResource(dbKey);
            if (currentRes.isPresent()) {
                /*
                 * 已经存在资源,重用.
                 */
                resource = currentRes.get();
            } else {

                if (resourceTask.isAttachmentMaster()) {
                    Collection<TransactionResource> masterResources =
                        tx.listTransactionResource(TransactionResourceType.MASTER);
                    TransactionResource masterResource = masterResources.stream().findFirst().orElseGet(null);
                    /*
                    如果当前要求创建一个依附于master的事务资源.
                    找到一个master类型的资源,那么将共享master的资源类型.
                    找不到将直接普通方式创建.
                     */
                    if (masterResource != null) {

                        resource = buildResourceFromMaster(masterResource, dbKey, false);

                    } else {

                        resource = buildResource(targetDataSource, dbKey, false);

                    }
                } else {
                    resource = buildResource(targetDataSource, dbKey, false);
                }

                txOp.get().join(resource);
            }
        } else {

            // 无事务执行.
            resource = buildResource(targetDataSource, dbKey, true);
        }

        try {
            if (txOp.isPresent()) {
                return resourceTask.run(txOp.get(), resource);
            } else {
                return resourceTask.run(null, resource);
            }
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage(), ex);
        } finally {
            if (!txOp.isPresent()) {
                resource.destroy();
            }
        }
    }

    private TransactionResource buildResourceFromMaster(TransactionResource masterResource, String dbKey,
                                                        boolean autocommit) throws SQLException {
        Connection conn = (Connection) masterResource.value();
        try {
            return this.transactionResourceFactory.build(dbKey, conn, autocommit);
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    private String buildResourceKey(DataSource dataSource, String tableName) {
        return dataSource.toString() + "." + tableName;
    }

    private TransactionResource buildResource(DataSource ds, String dbKey, boolean autocommit) throws SQLException {
        Connection conn = ds.getConnection();
        try {
            return this.transactionResourceFactory.build(dbKey, conn, autocommit);
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }
}
