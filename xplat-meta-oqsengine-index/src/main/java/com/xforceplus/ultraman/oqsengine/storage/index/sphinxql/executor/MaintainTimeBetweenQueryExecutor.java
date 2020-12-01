package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author : xujia
 * date : 2020/11/18
 * @since : 1.8
 */
public class MaintainTimeBetweenQueryExecutor implements Executor<Long, Collection<EntityRef>> {

    final Logger logger = LoggerFactory.getLogger(MaintainTimeBetweenQueryExecutor.class);

    private long entityId;
    private long maintainId;
    private long start;
    private long end;
    private String indexTableName;

    private DataSource resource;

    public MaintainTimeBetweenQueryExecutor(DataSource resource, String indexTableName, long entityId, long maintainId, long start, long end) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        this.entityId = entityId;
        this.maintainId = maintainId;
        this.start = start;
        this.end = end;
    }

    public static MaintainTimeBetweenQueryExecutor build(DataSource resource, String indexTableName, long entityId, long maintainId, long start, long end) {
        return new MaintainTimeBetweenQueryExecutor(resource, indexTableName, entityId, maintainId, start, end);
    }

    @Override
    public Collection<EntityRef> execute(Long aLong) throws SQLException {

        ResultSet rs = null;
        String sql = buildCleanSelect(indexTableName);
        try (Connection connection = resource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            // entityId
            st.setLong(1, entityId);
            // maintainId
            st.setLong(2, maintainId);
            // start
            st.setLong(3, start);
            // end
            st.setLong(4, end);

            List<EntityRef> entityRefList = new ArrayList<>();
            rs = st.executeQuery();
            while (rs.next()) {
                entityRefList.add(
                    new EntityRef(
                        rs.getLong(FieldDefine.ID),
                        rs.getLong(FieldDefine.PREF),
                        rs.getLong(FieldDefine.CREF),
                        rs.getInt(FieldDefine.OQS_MAJOR)
                    )
                );
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
            FieldDefine.CREF,
            FieldDefine.OQS_MAJOR
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
