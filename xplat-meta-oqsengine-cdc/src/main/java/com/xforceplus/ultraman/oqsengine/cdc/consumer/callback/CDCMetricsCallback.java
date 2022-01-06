package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.util.List;

/**
 * CDC 指标信息的通知回调.
 *
 * @author xujia 2020/11/4
 * @since : 1.8
 */
public interface CDCMetricsCallback {

    /**
     * 提交确认信息.
     *
     * @param ackMetrics 确认指标.
     */
    void ack(CDCAckMetrics ackMetrics);

    /**
     * 心跳.
     */
    void heartBeat();

    /**
     * 当前commitId处于卡住状态.
     */
    void notReady(long commitId);

    /**
     * 需要在一个原子操作时保证一致性的信息，保证在宕机后从redis恢复的完整性.
     *
     * @param cdcMetrics 指标.
     */
    void saveLastUnCommit(CDCMetrics cdcMetrics);

    /**
     * 需要在一个原子操作时保证一致性的信息，保证在宕机后从redis恢复的完整性.
     * 查询由 cdcSaveLastUnCommit保存的信息.
     *
     * @return 查询结果.
     */
    CDCMetrics queryLastUnCommit();

    /**
     * 判断当前的CommitId是否可以插入.
     *
     * @param commitId 提交号.
     */
    boolean isReady(long commitId);


    /**
     * 判断当前批量commitIds中没有ready的Ids.
     *
     * @param commitIds 提交号.
     */
    List<Long> notReady(List<Long> commitIds);
}



