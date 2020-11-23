package com.xforceplus.ultraman.oqsengine.status;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;

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
     *
     */
    boolean heartBeat();

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
    boolean save(CDCMetrics cdcMetrics);

    /**
     * 获取最后的CDC指标快照.
     *
     * @return CDC指标快照.
     */
    Optional<CDCMetrics> get();
}
