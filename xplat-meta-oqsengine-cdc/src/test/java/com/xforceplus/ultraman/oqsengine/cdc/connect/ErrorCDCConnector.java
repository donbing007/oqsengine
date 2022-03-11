package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnectors;
import java.net.InetSocketAddress;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ErrorCDCConnector extends SingleCDCConnector {

    public int renewConnect;

    public boolean shouldOpen;

    public ErrorCDCConnector(String connectString, String destination, String userName, String password, int port) {
        super(connectString, destination, userName, password, port);
        renewConnect = 0;
    }

    /**
     * 初始化.
     */
    @Override
    public void init() {
        shouldOpen = true;
        canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(connectString,
            port), destination, userName, password);

        renewConnect++;
    }

    /**
     * 打开canal连接.
     */
    public void open() {
        if (shouldOpen) {
            super.open();
            return;
        }
        throw new RuntimeException("mock error connector...");
    }

    public void setShouldOpen(boolean shouldOpen) {
        this.shouldOpen = shouldOpen;
    }

    public int getRenewConnect() {
        return renewConnect;
    }

}
