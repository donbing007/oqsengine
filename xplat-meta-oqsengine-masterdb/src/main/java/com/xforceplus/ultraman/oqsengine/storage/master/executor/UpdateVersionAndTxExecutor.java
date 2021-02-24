package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 更新目标数据版本和事务信息.
 *
 * @author dongbin
 * @version 0.1 2020/11/3 12:02
 * @since 1.8
 */
public class UpdateVersionAndTxExecutor extends AbstractMasterExecutor<StorageEntity, Integer> {

    public static Executor<StorageEntity, Integer> build(
        String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        return new UpdateVersionAndTxExecutor(tableName, resource, timeoutMs);
    }

    public UpdateVersionAndTxExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public UpdateVersionAndTxExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setInt(1, storageEntity.getVersion());
            st.setLong(2, storageEntity.getUpdateTime());
            st.setLong(3, storageEntity.getTx());
            st.setLong(4, storageEntity.getCommitid());
            st.setLong(5, OperationType.UPDATE.getValue());
            st.setLong(6, storageEntity.getId());

            checkTimeout(st);

            return st.executeUpdate();
        }
    }

    private String buildSQL(StorageEntity storageEntity) {
        // update table set version=?,time=?,tx=?,commitid=?,op = ? where id=?
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?");
        return sql.toString();
    }
}
