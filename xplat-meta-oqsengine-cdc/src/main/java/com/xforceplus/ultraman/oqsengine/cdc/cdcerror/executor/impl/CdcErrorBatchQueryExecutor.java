package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CdcErrorBatchQueryExecutor extends AbstractDevOpsExecutor<Collection<String>, Collection<CdcErrorTask>>{

    public CdcErrorBatchQueryExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static CdcErrorExecutor<Collection<String>, Collection<CdcErrorTask>> build(
        String tableName, DataSource dataSource, long timeoutMs) {
        return new CdcErrorBatchQueryExecutor(tableName, dataSource, timeoutMs);
    }

    @Override
    public Collection<CdcErrorTask> execute(Collection<String> res) throws SQLException {
        if (res.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = String.format(buildSQL(),
            res.stream().map(e -> {return "'" + e + "'";}).collect(Collectors.joining(",")));

        try (Connection connection = getDataSource().getConnection();
            PreparedStatement st = connection.prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            ResultSet rs = null;
            List<CdcErrorTask> cdcErrorTasks = new ArrayList<>();

            rs = st.executeQuery();

            CdcErrorTask cdcErrorTask = null;
            while (rs.next()) {
                cdcErrorTask = new CdcErrorTask();
                cdcErrorTask.setSeqNo(rs.getLong(ErrorFieldDefine.SEQ_NO));
                cdcErrorTask.setUniKey(rs.getString(ErrorFieldDefine.UNI_KEY));
                cdcErrorTask.setBatchId(rs.getLong(ErrorFieldDefine.BATCH_ID));
                cdcErrorTask.setId(rs.getLong(ErrorFieldDefine.ID));
                cdcErrorTask.setEntity(rs.getLong(ErrorFieldDefine.ENTITY));
                cdcErrorTask.setVersion(rs.getInt(ErrorFieldDefine.VERSION));
                cdcErrorTask.setOp(rs.getInt(ErrorFieldDefine.OP));
                cdcErrorTask.setCommitId(rs.getLong(ErrorFieldDefine.COMMIT_ID));
                cdcErrorTask.setErrorType(rs.getInt(ErrorFieldDefine.TYPE));
                cdcErrorTask.setStatus(rs.getInt(ErrorFieldDefine.STATUS));
                cdcErrorTask.setOperationObject(rs.getString(ErrorFieldDefine.OPERATION_OBJECT));
                cdcErrorTask.setMessage(rs.getString(ErrorFieldDefine.MESSAGE));
                cdcErrorTask.setExecuteTime(rs.getLong(ErrorFieldDefine.EXECUTE_TIME));
                cdcErrorTask.setFixedTime(rs.getLong(ErrorFieldDefine.FIXED_TIME));
                cdcErrorTasks.add(cdcErrorTask);
            }

            return cdcErrorTasks;
        }
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();

        buff.append("SELECT ")
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
            )
            .append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(ErrorFieldDefine.UNI_KEY)
            .append(" IN (%s) ")
            .append("order by ").append(ErrorFieldDefine.EXECUTE_TIME).append(" desc");

        return buff.toString();
    }
}
