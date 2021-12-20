package com.xforceplus.ultraman.oqsengine.boot.config.redis;

/**
 * redisson master-slave连接模式配置.
 *
 * @author dongbin
 * @version 0.1 2021/12/8 16:22
 * @since 1.8
 */
public class RedissonMasterSlave {

    private boolean enabled = false;
    private String masterAddress = "redis://127.0.0.1:6379";
    private String[] slaveAddresses = new String[] {
        "redis://127.0.0.1:6380",
        "redis://127.0.0.1:6381",
    };

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    public String[] getSlaveAddresses() {
        return slaveAddresses;
    }

    public void setSlaveAddresses(String[] slaveAddresses) {
        this.slaveAddresses = slaveAddresses;
    }
}
