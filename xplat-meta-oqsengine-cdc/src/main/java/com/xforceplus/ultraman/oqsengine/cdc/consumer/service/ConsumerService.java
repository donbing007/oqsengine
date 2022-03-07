package com.xforceplus.ultraman.oqsengine.cdc.consumer.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.List;

/**
 * desc :.
 * name : ConsumerService
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public interface ConsumerService {
    /**
     * 消费当前批次的Binlog.
     */
    CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCMetrics cdcMetrics)
        throws SQLException;
}
