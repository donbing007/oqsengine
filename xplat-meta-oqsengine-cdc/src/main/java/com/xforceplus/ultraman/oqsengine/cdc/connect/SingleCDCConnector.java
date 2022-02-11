package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnectors;
import java.net.InetSocketAddress;

/**
 * 非集群模式连接器.
 *
 * @author xujia 2020/11/5
 * @since : 1.8
 */
public class SingleCDCConnector extends AbstractCDCConnector {

    /**
     * init.
     */
    public void init(String connectString, int port, String destination, String userName, String password) {
        canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(connectString,
            port), destination, userName, password);
    }
}
