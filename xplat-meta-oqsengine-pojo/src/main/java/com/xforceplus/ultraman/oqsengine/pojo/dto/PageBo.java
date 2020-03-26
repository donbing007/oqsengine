package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * 页面对象配置对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class PageBo implements Serializable {

    /**
     * BoSeeting的id
     */
    private Long id;

    /**
     * 页面id
     */
    private Long pageId;

    /**
     * 业务对象名称
     */
    private String boName;

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
    public PageBo() {
    }

    /**
     * 构造方法
     * @param id
     * @param pageId
     * @param boName
     * @param setting
     * @param version
     */
    public PageBo(Long id, Long pageId, String boName, String setting, String version) {
        this.id = id;
        this.pageId = pageId;
        this.boName = boName;
        this.setting = setting;
        this.version = version;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageBo)) return false;
        PageBo pageBo = (PageBo) o;
        return Objects.equals(getId(), pageBo.getId()) &&
                Objects.equals(getPageId(), pageBo.getPageId()) &&
                Objects.equals(getBoName(), pageBo.getBoName()) &&
                Objects.equals(getSetting(), pageBo.getSetting()) &&
                Objects.equals(getVersion(), pageBo.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPageId(), getBoName(), getSetting(), getVersion());
    }

    @Override
    public String toString() {
        return "PageBo{" +
                "id=" + id +
                ", pageId=" + pageId +
                ", boName='" + boName + '\'' +
                ", setting='" + setting + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
