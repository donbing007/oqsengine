package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * desc :
 * name : SyncExecutor
 *
 * @author : xujia
 * date : 2020/12/1
 * @since : 1.8
 */
public interface SyncExecutor {
    int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics, Map<String, String> skips) throws SQLException;

    boolean doErrRecordOrRecover(Long batchId, Long id, Long commitId, ErrorType errorType, String message, List<OriginalEntity> entities) throws SQLException;
}
