package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;

/**
 * 主库存任务执行器抽像.
 *
 * @author dongbin
 * @version 0.1 2022/3/10 15:47
 * @since 1.8
 */
public abstract class AbstractMasterTaskExecutor<R, T> extends AbstractJdbcTaskExecutor<R, T> {

    private IEntityClass entityClass;

    public AbstractMasterTaskExecutor(String tableName,
                                      TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public AbstractMasterTaskExecutor(String tableName,
                                      TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }
}
