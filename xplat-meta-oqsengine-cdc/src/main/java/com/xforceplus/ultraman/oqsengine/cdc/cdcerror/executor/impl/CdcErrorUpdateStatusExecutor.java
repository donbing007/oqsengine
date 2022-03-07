package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * CDC error 更新执行器.
 *
 * @author xujia 2020/11/22
 * @since : 1.8
 */
public class CdcErrorUpdateStatusExecutor extends AbstractDevOpsExecutor<Long, Integer> {
    private FixedStatus fixedStatus;

    public CdcErrorUpdateStatusExecutor(String tableName, DataSource dataSource, long timeoutMs, FixedStatus fixedStatus) {
        super(tableName, dataSource, timeoutMs);
        this.fixedStatus = fixedStatus;
    }

    public static CdcErrorExecutor<Long, Integer> build(
        String tableName, DataSource dataSource, long timeout, FixedStatus fixedStatus) {
        return new CdcErrorUpdateStatusExecutor(tableName, dataSource, timeout, fixedStatus);
    }

    @Override
    public Integer execute(Long res) throws SQLException {
        String sql = buildSQL();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, fixedStatus.getStatus());
            st.setLong(2, System.currentTimeMillis());
            st.setLong(3, res);

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
            .append(" SET ").append(ErrorFieldDefine.STATUS).append("=").append("?");

        buff.append(", ").append(ErrorFieldDefine.FIXED_TIME).append("=").append("?");

        buff.append(" WHERE ").append(ErrorFieldDefine.SEQ_NO).append("=").append("?");

        return buff.toString();
    }
}
