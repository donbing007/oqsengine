package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

/**
 * ult page boitem
 */
public class UltPageBoItem {
    /**
     * 配置的id
     */
    private Long id;

    /**
     * 页面id
     */
    private Long pageId;

    /**
     * 页面CODE
     */
    private String code;

    /**
     * 业务对象名称
     */
    private String boName;

    /**
     * 业务对象CODE
     */
    private String boCode;

    /**
     * 说明信息
     */
    private String remark;

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
     * 显示位置
     */
    private Long sortPlace;

    /**
     * 部署状态
     */
    private String envStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getBoName() {
        return boName;
    }

    public void setBoName(String boName) {
        this.boName = boName;
    }

    public String getBoCode() {
        return boCode;
    }

    public void setBoCode(String boCode) {
        this.boCode = boCode;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(String envStatus) {
        this.envStatus = envStatus;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Long getSortPlace() {
        return sortPlace;
    }

    public void setSortPlace(Long sortPlace) {
        this.sortPlace = sortPlace;
    }

    @Override
    public String toString() {
        return "UltPageBoItem{" +
                "id=" + id +
                ", pageId=" + pageId +
                ", code='" + code + '\'' +
                ", boName='" + boName + '\'' +
                ", boCode='" + boCode + '\'' +
                ", remark='" + remark + '\'' +
                ", tenantId=" + tenantId +
                ", tenantName='" + tenantName + '\'' +
                ", tenantCode='" + tenantCode + '\'' +
                ", setting='" + setting + '\'' +
                ", envStatus='" + envStatus + '\'' +
                '}';
    }
}
