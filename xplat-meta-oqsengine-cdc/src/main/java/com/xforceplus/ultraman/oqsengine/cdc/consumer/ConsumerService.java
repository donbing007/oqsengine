package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCUnCommitMetrics;

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
    CDCMetrics consume(List<CanalEntry.Entry> entries, CDCUnCommitMetrics cdcUnCommitMetrics) throws SQLException;
}
