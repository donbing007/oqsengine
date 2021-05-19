package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * desc :
 * name : SegmentResetExecutor
 *
 * @author : leo
 * date : 2020/11/22
 * @since : 1.8
 */
public class SegmentResetExecutor extends AbstractSegmentExecutor<SegmentInfo, Integer> {

    public SegmentResetExecutor(String tableName, TransactionResource resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public static SegmentResetExecutor
                                build(String tableName, TransactionResource resource, long timeout) {
        return new SegmentResetExecutor(tableName, resource, timeout);
    }

    @Override
    public Integer execute(SegmentInfo segmentInfo) throws SQLException {
        String sql = buildSQL();

        try (Connection connection = getResource().value();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1,segmentInfo.getPatternKey());
            st.setLong(2, segmentInfo.getId());
            st.setLong(3,segmentInfo.getMaxId());
            st.setLong(4, segmentInfo.getVersion());
            st.setString(5, segmentInfo.getBizType());
            st.setString(6,segmentInfo.getPatternKey());
            checkTimeout(st);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }
            return st.executeUpdate();
        }
    }

    private  String buildSQL() {
        StringBuilder buff = new StringBuilder();
        buff.append("UPDATE ")
                .append(getTableName())
                .append(" SET ").append(SegmentFieldDefine.VERSION).append(" = " )
                .append(String.format("%s + %s",SegmentFieldDefine.VERSION,"1"))
                .append(" , ")
                .append(SegmentFieldDefine.PATTERN_KEY).append(" = ").append("?")
                .append(" , ")
                .append(SegmentFieldDefine.MAX_ID).append(" = ").append(" 0 ")
                .append(" WHERE ").append(SegmentFieldDefine.ID).append(" = ").append("?")
                .append(" AND ").append(SegmentFieldDefine.MAX_ID).append(" = ").append("?")
                .append(" AND ").append(SegmentFieldDefine.VERSION).append(" = ").append("?")
                .append(" AND ").append(SegmentFieldDefine.BIZ_TYPE).append(" = ").append("?")
                .append(" AND ").append(SegmentFieldDefine.PATTERN_KEY).append(" != ").append("?");
        return buff.toString();
    }
}
