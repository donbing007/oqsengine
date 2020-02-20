package com.xforceplus.ultraman.oqsengine.sdk.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.boot.context.properties.bind.Binder")
@ConfigurationProperties(prefix = "xplat.meta.oqsengine")
@EnableConfigurationProperties(AutomaticConfiguration.class)
@ComponentScan(value = "com.xforceplus.ultraman.oqsengine.sdk.*")
public class AutomaticConfiguration {
    //grpc相关属性配置
    private String host = "127.0.0.1";

    private int port = 8206;

    private long heartbeatTimeoutSeconds = 30;

    private long heartbeatIntervalSeconds = 30;

    private long readTimeoutMs = 200;

    private String includeRex = "(.*)";

    private boolean debug = false;

    private AuthSearcherConfig authSearcherConfig = new AuthSearcherConfig();

    private ControllerExtendConfiguration controllerExtendConfiguration = new ControllerExtendConfiguration();


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getHeartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    public void setHeartbeatTimeoutSeconds(long heartbeatTimeoutSeconds) {
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }

    public long getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getIncludeRex() {
        return includeRex;
    }

    public void setIncludeRex(String includeRex) {
        this.includeRex = includeRex;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public AuthSearcherConfig getAuthSearcherConfig() {
        return authSearcherConfig;
    }

    public void setAuthSearcherConfig(AuthSearcherConfig authSearcherConfig) {
        this.authSearcherConfig = authSearcherConfig;
    }

    public ControllerExtendConfiguration getControllerExtendConfiguration() {
        return controllerExtendConfiguration;
    }

    public void setControllerExtendConfiguration(ControllerExtendConfiguration controllerExtendConfiguration) {
        this.controllerExtendConfiguration = controllerExtendConfiguration;
    }
}
