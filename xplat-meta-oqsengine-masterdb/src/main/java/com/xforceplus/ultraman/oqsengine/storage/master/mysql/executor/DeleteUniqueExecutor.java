package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 删除业务主键执行期.
 *
 * @author leo
 * @version 0.1 2020/03/21 16:03
 * @since 1.8
 */
public class DeleteUniqueExecutor extends AbstractJdbcTaskExecutor<StorageUniqueEntity, Integer> {

    public static Executor<StorageUniqueEntity, Integer> build(
        String tableName, TransactionResource resource, long timeout) {
        return new DeleteUniqueExecutor(tableName, resource, timeout);
    }

    public DeleteUniqueExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteUniqueExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public Integer execute(StorageUniqueEntity storageUniqueEntity) throws Exception {
        String sql = buildForceSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, storageUniqueEntity.getId());
            checkTimeout(st);
            return st.executeUpdate();
        }
    }

    private String buildForceSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append('?');
        return sql.toString();
    }
}
