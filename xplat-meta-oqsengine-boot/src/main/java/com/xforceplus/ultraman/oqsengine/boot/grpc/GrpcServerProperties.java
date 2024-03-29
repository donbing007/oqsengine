package com.xforceplus.ultraman.oqsengine.boot.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * properties.
 */
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {

    private boolean enabled = true;

    private String host = "0.0.0.0";

    private Integer port = 8081;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
