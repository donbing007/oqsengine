package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 更新执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 15:44
 * @since 1.8
 */
public class UpdateExecutor extends AbstractMasterExecutor<StorageEntity, Integer> {

    public static Executor<StorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new UpdateExecutor(tableName, resource, timeoutMs);
    }

    public UpdateExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public UpdateExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, storageEntity.getUpdateTime());
            st.setLong(2, storageEntity.getTx());
            st.setLong(3, storageEntity.getCommitid());
            st.setInt(4, storageEntity.getOp());
            st.setInt(5, OqsVersion.MAJOR);
            st.setString(6, storageEntity.getAttribute());
            st.setLong(7, storageEntity.getId());
            st.setInt(8, storageEntity.getVersion());

            checkTimeout(st);

            return st.executeUpdate();
        }
    }

    private String buildSQL(StorageEntity storageEntity) {
        //"update %s set version = version + 1, updatetime = ?, tx = ?, commitid = ?, op = ?, attribute = ?,meta = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.OQS_MAJOR).append("=").append("?, ")
            .append(FieldDefine.ATTRIBUTE).append("=").append("?, ")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("? AND ")
            .append(FieldDefine.VERSION).append("=").append("?");
        return sql.toString();
    }
}
