package com.xforceplus.ultraman.oqsengine.boot.config.redis;

import io.lettuce.core.ClientOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * lettuce 配置.
 *
 * @author dongbin
 * @version 0.1 2020/11/16 14:12
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "redis.lettuce")
public class LettuceRedisConfiguration {

    private String uri;
    private boolean cluster;

    /*
    PING在任何连接可用之前启用初始屏障。如果true，则每个连接和重新连接都将发出PING命令并在连接被激活和启用以供使用之前等待其响应。
    如果检查失败，则连接/重新连接被视为失败。FailedPING重新连接时作为协议错误处理，如果suspendReconnectOnProtocolFailure启用，
    可以暂停重新连接。
     */
    private boolean pingBeforeActivateConnection = true;
    /*
    控制连接上的自动重新连接行为。一旦连接被关闭/重置而不打算关闭它，客户端将尝试重新连接，激活连接并重新发出任何排队的命令。
     */
    private boolean autoReconnect = true;
    /*
    如果此标志是，true则重新连接将因协议错误而暂停。重新连接本身有两个阶段：套接字连接和协议/连接激活。
    如果发生连接超时、连接重置、主机查找失败，这不影响命令的取消。相反，在激活连接失败之前由于 SSL 错误或 PING 而导致协议/连接激活失败的情况下，
    排队的命令将被取消。
     */
    private boolean suspendReconnectOnProtocolFailure = false;
    /*
    控制每个连接的请求队列大小。RedisException如果超过队列大小，命令调用将导致RedisException。
    将设置requestQueueSize为较低的值将导致在过载或连接处于断开状态时更早出现异常。
    更高的值意味着到达边界需要更长的时间，但更多的请求可能会排队，并使用更多的堆空间。
     */
    private int requestQueueSize = Integer.MAX_VALUE;
    /*
    连接可以通过多种方式在断开连接状态下运行。自动连接功能尤其允许在连接断开时重新触发已排队的命令。断开连接的行为设置允许对行为进行细粒度控制
    以下设置可用：
       DEFAULT: 启用自动重连时接受命令，禁用自动重连时拒绝命令。
       ACCEPT_COMMANDS: 在断开状态下接受命令。
       REJECT_COMMANDS: 在断开连接的状态下拒绝命令。
     */
    private ClientOptions.DisconnectedBehavior disconnectedBehavior = ClientOptions.DisconnectedBehavior.DEFAULT;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public boolean isPingBeforeActivateConnection() {
        return pingBeforeActivateConnection;
    }

    public void setPingBeforeActivateConnection(boolean pingBeforeActivateConnection) {
        this.pingBeforeActivateConnection = pingBeforeActivateConnection;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean isSuspendReconnectOnProtocolFailure() {
        return suspendReconnectOnProtocolFailure;
    }

    public void setSuspendReconnectOnProtocolFailure(boolean suspendReconnectOnProtocolFailure) {
        this.suspendReconnectOnProtocolFailure = suspendReconnectOnProtocolFailure;
    }

    public int getRequestQueueSize() {
        return requestQueueSize;
    }

    public void setRequestQueueSize(int requestQueueSize) {
        this.requestQueueSize = requestQueueSize;
    }

    public ClientOptions.DisconnectedBehavior getDisconnectedBehavior() {
        return disconnectedBehavior;
    }

    /**
     * 配置断开行为.
     */
    public void setDisconnectedBehavior(String disconnectedBehavior) {
        for (ClientOptions.DisconnectedBehavior v : ClientOptions.DisconnectedBehavior.values()) {
            if (v.name().equals(disconnectedBehavior)) {
                this.disconnectedBehavior = v;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid lettuce configuration,[disconnectedBehavior = %s]"));
    }
}
