package com.xforceplus.ultraman.oqsengine.cdc.consumer.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
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
    CDCMetrics consumeOneBatch(List<CanalEntry.Entry> entries, long batchId, CDCMetrics cdcMetrics)
        throws SQLException;

    /**
     * 获取metricsHandler.
     *
     * @return 获取metricsHandler.
     */
    CDCMetricsHandler metricsHandler();
}
