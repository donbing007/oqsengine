package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;

/**
 * desc :
 * name : CDCMetricsCallback
 *
 * @author : xujia
 * date : 2020/11/4
 * @since : 1.8
 */
public interface CDCMetricsCallback {

    /**
     * 提交确认信息
     *
     * @param ackMetrics
     */
    void cdcAck(CDCAckMetrics ackMetrics);

    /**
     * 需要在一个原子操作时保证一致性的信息，保证在宕机后从redis恢复的完整性
     *
     * @param cdcMetrics 指标.
     */
    void cdcSaveLastUnCommit(CDCMetrics cdcMetrics);

    /**
     * 需要在一个原子操作时保证一致性的信息，保证在宕机后从redis恢复的完整性.
     * 查询由 cdcSaveLastUnCommit保存的信息.
     *
     * @return 查询结果.
     */
    CDCMetrics queryLastUnCommit();
}
