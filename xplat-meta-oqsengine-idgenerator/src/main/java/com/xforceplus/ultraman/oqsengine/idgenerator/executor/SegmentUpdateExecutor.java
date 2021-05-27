package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;


/**
 * SegmentUpdateExecutor.
 *
 * @author leo
 */
public class SegmentUpdateExecutor extends AbstractSegmentExecutor<SegmentInfo, Integer> {

    public SegmentUpdateExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static SegmentUpdateExecutor build(String tableName, DataSource dataSource, long timeout) {
        return new SegmentUpdateExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Integer execute(SegmentInfo segmentInfo) throws SQLException {
        String sql = buildSQL();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, segmentInfo.getId());
            st.setLong(2, segmentInfo.getMaxId());
            st.setLong(3, segmentInfo.getVersion());
            st.setString(4, segmentInfo.getBizType());
            checkTimeout(st);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }
            return st.executeUpdate();
        }
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        buff.append("UPDATE ")
            .append(getTableName())
            .append(" SET ").append(SegmentFieldDefine.VERSION).append(" = ")
            .append(String.format("%s + %s", SegmentFieldDefine.VERSION, "1"))
            .append(" , ")
            .append(SegmentFieldDefine.MAX_ID).append(" = ")
            .append(String.format("%s + %s", SegmentFieldDefine.MAX_ID, SegmentFieldDefine.STEP))
            .append(" WHERE ").append(SegmentFieldDefine.ID).append(" = ").append("?")
            .append(" AND ").append(SegmentFieldDefine.MAX_ID).append(" = ").append("?")
            .append(" AND ").append(SegmentFieldDefine.VERSION).append(" = ").append("?")
            .append(" AND ").append(SegmentFieldDefine.BIZ_TYPE).append(" = ").append("?");
        return buff.toString();
    }
}
