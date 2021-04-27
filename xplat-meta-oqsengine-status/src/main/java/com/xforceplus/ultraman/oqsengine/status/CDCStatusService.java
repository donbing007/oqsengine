package com.xforceplus.ultraman.oqsengine.status;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CDC状态服务.
 *
 * @author dongbin
 * @version 0.1 2020/11/16 15:42
 * @since 1.8
 */
public interface CDCStatusService {

    /**
     * 保存HEART_BEAT.
     */
    boolean heartBeat();

    /**
     * 当前commitId处于卡住状态
     *
     */
    void notReady(long commitId);

    /**
     * 判断CDC是否健康.
     *
     * @return true健康, false不健康.
     */
    boolean isAlive();

    /**
     * 保存CDC指标.
     *
     * @param cdcMetrics CDC指标.
     */
    boolean saveUnCommit(CDCMetrics cdcMetrics);

    /**
     * 获取最后的CDC指标快照.
     *
     * @return CDC指标快照.
     */
    Optional<CDCMetrics> getUnCommit();

    /**
     * 保存CDC处理响应状态.
     *
     * @param ackMetrics 响应信息.
     * @return true保存成功, false保存失败.
     */
    boolean saveAck(CDCAckMetrics ackMetrics);

    /**
     * 查询最后保存的CDC响应状态.
     *
     * @return 响应快照.
     */
    Optional<CDCAckMetrics> getAck();
}
