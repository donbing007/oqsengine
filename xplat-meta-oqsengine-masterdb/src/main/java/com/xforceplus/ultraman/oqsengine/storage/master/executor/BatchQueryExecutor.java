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
import java.util.List;

/**
 * desc :
 * name : BatchQueryExecutor
 *
 * @author : xujia
 * date : 2020/11/18
 * @since : 1.8
 */
public class BatchQueryExecutor extends AbstractMasterExecutor<Long, Collection<StorageEntity>> {

    private long entity;
    private long startTime;
    private long endTime;
    private long startId;
    private int pageSize;

    public BatchQueryExecutor(String tableName, TransactionResource<Connection> resource, long timeout,
                              long entity, long startTime, long endTime, long startId, int pageSize) {
        super(tableName, resource, timeout);
        this.entity = entity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startId = startId;
        this.pageSize = pageSize;
    }

    public static Executor<Long, Collection<StorageEntity>> build(
            String tableName, TransactionResource resource, long timeout,
            long entity, long startTime, long endTime, long startId, int pageSize) {
        return new BatchQueryExecutor(tableName, resource, timeout, entity, startTime, endTime, startId, pageSize);
    }

    @Override
    public Collection<StorageEntity> execute(Long aLong) throws SQLException {
        String sql = buildSQL();
        PreparedStatement st = getResource().value().prepareStatement(sql);
        st.setLong(1, entity);
        st.setLong(2, startTime);
        st.setLong(3, endTime);
        st.setLong(4, startId);
        st.setLong(5, pageSize);


        checkTimeout(st);

        ResultSet rs = null;
        List<StorageEntity> entities = new ArrayList<>();
        StorageEntity entity;
        try {
            rs = st.executeQuery();

            while (rs.next()) {
                entity = new StorageEntity();
                entity.setId(rs.getLong(FieldDefine.ID));
                entity.setEntity(rs.getLong(FieldDefine.ENTITY));
                entity.setVersion(rs.getInt(FieldDefine.VERSION));
                entity.setTime(rs.getLong(FieldDefine.TIME));
                entity.setPref(rs.getLong(FieldDefine.PREF));
                entity.setCref(rs.getLong(FieldDefine.CREF));
                entity.setAttribute(rs.getString(FieldDefine.ATTRIBUTE));
                entity.setOp(rs.getInt(FieldDefine.OP));
                entity.setTx(rs.getLong(FieldDefine.TX));
                entity.setCommitid(rs.getLong(FieldDefine.COMMITID));

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

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
                FieldDefine.ID,
                FieldDefine.ENTITY,
                FieldDefine.VERSION,
                FieldDefine.TIME,
                FieldDefine.PREF,
                FieldDefine.CREF,
                FieldDefine.ATTRIBUTE,
                FieldDefine.OP,
                FieldDefine.TX,
                FieldDefine.COMMITID
                )
        );

        sql.append(" FROM ")
                .append(getTableName())
                .append(" WHERE ")
                .append(FieldDefine.ENTITY).append("=").append("?")
                .append(" AND ")
                .append(FieldDefine.DELETED).append("=").append("false")
                .append(" AND ")
                .append(FieldDefine.TIME).append(">=").append("?")
                .append(" AND ")
                .append(FieldDefine.TIME).append("<=").append("?")
                .append(" AND ")
                .append(FieldDefine.ID).append(">").append("?")
                .append(" ORDER BY id ")
                .append("Limit ").append("?");
        return sql.toString();
    }
}
