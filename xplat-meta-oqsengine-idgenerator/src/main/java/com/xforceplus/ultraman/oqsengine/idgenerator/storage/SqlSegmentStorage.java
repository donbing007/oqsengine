package com.xforceplus.ultraman.oqsengine.idgenerator.storage;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentBuildExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentQueryExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentResetExecutor;
import com.xforceplus.ultraman.oqsengine.idgenerator.executor.SegmentUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:54 AM
 */
public class SqlSegmentStorage implements SegmentStorage, Lifecycle {



    private String table;

    private long queryTimeout;


    @Resource(name = "segmentJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

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
        return (int)transactionExecutor.execute((transaction, resource, hint)
                -> new SegmentBuildExecutor(table,resource,queryTimeout).execute(segmentInfo));
    }

    @Override
    public int udpate(SegmentInfo segmentInfo) throws SQLException {
        return (int)transactionExecutor.execute((transaction, resource, hint)
                -> new SegmentUpdateExecutor(table,resource,queryTimeout).execute(segmentInfo));
    }

    public int reset(SegmentInfo segmentInfo) throws SQLException {
        return (int)transactionExecutor.execute((transaction,resource,hint)
            -> new SegmentResetExecutor(table,resource,queryTimeout).execute(segmentInfo));
    }

    @Override
    public Optional<SegmentInfo> query(String bizType) throws SQLException {
        return (Optional<SegmentInfo>) transactionExecutor.execute((transaction, resource, hint)
                -> new SegmentQueryExecutor(table,resource,queryTimeout).execute(bizType));
    }
}
