package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by justin.xu on 04/2021
 */
public class CdcErrorRecoverExecutor extends AbstractDevOpsExecutor<Long, Integer>  {

    private String operationObjectStr;
    private FixedStatus fixedStatus;

    public CdcErrorRecoverExecutor(String tableName, DataSource dataSource, long timeoutMs, FixedStatus fixedStatus, String operationObjectStr) {
        super(tableName, dataSource, timeoutMs);
        this.operationObjectStr = operationObjectStr;
        this.fixedStatus = fixedStatus;
    }

    public static CdcErrorExecutor<Long, Integer>
        build(String tableName, DataSource dataSource, long timeout, FixedStatus fixedStatus, String operationObjectStr) {
        return new CdcErrorRecoverExecutor(tableName, dataSource, timeout, fixedStatus, operationObjectStr);
    }

    @Override
    public Integer execute(Long res) throws SQLException {
        if (null != res && null != operationObjectStr && !operationObjectStr.isEmpty()) {
            String sql = buildSQL();

            try (Connection connection = getDataSource().getConnection();
                 PreparedStatement st = connection.prepareStatement(sql)) {
                st.setInt(1, fixedStatus.ordinal());
                st.setString(2, operationObjectStr);
                st.setLong(3, res);

                checkTimeout(st);

                if (logger.isDebugEnabled()) {
                    logger.debug(st.toString());
                }

                return st.executeUpdate();
            }

        }
        return 0;
    }
    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        buff.append("UPDATE ")
                .append(getTableName())
                .append(" SET ").append(ErrorFieldDefine.STATUS).append("=").append("?");

        buff.append(", ").append(ErrorFieldDefine.OPERATION_OBJECT).append("=").append("?");

        buff.append(" WHERE ").append(ErrorFieldDefine.SEQ_NO).append("=").append("?");

        return buff.toString();
    }
}
