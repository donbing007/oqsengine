package com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.DevOpsExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.ErrorFieldDefine;

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

    public static DevOpsExecutor<CdcErrorTask, Integer> build(String tableName, DataSource dataSource, long timeout) {
        return new CdcErrorBuildExecutor(tableName, dataSource, timeout);
    }

    @Override
    public Integer execute(CdcErrorTask res) throws SQLException {
        String sql = buildSQL();
        try (Connection connection = getDataSource().getConnection();
                        PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, res.getSeqNo());
            st.setLong(2, res.getId());
            st.setLong(3, res.getCommitId());
            st.setInt(4, res.getStatus());
            st.setString(5, res.getMessage());
            st.setLong(6, res.getExecuteTime());
            st.setLong(7, res.getFixedTime());

            checkTimeout(st);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        // insert into ${table} (seqno, id, commitid, status, message, executetime, fixedtime) values(?,?,?,?,?,?,?)
        buff.append("INSERT INTO ").append(getTableName())
                .append(' ')
                .append("(")
                .append(String.join(",",
                            ErrorFieldDefine.SEQ_NO,
                            ErrorFieldDefine.ID,
                            ErrorFieldDefine.COMMIT_ID,
                            ErrorFieldDefine.STATUS,
                            ErrorFieldDefine.MESSAGE,
                            ErrorFieldDefine.EXECUTE_TIME,
                            ErrorFieldDefine.FIXED_TIME)
                ).append(") VALUES (")
                .append(String.join(",", Collections.nCopies(7, "?")))
                .append(")");
        return buff.toString();
    }
}
