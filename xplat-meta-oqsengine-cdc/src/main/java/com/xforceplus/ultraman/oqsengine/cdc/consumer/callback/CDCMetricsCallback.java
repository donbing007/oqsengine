package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;

/**
 * desc :
 * name : CDCMetricsCallback
 *
 * @author : xujia
 * date : 2020/11/4
 * @since : 1.8
 */
public interface CDCMetricsCallback {

    void cdcCallBack(CDCMetrics cdcMetrics);
}
