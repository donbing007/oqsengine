package com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.DevOpsExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.ErrorFieldDefine;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.FixedStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * desc :
 * name : CdcErrorUpdateExecutor
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
public class CdcErrorUpdateExecutor extends AbstractDevOpsExecutor<Long, Integer> {
    private FixedStatus fixedStatus;

    public CdcErrorUpdateExecutor(String tableName, DataSource dataSource, long timeoutMs, FixedStatus fixedStatus) {
        super(tableName, dataSource, timeoutMs);
        this.fixedStatus = fixedStatus;
    }

    public static DevOpsExecutor<Long, Integer>
                                build(String tableName, DataSource dataSource, long timeout, FixedStatus fixedStatus) {
        return new CdcErrorUpdateExecutor(tableName, dataSource, timeout, fixedStatus);
    }

    @Override
    public Integer execute(Long res) throws SQLException {
        String sql = buildSQL();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, fixedStatus.ordinal());
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
