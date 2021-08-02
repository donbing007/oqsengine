package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明: 更新业务主键
 * 作者(@author): liwei
 * 创建时间: 2021/3/19 4:55 PM
 */
public class UpdateUniqueExecutor extends AbstractJdbcTaskExecutor<StorageUniqueEntity, Integer> {

    public static Executor<StorageUniqueEntity, Integer> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new UpdateUniqueExecutor(tableName, resource, timeoutMs);
    }

    public UpdateUniqueExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public UpdateUniqueExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(StorageUniqueEntity storageUniqueEntity) throws SQLException {
        String sql = buildSQL(storageUniqueEntity);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setString(1, storageUniqueEntity.getKey());
            st.setLong(2, storageUniqueEntity.getId());
            checkTimeout(st);
            return st.executeUpdate();
        }
    }

    private String buildSQL(StorageUniqueEntity storageUniqueEntity) {

        // todo update entityClass ?
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.UNIQUE_KEY).append("=?")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?");
        return sql.toString();
    }
}
