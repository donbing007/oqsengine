package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import io.micrometer.core.annotation.Timed;

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
    CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCUnCommitMetrics cdcUnCommitMetrics) throws SQLException;
}
