package com.xforceplus.ultraman.oqsengine.pojo.dto;

import java.util.Objects;

/**
 * 表单配置对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class UltForm {
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 表单名称
     */
    private String name;

    /**
     * 表单CODE
     */
    private String code;

    /**
     * 定制来源表单id
     */
    private Long refFormId;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    private String tenantCode;

    /**
     * 配置信息
     */
    private String setting;

    /**
     * 版本信息
     */
    private String version;

    /**
     * 构造方法
     */
    public UltForm() {
    }

    /**
     * 构造方法
     * @param id
     * @param appId
     * @param name
     * @param code
     * @param refFormId
     * @param tenantId
     * @param tenantName
     * @param setting
     * @param version
     */
    public UltForm(Long id, Long appId, String name, String code, Long refFormId, Long tenantId, String tenantCode, String tenantName, String setting, String version) {
        this.id = id;
        this.appId = appId;
        this.name = name;
        this.code = code;
        this.refFormId = refFormId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.setting = setting;
        this.version = version;
        this.tenantCode = tenantCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getRefFormId() {
        return refFormId;
    }

    public void setRefFormId(Long refFormId) {
        this.refFormId = refFormId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UltForm ultForm = (UltForm) o;
        return Objects.equals(id, ultForm.id) &&
                Objects.equals(appId, ultForm.appId) &&
                Objects.equals(name, ultForm.name) &&
                Objects.equals(code, ultForm.code) &&
                Objects.equals(refFormId, ultForm.refFormId) &&
                Objects.equals(tenantId, ultForm.tenantId) &&
                Objects.equals(tenantName, ultForm.tenantName) &&
                Objects.equals(tenantCode, ultForm.tenantCode) &&
                Objects.equals(setting, ultForm.setting) &&
                Objects.equals(version, ultForm.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, name, code, refFormId, tenantId, tenantName, tenantCode, setting, version);
    }
}
