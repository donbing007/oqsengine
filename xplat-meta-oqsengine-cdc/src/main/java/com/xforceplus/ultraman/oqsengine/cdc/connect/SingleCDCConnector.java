package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnectors;

import java.net.InetSocketAddress;

/**
 * desc :
 * name : SingleCDCConnector
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class SingleCDCConnector extends CDCConnector {

    public void init(String connectString, int port, String destination, String userName, String password) {
        canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(connectString,
                port), destination, userName, password);
    }
}
