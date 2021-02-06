package com.xforceplus.ultraman.oqsengine.meta.connect;

import io.grpc.internal.GrpcUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * desc :
 * name : GRpcServerConfiguration
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
@ConfigurationProperties(prefix = "grpc")
public class GRpcServerConfiguration {
    private int maxInboundMetadataBytes = GrpcUtil.DEFAULT_MAX_HEADER_LIST_SIZE;
    private int maxInboundMessageBytes = GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE;
    private long heartbeatIntervalSeconds = 30;
    private long heartbeatTimeoutSeconds = 30;

    public int getMaxInboundMetadataBytes() {
        return maxInboundMetadataBytes;
    }

    public void setMaxInboundMetadataBytes(int maxInboundMetadataBytes) {
        this.maxInboundMetadataBytes = maxInboundMetadataBytes;
    }

    public int getMaxInboundMessageBytes() {
        return maxInboundMessageBytes;
    }

    public void setMaxInboundMessageBytes(int maxInboundMessageBytes) {
        this.maxInboundMessageBytes = maxInboundMessageBytes;
    }

    public long getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public long getHeartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    public void setHeartbeatTimeoutSeconds(long heartbeatTimeoutSeconds) {
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }
}
