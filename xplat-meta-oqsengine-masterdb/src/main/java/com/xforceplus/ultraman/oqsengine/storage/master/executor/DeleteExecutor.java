package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
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
public class DeleteExecutor extends AbstractMasterExecutor<StorageEntity, Integer> {

    public static Executor<StorageEntity, Integer> build(
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
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        PreparedStatement st;
        if (VersionHelp.isOmnipotence(storageEntity.getVersion())) {

            String sql = buildForceSQL(storageEntity);
            st = getResource().value().prepareStatement(sql);
            st.setInt(1, storageEntity.getVersion());
            st.setBoolean(2, true);
            st.setLong(3, storageEntity.getTime());
            st.setLong(4, storageEntity.getTx());
            st.setLong(5, storageEntity.getCommitid());
            st.setInt(6, storageEntity.getOp());
            st.setLong(7, storageEntity.getId());
        } else {

            String sql = buildSQl(storageEntity);
            st = getResource().value().prepareStatement(sql);
            st.setBoolean(1, true);
            st.setLong(2, storageEntity.getTime());
            st.setLong(3, storageEntity.getTx());
            st.setLong(4, storageEntity.getCommitid());
            st.setInt(5, storageEntity.getOp());
            st.setLong(6, storageEntity.getId());
            st.setInt(7, storageEntity.getVersion());
        }

        checkTimeout(st);

        try {
            return st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private String buildForceSQL(StorageEntity storageEntity) {
        //"update %s set version = ?, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append("?, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?');

        return sql.toString();
    }

    private String buildSQl(StorageEntity storageEntity) {
        //"update %s set version = version + 1, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?')
            .append(" AND ")
            .append(FieldDefine.VERSION).append("=").append('?');

        return sql.toString();
    }
}
