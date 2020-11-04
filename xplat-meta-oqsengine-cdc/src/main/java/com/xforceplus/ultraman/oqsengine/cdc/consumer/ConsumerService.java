package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;

import java.sql.SQLException;
import java.util.List;

/**
 * desc :
 * name : ConsumerService
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public interface ConsumerService {
    /*
        消费当前批次的Binlog
     */
    boolean consume(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException;
}
