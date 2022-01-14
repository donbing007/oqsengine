package com.xforceplus.ultraman.oqsengine.boot.config.redis;

import org.redisson.config.ReadMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * redisson redis 配置.
 *
 * @author dongbin
 * @version 0.1 2021/12/8 15:15
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "redis.redisson")
public class RedissonRedisConfiguration {

    private boolean keepPubSubOrder = true;
    private int threads = 0;
    private int database = 0;
    private int nettyThreads = 0;
    private int retryAttempts = 3;
    private int retryInterval = 1500;
    private int subscriptionsPerConnection = 5;
    private int subscriptionConnectionPoolSize = 50;
    private int subscriptionConnectionMinimumIdleSize = 1;
    private int dnsMonitoringInterval = -1;
    private int slaveSubscriptionConnectionMinimumIdleSize = 1;
    private int slaveSubscriptionConnectionPoolSize = 50;
    private int slaveConnectionMinimumIdleSize = 32;
    private int slaveConnectionPoolSize = 64;
    private int masterConnectionMinimumIdleSize = 32;
    private int masterConnectionPoolSize = 64;
    private int idleConnectionTimeout = 10000;
    private int connectTimeout = 10000;
    private int timeout = 6000;
    private long lockWatchdogTimeout = 30000;
    private String password = null;
    private String clientName = null;
    private ReadMode readMode = ReadMode.SLAVE;

    private RedissonSingel singel;
    private RedissonCluster cluster;
    private RedissonSentine sentine;
    private RedissonMasterSlave masterSlave;

    public boolean isKeepPubSubOrder() {
        return keepPubSubOrder;
    }

    public void setKeepPubSubOrder(boolean keepPubSubOrder) {
        this.keepPubSubOrder = keepPubSubOrder;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getNettyThreads() {
        return nettyThreads;
    }

    public void setNettyThreads(int nettyThreads) {
        this.nettyThreads = nettyThreads;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getSubscriptionsPerConnection() {
        return subscriptionsPerConnection;
    }

    public int getSubscriptionConnectionPoolSize() {
        return subscriptionConnectionPoolSize;
    }

    public void setSubscriptionConnectionPoolSize(int subscriptionConnectionPoolSize) {
        this.subscriptionConnectionPoolSize = subscriptionConnectionPoolSize;
    }

    public void setSubscriptionsPerConnection(int subscriptionsPerConnection) {
        this.subscriptionsPerConnection = subscriptionsPerConnection;
    }

    public int getSubscriptionConnectionMinimumIdleSize() {
        return subscriptionConnectionMinimumIdleSize;
    }

    public void setSubscriptionConnectionMinimumIdleSize(int subscriptionConnectionMinimumIdleSize) {
        this.subscriptionConnectionMinimumIdleSize = subscriptionConnectionMinimumIdleSize;
    }

    public int getDnsMonitoringInterval() {
        return dnsMonitoringInterval;
    }

    public void setDnsMonitoringInterval(int dnsMonitoringInterval) {
        this.dnsMonitoringInterval = dnsMonitoringInterval;
    }

    public long getLockWatchdogTimeout() {
        return lockWatchdogTimeout;
    }

    public void setLockWatchdogTimeout(long lockWatchdogTimeout) {
        this.lockWatchdogTimeout = lockWatchdogTimeout;
    }

    public int getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    public void setIdleConnectionTimeout(int idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    /**
     * 设置密码.
     */
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.password = password;
        }
    }

    public String getClientName() {
        return clientName;
    }

    public int getSlaveSubscriptionConnectionMinimumIdleSize() {
        return slaveSubscriptionConnectionMinimumIdleSize;
    }

    public void setSlaveSubscriptionConnectionMinimumIdleSize(int slaveSubscriptionConnectionMinimumIdleSize) {
        this.slaveSubscriptionConnectionMinimumIdleSize = slaveSubscriptionConnectionMinimumIdleSize;
    }

    public int getSlaveSubscriptionConnectionPoolSize() {
        return slaveSubscriptionConnectionPoolSize;
    }

    public void setSlaveSubscriptionConnectionPoolSize(int slaveSubscriptionConnectionPoolSize) {
        this.slaveSubscriptionConnectionPoolSize = slaveSubscriptionConnectionPoolSize;
    }

    public int getSlaveConnectionMinimumIdleSize() {
        return slaveConnectionMinimumIdleSize;
    }

    public void setSlaveConnectionMinimumIdleSize(int slaveConnectionMinimumIdleSize) {
        this.slaveConnectionMinimumIdleSize = slaveConnectionMinimumIdleSize;
    }

    public int getSlaveConnectionPoolSize() {
        return slaveConnectionPoolSize;
    }

    public void setSlaveConnectionPoolSize(int slaveConnectionPoolSize) {
        this.slaveConnectionPoolSize = slaveConnectionPoolSize;
    }

    public int getMasterConnectionMinimumIdleSize() {
        return masterConnectionMinimumIdleSize;
    }

    public void setMasterConnectionMinimumIdleSize(int masterConnectionMinimumIdleSize) {
        this.masterConnectionMinimumIdleSize = masterConnectionMinimumIdleSize;
    }

    public int getMasterConnectionPoolSize() {
        return masterConnectionPoolSize;
    }

    public void setMasterConnectionPoolSize(int masterConnectionPoolSize) {
        this.masterConnectionPoolSize = masterConnectionPoolSize;
    }

    public ReadMode getReadMode() {
        return readMode;
    }

    /**
     * 设置读取模式.
     */
    public void setReadMode(String readMode) {
        for (ReadMode mode : ReadMode.values()) {
            if (mode.name().equals(readMode)) {
                this.readMode = mode;
                return;
            }
        }

        this.readMode = ReadMode.SLAVE;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public RedissonSingel getSingel() {
        return singel;
    }

    public void setSingel(RedissonSingel singel) {
        this.singel = singel;
    }

    public RedissonCluster getCluster() {
        return cluster;
    }

    public void setCluster(RedissonCluster cluster) {
        this.cluster = cluster;
    }

    public RedissonSentine getSentine() {
        return sentine;
    }

    public void setSentine(RedissonSentine sentine) {
        this.sentine = sentine;
    }

    public RedissonMasterSlave getMasterSlave() {
        return masterSlave;
    }

    public void setMasterSlave(RedissonMasterSlave masterSlave) {
        this.masterSlave = masterSlave;
    }
}
