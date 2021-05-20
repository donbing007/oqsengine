package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * desc :
 * name : CdcErrorQueryExecutor
 *
 * @author : leo
 * date : 2020/11/22
 * @since : 1.8
 */
public class SegmentQueryExecutor extends AbstractSegmentExecutor<String, Optional<SegmentInfo>> {

    public SegmentQueryExecutor(String tableName, TransactionResource resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public static AbstractSegmentExecutor<String, Optional<SegmentInfo>>
    build(String tableName, TransactionResource resource, long timeout) {
        return new SegmentQueryExecutor(tableName, resource, timeout);
    }

    @Override
    public Optional<SegmentInfo> execute(String bizType) throws SQLException {
        String sql = buildSQL();
        SegmentInfo entity = null;
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
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
        catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

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
