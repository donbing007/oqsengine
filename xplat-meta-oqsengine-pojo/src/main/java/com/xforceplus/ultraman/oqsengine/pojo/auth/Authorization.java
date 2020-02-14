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

    public Authorization() {
    }

    public Authorization(Long appId, String role, Long tenantId) {
        this.appId = appId;
        this.role = role;
        this.tenantId = tenantId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Authorization)) return false;
        Authorization authorization = (Authorization) o;
        return Objects.equals(getRole(), authorization.getRole()) &&
            Objects.equals(getTenantId(), authorization.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRole(), getTenantId());
    }

    @Override
    public String toString() {
        return "Auth{" +
            "appId='" + appId + '\'' +
            ", role='" + role + '\'' +
            ", tenant='" + tenantId + '\'' +
            '}';
    }
}
