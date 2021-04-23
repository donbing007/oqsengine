package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.ErrorFieldDefine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * desc :
 * name : CdcErrorQueryExecutor
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
public class CdcErrorQueryExecutor extends AbstractDevOpsExecutor<CdcErrorQueryCondition, Collection<CdcErrorTask>> {

    public CdcErrorQueryExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        super(tableName, dataSource, timeoutMs);
    }

    public static CdcErrorExecutor<CdcErrorQueryCondition, Collection<CdcErrorTask>>
                                        build(String tableName, DataSource dataSource, long timeout) {
        return new CdcErrorQueryExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Collection<CdcErrorTask> execute(CdcErrorQueryCondition res) throws SQLException {
        StringBuilder buff = new StringBuilder();
        boolean hasWhereCondition = buildSQL(buff, res);

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement st = connection.prepareStatement(buff.toString())) {
            if (hasWhereCondition) {
                int parameterIndex = 1;
                //  add seqNo
                if (null != res.getSeqNo()) {
                    st.setLong(parameterIndex++, res.getSeqNo());
                }

                //  add batch
                if (null != res.getBatchId()) {
                    st.setLong(parameterIndex++, res.getBatchId());
                }

                //  add id
                if (null != res.getId()) {
                    st.setLong(parameterIndex++, res.getId());
                }

                //  add commitId
                if (null != res.getCommitId()) {
                    st.setLong(parameterIndex++, res.getCommitId());
                }

                //  add type
                if (null != res.getType()) {
                    st.setInt(parameterIndex++, res.getType());
                }

                //  add status
                if (null != res.getStatus()) {
                    st.setInt(parameterIndex++, res.getStatus());
                }

                //  add rangeLEExecuteTime
                if (null != res.getRangeLEExecuteTime()) {
                    st.setLong(parameterIndex++, res.getRangeLEExecuteTime());
                }

                //  add rangeGeExecuteTime
                if (null != res.getRangeGeExecuteTime()) {
                    st.setLong(parameterIndex++, res.getRangeGeExecuteTime());
                }

                //  add rangeLEFixedTime
                if (null != res.getRangeLEFixedTime()) {
                    st.setLong(parameterIndex++, res.getRangeLEFixedTime());
                }

                //  add rangeLEFixedTime
                if (null != res.getRangeGeFixedTime()) {
                    st.setLong(parameterIndex, res.getRangeGeFixedTime());
                }
            }

            checkTimeout(st);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            ResultSet rs = null;
            List<CdcErrorTask> cdcErrorTasks = new ArrayList<>();
            CdcErrorTask cdcErrorTask;
            try {
                rs = st.executeQuery();

                while (rs.next()) {
                    cdcErrorTask = new CdcErrorTask();
                    cdcErrorTask.setSeqNo(rs.getLong(ErrorFieldDefine.SEQ_NO));
                    cdcErrorTask.setBatchId(rs.getLong(ErrorFieldDefine.BATCH_ID));
                    cdcErrorTask.setId(rs.getLong(ErrorFieldDefine.ID));
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

            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    }

    public boolean buildSQL(StringBuilder buff, CdcErrorQueryCondition res) {
        buff.append("SELECT ")
                .append(String.join(",",
                        ErrorFieldDefine.SEQ_NO,
                        ErrorFieldDefine.BATCH_ID,
                        ErrorFieldDefine.ID,
                        ErrorFieldDefine.COMMIT_ID,
                        ErrorFieldDefine.TYPE,
                        ErrorFieldDefine.STATUS,
                        ErrorFieldDefine.OPERATION_OBJECT,
                        ErrorFieldDefine.MESSAGE,
                        ErrorFieldDefine.EXECUTE_TIME,
                        ErrorFieldDefine.FIXED_TIME)
                )
                .append(" FROM ")
                .append(getTableName());

        String conditionString = res.conditionToQuerySql();

        boolean haveCondition = false;
        if (!conditionString.isEmpty()) {
            buff.append(" WHERE ")
                    .append(conditionString);
            haveCondition = true;
        }

        buff.append(" order by ").append(ErrorFieldDefine.EXECUTE_TIME).append(" desc");

        return haveCondition;
    }
}
