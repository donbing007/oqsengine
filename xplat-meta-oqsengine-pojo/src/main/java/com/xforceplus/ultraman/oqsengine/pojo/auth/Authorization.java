package com.xforceplus.ultraman.oqsengine.pojo.auth;

import java.io.Serializable;
import java.util.Objects;

/**
 * 授权信息.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Authorization implements Serializable {
    private Long appId;
    private String role;
    private Long tenantId;
    private String env;

    public Authorization() {
    }

    public Authorization(Long appId, String role, Long tenantId) {
        this.appId = appId;
        this.role = role;
        this.tenantId = tenantId;
    }

    public Authorization(Long appId, String role, Long tenantId, String env) {
        this.appId = appId;
        this.role = role;
        this.tenantId = tenantId;
        this.env = env;
    }

    public Authorization(String role, Long tenantId) {
        this(null, role, tenantId);
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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
        if (!(o instanceof Authorization)) return false;
        Authorization that = (Authorization) o;
        return Objects.equals(getAppId(), that.getAppId()) &&
                Objects.equals(getRole(), that.getRole()) &&
                Objects.equals(getTenantId(), that.getTenantId()) &&
                Objects.equals(getEnv(), that.getEnv());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppId(), getRole(), getTenantId(), getEnv());
    }

    @Override
    public String toString() {
        return "Authorization{" +
                "appId=" + appId +
                ", role='" + role + '\'' +
                ", tenantId=" + tenantId +
                ", env='" + env + '\'' +
                '}';
    }
}
