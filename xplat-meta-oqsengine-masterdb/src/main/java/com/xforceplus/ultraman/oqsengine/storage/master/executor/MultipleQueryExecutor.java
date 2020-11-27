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
public class MultipleQueryExecutor extends AbstractMasterExecutor<Collection<Long>, Collection<StorageEntity>> {

    public static Executor<Collection<Long>, Collection<StorageEntity>> build(
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
    public Collection<StorageEntity> execute(Collection<Long> ids) throws SQLException {
        String sql = buildSQL(ids.size());
        PreparedStatement st = getResource().value().prepareStatement(sql);
        int index = 1;
        for (long id : ids) {
            st.setLong(index++, id);
        }
        st.setBoolean(ids.size() + 1, false);

        checkTimeout(st);

        ResultSet rs = null;
        List<StorageEntity> entities = new ArrayList<>(ids.size());
        StorageEntity entity;
        try {
            rs = st.executeQuery();

            while (rs.next()) {
                entity = new StorageEntity();
                entity.setId(rs.getLong(FieldDefine.ID));
                entity.setEntity(rs.getLong(FieldDefine.ENTITY));
                entity.setVersion(rs.getInt(FieldDefine.VERSION));
                entity.setOp(rs.getInt(FieldDefine.OP));
                entity.setTx(rs.getLong(FieldDefine.TX));
                entity.setCommitid(rs.getLong(FieldDefine.COMMITID));
                entity.setTime(rs.getLong(FieldDefine.TIME));
                entity.setPref(rs.getLong(FieldDefine.PREF));
                entity.setCref(rs.getLong(FieldDefine.CREF));
                entity.setOqsMajor(rs.getInt(FieldDefine.OQS_MAJOR));
                entity.setAttribute(rs.getString(FieldDefine.ATTRIBUTE));
                entities.add(entity);
            }

            return entities;

        } finally {
            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }
        }
    }

    private String buildSQL(int size) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.ID,
            FieldDefine.ENTITY,
            FieldDefine.VERSION,
            FieldDefine.OP,
            FieldDefine.PREF,
            FieldDefine.CREF,
            FieldDefine.TIME,
            FieldDefine.TX,
            FieldDefine.COMMITID,
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
