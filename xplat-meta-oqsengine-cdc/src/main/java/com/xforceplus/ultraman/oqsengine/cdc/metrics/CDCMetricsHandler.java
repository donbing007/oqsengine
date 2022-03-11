package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public interface CDCMetricsHandler {

    /**
     * 开启Service.
     */
    void init();

    /**
     * 关闭Service.
     */
    void shutdown();

    /**
     * 心跳.
     */
    void heartBeat();

    /**
     * 正常回调.
     */
    void callBackSuccess(CDCMetrics cdcMetrics);

    /**
     * 异常回调.
     */
    void callBackError(CDCMetrics cdcMetrics);

    /**
     * 备份当前的指标.
     * @param cdcMetrics 当前指标对象.
     */
    void backup(CDCMetrics cdcMetrics);

    /**
     * 查询指标.
     * @return 指标对象.
     */
    CDCMetrics query() throws SQLException;

    /**
     * 查询当前的commitIds列表是否处于可操作状态.
     * 没有准备将进行等待.
     * @param commitIds 提交号列表.
     */
    void isReady(List<Long> commitIds);


    /**
     * 表示一个新的连接的回调.
     * @param cdcMetrics 指标对象.
     */
    void renewConnect(CDCMetrics cdcMetrics);
}


