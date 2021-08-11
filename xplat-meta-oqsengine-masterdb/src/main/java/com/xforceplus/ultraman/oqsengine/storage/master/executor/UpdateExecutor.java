package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
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
public class UpdateExecutor extends AbstractJdbcTaskExecutor<MasterStorageEntity, Integer> {

    public static Executor<MasterStorageEntity, Integer> build(
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
    public Integer execute(MasterStorageEntity masterStorageEntity) throws Exception {
        String sql = buildSQL(masterStorageEntity);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, masterStorageEntity.getUpdateTime());
            st.setLong(2, masterStorageEntity.getTx());
            st.setLong(3, masterStorageEntity.getCommitid());
            st.setInt(4, masterStorageEntity.getOp());
            st.setInt(5, OqsVersion.MAJOR);
            st.setInt(6, masterStorageEntity.getEntityClassVersion());
            st.setString(7, masterStorageEntity.getAttribute());
            st.setLong(8, masterStorageEntity.getId());
            st.setInt(9, masterStorageEntity.getVersion());

            checkTimeout(st);

            return st.executeUpdate();
        }
    }

    private String buildSQL(MasterStorageEntity masterStorageEntity) {
        //"update %s set version = version + 1, updatetime = ?, tx = ?, commitid = ?, op = ?, attribute = ?,meta = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.UPDATE_TIME).append("=?, ")
            .append(FieldDefine.TX).append("=?, ")
            .append(FieldDefine.COMMITID).append("=?, ")
            .append(FieldDefine.OP).append("=?, ")
            .append(FieldDefine.OQS_MAJOR).append("=?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=?, ")
            .append(FieldDefine.ATTRIBUTE).append("=?")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?")
            .append(" AND ")
            .append(FieldDefine.VERSION).append("=?");
        return sql.toString();
    }
}
