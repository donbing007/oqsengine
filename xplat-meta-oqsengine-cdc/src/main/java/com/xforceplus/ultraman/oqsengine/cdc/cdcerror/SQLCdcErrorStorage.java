package com.xforceplus.ultraman.oqsengine.cdc.cdcerror;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorBuildExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorQueryExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorRecoverExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.sql.SQLException;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 基于SQL的CDC错误信息记录.
 *
 * @author xujia 2020/11/21
 * @since 1.8
 */
public class SQLCdcErrorStorage implements CdcErrorStorage {

    @Resource(name = "devOpsDataSource")
    private DataSource devOpsDataSource;

    private String cdcErrorRecordTable;

    private long queryTimeout;


    public void setCdcErrorRecordTable(String cdcErrorRecordTable) {
        this.cdcErrorRecordTable = cdcErrorRecordTable;
    }

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @PostConstruct
    public void init() {
        if (queryTimeout <= 0) {
            setQueryTimeout(10_000L);
        }
    }

    @Override
    public int buildCdcError(CdcErrorTask cdcErrorTask) throws SQLException {
        return CdcErrorBuildExecutor
            .build(cdcErrorRecordTable, devOpsDataSource, queryTimeout)
            .execute(cdcErrorTask);
    }

    @Override
    public int updateCdcError(long seqNo, FixedStatus fixedStatus) throws SQLException {
        return CdcErrorUpdateExecutor
            .build(cdcErrorRecordTable, devOpsDataSource, queryTimeout, fixedStatus)
            .execute(seqNo);
    }

    @Override
    public int submitRecover(long seqNo, FixedStatus fixedStatus, String operationObjectString) throws SQLException {
        return CdcErrorRecoverExecutor
            .build(cdcErrorRecordTable, devOpsDataSource, queryTimeout, fixedStatus, operationObjectString)
            .execute(seqNo);
    }

    @Override
    public Collection<CdcErrorTask> queryCdcErrors(CdcErrorQueryCondition res) throws SQLException {
        return CdcErrorQueryExecutor
            .build(cdcErrorRecordTable, devOpsDataSource, queryTimeout)
            .execute(res);
    }
}
