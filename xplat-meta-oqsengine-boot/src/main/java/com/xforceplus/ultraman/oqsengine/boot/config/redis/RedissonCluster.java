package com.xforceplus.ultraman.oqsengine.boot.config.redis;

/**
 * 集群模式.
 *
 * @author dongbin
 * @version 0.1 2021/12/8 16:22
 * @since 1.8
 */
public class RedissonCluster {

    private boolean enabled = false;
    private int scanInterval = 1000;
    private String[] nodeAddresses = new String[] {
        "redis://127.0.0.1:7004",
        "redis://127.0.0.1:7001",
        "redis://127.0.0.1:7000"
    };

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    public String[] getNodeAddresses() {
        return nodeAddresses;
    }

    public void setNodeAddresses(String[] nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
    }
}
