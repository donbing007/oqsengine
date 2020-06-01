package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.util.List;
import java.util.Objects;

/**
 * 页面对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
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

    private String tenantCode;

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

    /**
     * 构造方法
     */
    public UltPage() {
    }

    /**
     * 构造方法
     * @param id
     * @param appId
     * @param name
     * @param code
     * @param refPageId
     * @param tenantId
     * @param tenantName
     * @param version
     * @param pageBoVos
     */
    public UltPage(Long id, Long appId, String name, String code, Long refPageId,
                   Long tenantId, String tenantName, String tenantCode, String version, List<UltPageBo> pageBoVos) {
        this.id = id;
        this.appId = appId;
        this.name = name;
        this.code = code;
        this.refPageId = refPageId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.version = version;
        this.pageBoVos = pageBoVos;
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
        UltPage ultPage = (UltPage) o;
        return id.equals(ultPage.id) &&
                appId.equals(ultPage.appId) &&
                name.equals(ultPage.name) &&
                code.equals(ultPage.code) &&
                refPageId.equals(ultPage.refPageId) &&
                tenantId.equals(ultPage.tenantId) &&
                tenantName.equals(ultPage.tenantName) &&
                tenantCode.equals(ultPage.tenantCode) &&
                version.equals(ultPage.version) &&
                envStatus.equals(ultPage.envStatus) &&
                pageBoVos.equals(ultPage.pageBoVos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, name, code, refPageId, tenantId, tenantName, tenantCode, version, envStatus, pageBoVos);
    }
}
