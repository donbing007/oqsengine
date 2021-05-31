package com.xforceplus.ultraman.oqsengine.storage.master.executor.errors;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.ErrorDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.AbstractMasterExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ReplaceErrorExecutor extends AbstractMasterExecutor<ErrorStorageEntity, Integer> {

    public static Executor<ErrorStorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new ReplaceErrorExecutor(tableName, resource, timeoutMs);
    }

    public ReplaceErrorExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public ReplaceErrorExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(ErrorStorageEntity errorStorageEntity) throws SQLException {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, errorStorageEntity.getMaintainId());
            st.setLong(2, errorStorageEntity.getId());
            st.setLong(3, errorStorageEntity.getEntity());
            st.setString(4, errorStorageEntity.getErrors());
            st.setLong(5, errorStorageEntity.getExecuteTime());
            st.setLong(6, errorStorageEntity.getFixedTime());
            st.setInt(7, errorStorageEntity.getStatus());

            checkTimeout(st);

            return st.executeUpdate();
        }
    }


    private String buildSQL() {
        //"replace %s set maintainid = ?, id = ?, entity = ?, errors = ?, executetime = ?, fixedtime = ?, status = ? where id = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("REPLACE ").append(getTableName())
            .append(" SET ")
            .append(ErrorDefine.MAINTAIN_ID).append("=?, ")
            .append(ErrorDefine.ID).append("=?, ")
            .append(ErrorDefine.ENTITY).append("=?, ")
            .append(ErrorDefine.ERRORS).append("=?, ")
            .append(ErrorDefine.EXECUTE_TIME).append("=?, ")
            .append(ErrorDefine.FIXED_TIME).append("=?, ")
            .append(ErrorDefine.STATUS).append("=? ")
            .append(" WHERE ")
            .append(ErrorDefine.ID).append("=").append("?");
        return sql.toString();
    }


}

