package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * desc :
 * name : CdcErrorBuildExecutor
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public class CdcErrorBuildExecutor extends AbstractDevOpsExecutor<CdcErrorTask, Integer> {

    public CdcErrorBuildExecutor(String tableName, DataSource dataSource, long timeout) {
        super(tableName, dataSource, timeout);
    }

    public static CdcErrorExecutor<CdcErrorTask, Integer> build(String tableName, DataSource dataSource, long timeout) {
        return new CdcErrorBuildExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Integer execute(CdcErrorTask res) throws SQLException {
        String sql = buildSQL();
        try (Connection connection = getDataSource().getConnection();
                        PreparedStatement st = connection.prepareStatement(sql)) {

            int pos = 1;
            st.setLong(pos++, res.getSeqNo());
            st.setString(pos++, res.getUniKey());
            st.setLong(pos++, res.getBatchId());
            st.setLong(pos++, res.getId());
            st.setLong(pos++, res.getEntity());
            st.setInt(pos++, res.getVersion());
            st.setInt(pos++, res.getOp());
            st.setLong(pos++, res.getCommitId());
            st.setInt(pos++, res.getErrorType());
            st.setInt(pos++, res.getStatus());
            st.setString(pos++, res.getOperationObject());
            st.setString(pos++, res.getMessage());
            st.setLong(pos++, res.getExecuteTime());
            st.setLong(pos, res.getFixedTime());

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
                            ErrorFieldDefine.SEQ_NO,
                            ErrorFieldDefine.UNI_KEY,
                            ErrorFieldDefine.BATCH_ID,
                            ErrorFieldDefine.ID,
                            ErrorFieldDefine.ENTITY,
                            ErrorFieldDefine.VERSION,
                            ErrorFieldDefine.OP,
                            ErrorFieldDefine.COMMIT_ID,
                            ErrorFieldDefine.TYPE,
                            ErrorFieldDefine.STATUS,
                            ErrorFieldDefine.OPERATION_OBJECT,
                            ErrorFieldDefine.MESSAGE,
                            ErrorFieldDefine.EXECUTE_TIME,
                            ErrorFieldDefine.FIXED_TIME)
                ).append(") VALUES (")
                .append(String.join(",", Collections.nCopies(14, "?")))
                .append(")");
        return buff.toString();
    }
}
