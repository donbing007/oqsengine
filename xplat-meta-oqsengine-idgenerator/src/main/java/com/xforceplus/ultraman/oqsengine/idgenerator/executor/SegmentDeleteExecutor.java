package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;


/**
 * SegmentResetExecutor.
 *
 * @author leo
 */
public class SegmentDeleteExecutor extends AbstractSegmentExecutor<SegmentInfo, Integer> {

    public SegmentDeleteExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static SegmentDeleteExecutor build(String tableName, DataSource dataSource, long timeout) {
        return new SegmentDeleteExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Integer execute(SegmentInfo segmentInfo) throws SQLException {
        String sql = buildSQL();
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, segmentInfo.getBizType());
            checkTimeout(st);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }
            return st.executeUpdate();
        }
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        buff.append("DELETE FROM ")
            .append(getTableName())
            .append(" WHERE ").append(SegmentFieldDefine.BIZ_TYPE).append(" = ").append("?");
        return buff.toString();
    }
}
