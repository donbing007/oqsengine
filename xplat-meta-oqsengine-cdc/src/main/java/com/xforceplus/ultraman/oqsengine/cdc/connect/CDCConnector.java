package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnector;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.DEFAULT_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.DEFAULT_SUBSCRIBE_FILTER;

/**
 * desc :
 * name : CDCConnector
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class CDCConnector {

    private String subscribeFilter = DEFAULT_SUBSCRIBE_FILTER;

    private int batchSize = DEFAULT_BATCH_SIZE;

    protected CanalConnector canalConnector;

    public CanalConnector getCanalConnector() {
        return canalConnector;
    }

    public String getSubscribeFilter() {
        return subscribeFilter;
    }

    public void setSubscribeFilter(String subscribeFilter) {
        this.subscribeFilter = subscribeFilter;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
