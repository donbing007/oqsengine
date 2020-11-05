package com.xforceplus.ultraman.oqsengine.cdc.consumer.callback;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;

/**
 * desc :
 * name : TestCallbackService
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class TestCallbackService implements CDCMetricsCallback {
    @Override
    public void cdcCallBack(CDCMetrics cdcMetrics) {
        System.out.println(cdcMetrics.toString());
    }
}
