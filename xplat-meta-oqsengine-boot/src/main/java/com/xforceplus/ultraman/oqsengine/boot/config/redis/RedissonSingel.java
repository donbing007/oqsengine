package com.xforceplus.ultraman.oqsengine.boot.config.redis;

/**
 * 单机.
 *
 * @author dongbin
 * @version 0.1 2021/12/8 16:21
 * @since 1.8
 */
public class RedissonSingel {

    private boolean enabled = false;
    private int connectionMinimumIdleSize = 32;
    private int connectionPoolSize = 64;
    private String address = "redis://127.0.0.1:6379";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getConnectionMinimumIdleSize() {
        return connectionMinimumIdleSize;
    }

    public void setConnectionMinimumIdleSize(int connectionMinimumIdleSize) {
        this.connectionMinimumIdleSize = connectionMinimumIdleSize;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
