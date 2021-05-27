package com.xforceplus.ultraman.oqsengine.idgenerator.storage;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentBuildExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentQueryExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentResetExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentUpdateExecutor;
import java.sql.SQLException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * SqlSegmentStorage.
 *
 * @author leo
 * @version 0.1 2021/5/16 22:11
 * @since 1.8
 */
public class SqlSegmentStorage implements SegmentStorage, Lifecycle {


    @Resource(name = "segmentDataSource")
    private DataSource dataSource;

    private String table;

    private long queryTimeout;


    @Override
    @PostConstruct
    public void init() {

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }
    }


    public void setTable(String table) {
        this.table = table;
    }

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @Override
    public int build(SegmentInfo segmentInfo) throws SQLException {
        return SegmentBuildExecutor.build(table, dataSource, queryTimeout).execute(segmentInfo);
    }

    @Override
    public int udpate(SegmentInfo segmentInfo) throws SQLException {
        return SegmentUpdateExecutor.build(table, dataSource, queryTimeout).execute(segmentInfo);
    }

    public int reset(SegmentInfo segmentInfo) throws SQLException {
        return SegmentResetExecutor.build(table, dataSource, queryTimeout).execute(segmentInfo);
    }

    @Override
    public Optional<SegmentInfo> query(String bizType) throws SQLException {
        return SegmentQueryExecutor.build(table, dataSource, queryTimeout).execute(bizType);
    }
}
