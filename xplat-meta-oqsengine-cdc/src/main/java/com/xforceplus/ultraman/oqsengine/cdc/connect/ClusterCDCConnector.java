package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnectors;


/**
 * 集群模式连接.
 *
 * @author xujia 2020/11/5
 * @since : 1.8
 */
public class ClusterCDCConnector extends AbstractCDCConnector {

    public ClusterCDCConnector(String connectString, String destination, String userName, String password) {
        super(connectString, destination, userName, password);
    }

    @Override
    public void init() {
        canalConnector = CanalConnectors.newClusterConnector(connectString, destination, userName, password);
    }
}
