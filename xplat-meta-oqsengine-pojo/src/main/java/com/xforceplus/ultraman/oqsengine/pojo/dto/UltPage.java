package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.util.List;
import java.util.Objects;

public class UltPage {

    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 页面名称
     */
    private String name;

    /**
     * 页面CODE
     */
    private String code;

    /**
     * 定制来源页面id
     */
    private Long refPageId;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 版本信息
     */
    private String version;

    /**
     * 部署状态
     */
    private String envStatus;

    /**
     * 页面Bo配置信息集合
     */
    private List<UltPageBo> pageBoVos;

    public UltPage() {
    }

    public UltPage(Long id, Long appId, String name, String code, Long refPageId, Long tenantId, String tenantName, String version, List<UltPageBo> pageBoVos) {
        this.id = id;
        this.appId = appId;
        this.name = name;
        this.code = code;
        this.refPageId = refPageId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.version = version;
        this.pageBoVos = pageBoVos;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<UltPageBo> getPageBoVos() {
        return pageBoVos;
    }

    public void setPageBoVos(List<UltPageBo> pageBoVos) {
        this.pageBoVos = pageBoVos;
    }

    public Long getRefPageId() {
        return refPageId;
    }

    public void setRefPageId(Long refPageId) {
        this.refPageId = refPageId;
    }

    public String getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(String envStatus) {
        this.envStatus = envStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UltPage)) return false;
        UltPage ultPage = (UltPage) o;
        return Objects.equals(getId(), ultPage.getId()) &&
                Objects.equals(getAppId(), ultPage.getAppId()) &&
                Objects.equals(getName(), ultPage.getName()) &&
                Objects.equals(getCode(), ultPage.getCode()) &&
                Objects.equals(getRefPageId(), ultPage.getRefPageId()) &&
                Objects.equals(getTenantId(), ultPage.getTenantId()) &&
                Objects.equals(getTenantName(), ultPage.getTenantName()) &&
                Objects.equals(getVersion(), ultPage.getVersion()) &&
                Objects.equals(getPageBoVos(), ultPage.getPageBoVos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAppId(), getName(), getCode(), getRefPageId(), getTenantId(), getTenantName(), getVersion(), getPageBoVos());
    }
}
