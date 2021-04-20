package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

import java.util.List;
import java.util.Map;

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
     * 心跳
     *
     */
    void heartBeat();

    /**
     * 当前commitId处于卡住状态
     *
     */
    void notReady(long commitId);

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

    /**
     * 判断当前的CommitId是否可以插入
     *
     * @param commitId
     */
    boolean isReadyCommit(long commitId);

    Map<String, String> querySkipRows();

    void expiredSkipRows(String[] skips);
}



