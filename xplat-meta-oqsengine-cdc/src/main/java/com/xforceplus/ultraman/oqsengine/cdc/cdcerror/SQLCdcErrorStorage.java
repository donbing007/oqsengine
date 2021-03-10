package com.xforceplus.ultraman.oqsengine.cdc.cdcerror;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorBuildExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorQueryExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

/**
 * desc :
 * name : SQLCdcErrorStorage
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
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
    public Collection<CdcErrorTask> queryCdcErrors(CdcErrorQueryCondition res) throws SQLException {
        return CdcErrorQueryExecutor
                .build(cdcErrorRecordTable, devOpsDataSource, queryTimeout)
                .execute(res);
    }
}
