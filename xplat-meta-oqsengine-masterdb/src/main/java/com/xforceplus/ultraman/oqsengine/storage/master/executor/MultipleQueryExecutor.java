package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 批量查询执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/3 14:37
 * @since 1.8
 */
public class MultipleQueryExecutor extends AbstractMasterExecutor<long[], Collection<StorageEntity>> {

    public static Executor<long[], Collection<StorageEntity>> build(
        String tableName, TransactionResource<Connection> resource, long timeout) {
        return new MultipleQueryExecutor(tableName, resource, timeout);
    }

    public MultipleQueryExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public MultipleQueryExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public Collection<StorageEntity> execute(long[] ids) throws SQLException {
        String sql = buildSQL(ids.length);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            int index = 1;
            for (long id : ids) {
                st.setLong(index++, id);
            }
            st.setBoolean(ids.length + 1, false);

            checkTimeout(st);

            List<StorageEntity> entities = new ArrayList<>(ids.length);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    StorageEntity.Builder builder = StorageEntity.Builder.aStorageEntity()
                        .withId(rs.getLong(FieldDefine.ID))
                        .withVersion(rs.getInt(FieldDefine.VERSION))
                        .withOp(rs.getInt(FieldDefine.OP))
                        .withCreateTime(rs.getLong(FieldDefine.CREATE_TIME))
                        .withUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME))
                        .withOqsMajor(rs.getInt(FieldDefine.OQS_MAJOR))
                        .withAttribute(rs.getString(FieldDefine.ATTRIBUTE));

                    long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
                    for (int i = 0; i < entityClassIds.length; i++) {
                        entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
                    }
                    builder.withEntityClasses(entityClassIds);
                    entities.add(builder.build());
                }

                return entities;
            }
        }

    }

    private String buildSQL(int size) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.ID,
            FieldDefine.ENTITYCLASS_LEVEL_0,
            FieldDefine.ENTITYCLASS_LEVEL_1,
            FieldDefine.ENTITYCLASS_LEVEL_2,
            FieldDefine.ENTITYCLASS_LEVEL_3,
            FieldDefine.ENTITYCLASS_LEVEL_4,
            FieldDefine.CREATE_TIME,
            FieldDefine.UPDATE_TIME,
            FieldDefine.VERSION,
            FieldDefine.OP,
            FieldDefine.OQS_MAJOR,
            FieldDefine.ATTRIBUTE
            )
        )
            .append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append(" IN (").append(String.join(",", Collections.nCopies(size, "?")))
            .append(") AND ")
            .append(FieldDefine.DELETED).append("=").append("?");
        return sql.toString();
    }
}
