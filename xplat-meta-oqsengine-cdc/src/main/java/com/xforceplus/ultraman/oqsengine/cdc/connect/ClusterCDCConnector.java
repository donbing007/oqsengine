package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnectors;


/**
 * desc :
 * name : ClusterCanalConnector
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class ClusterCDCConnector extends CDCConnector {

    public void init(String connectString, String destination, String userName, String password) {
        canalConnector = CanalConnectors.newClusterConnector(connectString, destination, userName, password);
    }
}
