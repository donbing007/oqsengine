package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 删除执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 16:03
 * @since 1.8
 */
public class DeleteExecutor extends AbstractMasterExecutor<MasterStorageEntity, Integer> {

    public static Executor<MasterStorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeout) {
        return new DeleteExecutor(tableName, resource, timeout);
    }

    public DeleteExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public Integer execute(MasterStorageEntity masterStorageEntity) throws SQLException {
        if (VersionHelp.isOmnipotence(masterStorageEntity.getVersion())) {

            String sql = buildForceSQL(masterStorageEntity);
            try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                st.setInt(1, VersionHelp.OMNIPOTENCE_VERSION);
                st.setBoolean(2, true);
                st.setLong(3, masterStorageEntity.getUpdateTime());
                st.setLong(4, masterStorageEntity.getTx());
                st.setLong(5, masterStorageEntity.getCommitid());
                st.setInt(6, masterStorageEntity.getOp());
                st.setInt(7, masterStorageEntity.getEntityClassVersion());
                st.setLong(8, masterStorageEntity.getId());
                checkTimeout(st);
                return st.executeUpdate();
            }
        } else {

            String sql = buildSQl(masterStorageEntity);
            try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                st.setBoolean(1, true);
                st.setLong(2, masterStorageEntity.getUpdateTime());
                st.setLong(3, masterStorageEntity.getTx());
                st.setLong(4, masterStorageEntity.getCommitid());
                st.setInt(5, masterStorageEntity.getOp());
                st.setInt(6, masterStorageEntity.getEntityClassVersion());
                st.setLong(7, masterStorageEntity.getId());
                st.setInt(8, masterStorageEntity.getVersion());
                checkTimeout(st);
                return st.executeUpdate();
            }
        }
    }

    private String buildForceSQL(MasterStorageEntity masterStorageEntity) {
        //"update %s set version = ?, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append("?, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?');

        return sql.toString();
    }

    private String buildSQl(MasterStorageEntity masterStorageEntity) {
        //"update %s set version = version + 1, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?')
            .append(" AND ")
            .append(FieldDefine.VERSION).append("=").append('?');

        return sql.toString();
    }
}
