package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.connect.impl.CustomCanalConnector;
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
    public void init(String connectString, int port, String destination, String userName, String password, String clientId) {

        short clientIdentity = 1001;

        if (null != clientId) {
            clientIdentity = Short.parseShort(clientId);
        }

        canalConnector =
            new CustomCanalConnector(new InetSocketAddress(connectString, port), userName, password, destination, clientIdentity);
    }
}
