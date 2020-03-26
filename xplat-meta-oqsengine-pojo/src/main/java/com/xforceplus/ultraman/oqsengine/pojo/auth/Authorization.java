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
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 用户角色id
     */
    private String role;
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 环境id
     */
    private String env;

    /**
     * 默认构造方法
     */
    public Authorization() {
    }

    /**
     * 传入app,role,tenantid信息的构造方法
     * @param appId
     * @param role
     * @param tenantId
     */
    public Authorization(Long appId, String role, Long tenantId) {
        this.appId = appId;
        this.role = role;
        this.tenantId = tenantId;
    }

    /**
     * 传入app,role,tenantid,env信息的构造方法
     * @param appId
     * @param role
     * @param tenantId
     * @param env
     */
    public Authorization(Long appId, String role, Long tenantId, String env) {
        this.appId = appId;
        this.role = role;
        this.tenantId = tenantId;
        this.env = env;
    }

    /**
     * 传入role,tenantid
     * @param role
     * @param tenantId
     */
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

    /**
     * equals方法
     * @param o
     * @return
     */
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

    /**
     * hashCode方法
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(getAppId(), getRole(), getTenantId(), getEnv());
    }

    /**
     * toString方法
     * @return
     */
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
