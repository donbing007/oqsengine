package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CdcErrorBatchInsertExecutor extends AbstractDevOpsExecutor<Collection<CdcErrorTask>, Boolean> {

    private static final String VALUES_TEMPLATE = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public CdcErrorBatchInsertExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static CdcErrorExecutor<Collection<CdcErrorTask>, Boolean> build(
        String tableName, DataSource dataSource, long timeoutMs) {
        return new CdcErrorBatchInsertExecutor(tableName, dataSource, timeoutMs);
    }

    @Override
    public Boolean execute(Collection<CdcErrorTask> cdcErrors) throws SQLException {
        String sql = buildSql(cdcErrors.size());

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            for (CdcErrorTask res : cdcErrors) {

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

                st.addBatch();
            }

            int[] flags = st.executeBatch();

            int success = 0;
            for (int i = 0; i < flags.length; i++) {
                if (flags[i] > 0 || flags[i] == Statement.SUCCESS_NO_INFO) {
                    success++;
                }
            }

            return success == cdcErrors.size();
        }

    }

    private String buildSql(int size) {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO ")
            .append(getTableName())
            .append(" (")
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
                ErrorFieldDefine.FIXED_TIME
            ))
            .append(") VALUES ")
            .append(VALUES_TEMPLATE);

        return sql.toString();
    }
}
