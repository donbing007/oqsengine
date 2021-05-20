package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.SegmentFieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * desc :
 * name : SegmentBuildExecutor
 *
 * @author : leo
 * date : 2021/05/08
 * @since : 1.8
 */
public class SegmentBuildExecutor extends AbstractSegmentExecutor<SegmentInfo, Integer> {

    public SegmentBuildExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    public static SegmentBuildExecutor build(String tableName, TransactionResource<Connection> resource, long timeout) {
        return new SegmentBuildExecutor(tableName, resource, timeout);
    }

    @Override
    public Integer execute(SegmentInfo res) throws SQLException {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
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
