package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import javax.sql.DataSource;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/7/21 6:25 PM
 */
public class SegmentBuildExecutor extends AbstractSegmentExecutor<SegmentInfo, Integer> {

    public SegmentBuildExecutor(String tableName, DataSource resource, long timeout) {
        super(tableName, resource, timeout);
    }

    public static SegmentBuildExecutor build(String tableName, DataSource dataSource, long timeout) {
        return new SegmentBuildExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Integer execute(SegmentInfo res) throws SQLException {
        String sql = buildSQL();
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            int pos = 1;
            st.setString(pos++, res.getBizType());
            st.setLong(pos++, res.getBeginId());
            st.setLong(pos++, res.getMaxId());
            st.setInt(pos++, res.getStep());
            st.setString(pos++, res.getPattern());
            st.setString(pos++, res.getPatternKey());
            st.setInt(pos++, res.getResetable());
            st.setInt(pos++, res.getMode());
            st.setLong(pos++, res.getVersion());
            st.setTimestamp(pos++, res.getCreateTime());
            st.setTimestamp(pos, res.getUpdateTime());
            checkTimeout(st);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }
            return st.executeUpdate();
        }

    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        buff.append("INSERT INTO ").append(getTableName())
            .append(' ')
            .append("(")
            .append(String.join(",",
                SegmentFieldDefine.BIZ_TYPE,
                SegmentFieldDefine.BEGIN_ID,
                SegmentFieldDefine.MAX_ID,
                SegmentFieldDefine.STEP,
                SegmentFieldDefine.PATTERN,
                SegmentFieldDefine.PATTERN_KEY,
                SegmentFieldDefine.RESETABLE,
                SegmentFieldDefine.MODE,
                SegmentFieldDefine.VERSION,
                SegmentFieldDefine.CREATE_TIME,
                SegmentFieldDefine.UPDATE_TIME)
            ).append(") VALUES (")
            .append(String.join(",", Collections.nCopies(11, "?")))
            .append(")");
        return buff.toString();
    }
}
