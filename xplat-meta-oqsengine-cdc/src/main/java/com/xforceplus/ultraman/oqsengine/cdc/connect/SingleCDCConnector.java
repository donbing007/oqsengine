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

    protected int port;

    public SingleCDCConnector(String connectString, String destination, String userName, String password, int port) {
        super(connectString, destination, userName, password);
        this.port = port;
    }

    @Override
    public void init() {
        canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(connectString,
            port), destination, userName, password);
    }
}
