package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 删除执行器.
 * 删除业务主键
 * @author leo
 * @version 0.1 2020/03/21 16:03
 * @since 1.8
 */
public class DeleteUniqueExecutor extends AbstractMasterExecutor<StorageUniqueEntity, Integer> {

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
    public Integer execute(StorageUniqueEntity storageUniqueEntity) throws SQLException {
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
