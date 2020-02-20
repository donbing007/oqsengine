package com.xforceplus.ultraman.oqsengine.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 授权信息配置.
 * @version 0.1 2019/11/19 15:22
 * @auth dongbin - wangzheng
 * @since 1.8
 */
@ConfigurationProperties(prefix = "xplat.meta.oqsengine.permissions")
@Component
public class AuthSearcherConfig {

    private String tenant;

    private String appId;

    private String role;

    private String env;

    public AuthSearcherConfig() {
    }

    public AuthSearcherConfig(String tenant, String appId, String role, String env) {
        this.tenant = tenant;
        this.appId = appId;
        this.role = role;
        this.env = env;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthSearcherConfig)) return false;
        AuthSearcherConfig that = (AuthSearcherConfig) o;
        return Objects.equals(getTenant(), that.getTenant()) &&
                Objects.equals(getAppId(), that.getAppId()) &&
                Objects.equals(getRole(), that.getRole()) &&
                Objects.equals(getEnv(), that.getEnv());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTenant(), getAppId(), getRole(), getEnv());
    }
}
