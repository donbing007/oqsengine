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
    public UltForm(Long id, Long appId, String name, String code, Long refFormId, Long tenantId, String tenantName, String setting, String version) {
        this.id = id;
        this.appId = appId;
        this.name = name;
        this.code = code;
        this.refFormId = refFormId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.setting = setting;
        this.version = version;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UltForm)) return false;
        UltForm ultForm = (UltForm) o;
        return Objects.equals(getId(), ultForm.getId()) &&
                Objects.equals(getAppId(), ultForm.getAppId()) &&
                Objects.equals(getName(), ultForm.getName()) &&
                Objects.equals(getCode(), ultForm.getCode()) &&
                Objects.equals(getRefFormId(), ultForm.getRefFormId()) &&
                Objects.equals(getTenantId(), ultForm.getTenantId()) &&
                Objects.equals(getTenantName(), ultForm.getTenantName()) &&
                Objects.equals(getSetting(), ultForm.getSetting()) &&
                Objects.equals(getVersion(), ultForm.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAppId(), getName(), getCode(), getRefFormId(), getTenantId(), getTenantName(), getSetting(), getVersion());
    }
}
