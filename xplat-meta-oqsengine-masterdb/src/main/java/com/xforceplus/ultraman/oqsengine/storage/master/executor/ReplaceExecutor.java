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
 * 更新执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 15:44
 * @since 1.8
 */
public class ReplaceExecutor extends AbstractMasterExecutor<StorageEntity, Integer> {

    public static Executor<StorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new ReplaceExecutor(tableName, resource, timeoutMs);
    }

    public ReplaceExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public ReplaceExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        PreparedStatement st = getResource().value().prepareStatement(sql);
        st.setLong(1, storageEntity.getTime());
        st.setLong(2, storageEntity.getTx());
        st.setLong(3, storageEntity.getCommitid());
        st.setInt(4, OperationType.UPDATE.getValue());
        st.setString(5, storageEntity.getAttribute());
        st.setString(6, storageEntity.getMeta());
        st.setLong(7, storageEntity.getId());
        st.setInt(8, storageEntity.getVersion());

        checkTimeout(st);

        try {
            return st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    private String buildSQL(StorageEntity storageEntity) {
        //"update %s set version = version + 1, time = ?, tx = ?, commitid = ?, op = ?, attribute = ?,meta = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ATTRIBUTE).append("=").append("?, ")
            .append(FieldDefine.META).append("=").append("? ")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("? AND ")
            .append(FieldDefine.VERSION).append("=").append("?");
        return sql.toString();
    }
}
