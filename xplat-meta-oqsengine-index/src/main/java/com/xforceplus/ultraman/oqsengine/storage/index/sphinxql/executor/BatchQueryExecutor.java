package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;

import javax.sql.DataSource;
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
public class BatchQueryExecutor implements Executor<Long, Collection<EntityRef>> {

    private long entityId;
    private long maintainId;
    private long start;
    private long end;
    private String indexTableName;

    private DataSource resource;

    public BatchQueryExecutor(DataSource resource, String indexTableName, long entityId, long maintainId, long start, long end) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        this.entityId = entityId;
        this.maintainId = maintainId;
        this.start = start;
        this.end = end;
    }

    public static BatchQueryExecutor build(DataSource resource, String indexTableName, long entityId, long maintainId, long start, long end) {
        return new BatchQueryExecutor(resource, indexTableName, entityId, maintainId, start, end);
    }

    @Override
    public Collection<EntityRef> execute(Long aLong) throws SQLException {

        ResultSet rs = null;
        String sql = buildCleanSelect(indexTableName);
        try (PreparedStatement st = resource.getConnection().prepareStatement(sql)){
            st.setLong(1, entityId);    // entityId
            st.setLong(2, maintainId);  // maintainId
            st.setLong(3, start);       // start
            st.setLong(4, end);         // end


            rs = st.executeQuery();
            List<EntityRef> entityRefList = new ArrayList<>();
            if (rs.next()) {
                entityRefList.add(new EntityRef(rs.getLong(FieldDefine.ID), rs.getLong(FieldDefine.PREF), rs.getLong(FieldDefine.CREF)));
            }

            return entityRefList;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }


    private String buildCleanSelect(String indexName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
                FieldDefine.ID,
                FieldDefine.PREF,
                FieldDefine.CREF
        ));
        sql.append(" FROM ")
                .append(indexName)
                .append(" WHERE ")
                .append(FieldDefine.ENTITY).append("=").append("?")
                .append(" AND ")
                .append(FieldDefine.MAINTAIN_ID).append("!=").append("?")
                .append(" AND ")
                .append(FieldDefine.TIME).append(">=").append("?")
                .append(" AND ")
                .append(FieldDefine.TIME).append("<=").append("?");

        return sql.toString();
    }
}
