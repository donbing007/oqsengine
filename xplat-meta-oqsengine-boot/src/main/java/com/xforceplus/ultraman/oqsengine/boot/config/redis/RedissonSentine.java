package com.xforceplus.ultraman.oqsengine.boot.config.redis;

/**
 * 哨兵.
 *
 * @author dongbin
 * @version 0.1 2021/12/8 16:22
 * @since 1.8
 */
public class RedissonSentine {

    private boolean enabled = false;
    private String masterName = "oqsmaster";
    private String[] sentinelAddresses = new String[0];

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String[] getSentinelAddresses() {
        return sentinelAddresses;
    }

    public void setSentinelAddresses(String[] sentinelAddresses) {
        this.sentinelAddresses = sentinelAddresses;
    }
}
