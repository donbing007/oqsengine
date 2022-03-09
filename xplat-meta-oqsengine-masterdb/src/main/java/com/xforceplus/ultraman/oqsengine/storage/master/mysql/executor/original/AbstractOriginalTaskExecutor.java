package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.util.Optional;

/**
 * 静态任务处理抽像.
 *
 * @author dongbin
 * @version 0.1 2022/2/24 15:49
 * @since 1.8
 */
public abstract class AbstractOriginalTaskExecutor<R, T> extends AbstractJdbcTaskExecutor<R, T> {

    private MetaManager metaManager;

    public AbstractOriginalTaskExecutor(String tableName, TransactionResource resource) {
        super(tableName, resource);
    }

    public AbstractOriginalTaskExecutor(String tableName, TransactionResource resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public void setMetaManager(MetaManager metaManager) {
        this.metaManager = metaManager;
    }

    protected Optional<IEntityClass> findEntityClass(long[] entityClasseIds, String profile) {
        long lastEntityClassId = 0;
        for (int i = 0; i < entityClasseIds.length; i++) {
            if (entityClasseIds[i] > 0) {
                lastEntityClassId = entityClasseIds[i];
            } else {
                break;
            }
        }

        if (lastEntityClassId <= 0) {
            return Optional.empty();
        }

        return metaManager.load(lastEntityClassId, profile);
    }

    /**
     * 构造静态表名, oqs_{应用code}_{对象code}_{定制}
     */
    protected String buildTableName(IEntityClass entityClass) {

        StringBuilder buff = new StringBuilder();
        buff.append("oqs_")
            .append(entityClass.appCode())
            .append('_')
            .append(entityClass.code());
        if (null != entityClass.profile() && !entityClass.profile().isEmpty()) {
            buff.append('_')
                .append(entityClass.profile());
        }

        return buff.toString();
    }

}
