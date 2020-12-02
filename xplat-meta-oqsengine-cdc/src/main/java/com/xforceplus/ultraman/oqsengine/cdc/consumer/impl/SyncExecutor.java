package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

import java.sql.SQLException;
import java.util.Collection;

/**
 * desc :
 * name : SyncExecutor
 *
 * @author : xujia
 * date : 2020/12/1
 * @since : 1.8
 */
public interface SyncExecutor {
    int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException;

    void errorRecord(long id, long commitId, String message) throws SQLException;
}
