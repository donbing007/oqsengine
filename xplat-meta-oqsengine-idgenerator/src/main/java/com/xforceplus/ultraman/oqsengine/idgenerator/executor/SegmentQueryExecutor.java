package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * SegmentQueryExecutor.
 *
 * @author leo
 */
public class SegmentQueryExecutor extends AbstractSegmentExecutor<String, Optional<SegmentInfo>> {

    public SegmentQueryExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static AbstractSegmentExecutor<String, Optional<SegmentInfo>> build(String tableName, DataSource dataSource,
                                                                               long timeout) {
        return new SegmentQueryExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Optional<SegmentInfo> execute(String bizType) throws Exception {
        String sql = buildSQL();
        SegmentInfo entity = null;
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, bizType);
            checkTimeout(st);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    SegmentInfo.SegmentBuilder segmentBuilder = SegmentInfo.builder();
                    entity = segmentBuilder.withBeginId(rs.getLong(SegmentFieldDefine.BEGIN_ID))
                        .withBizType(rs.getString(SegmentFieldDefine.BIZ_TYPE))
                        .withCreateTime(rs.getTimestamp(SegmentFieldDefine.CREATE_TIME))
                        .withUpdateTime(rs.getTimestamp(SegmentFieldDefine.UPDATE_TIME))
                        .withId(rs.getLong(SegmentFieldDefine.ID))
                        .withMaxId(rs.getLong(SegmentFieldDefine.MAX_ID))
                        .withMode(rs.getInt(SegmentFieldDefine.MODE))
                        .withPatten(rs.getString(SegmentFieldDefine.PATTERN))
                        .withPatternKey(rs.getString(SegmentFieldDefine.PATTERN_KEY))
                        .withResetable(rs.getInt(SegmentFieldDefine.RESETABLE))
                        .withStep(rs.getInt(SegmentFieldDefine.STEP))
                        .withVersion(rs.getLong(SegmentFieldDefine.VERSION)).build();

                }

                return Optional.ofNullable(entity);
            }
        }
    }

    /**
     * Get sql of build.
     *
     * @return sql
     */
    public String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
            .append(String.join(",",
                SegmentFieldDefine.ID,
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
            )
            .append(" FROM ")
            .append(getTableName());
        sql.append(" WHERE ")
            .append("biz_type = ?");
        return sql.toString();
    }
}
