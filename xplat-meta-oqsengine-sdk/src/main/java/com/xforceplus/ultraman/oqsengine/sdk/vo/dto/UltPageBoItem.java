package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

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
     * 业务对象名称
     */
    private String boName;

    /**
     * 业务对象CODE
     */
    private String boCode;

    /**
     * 配置信息
     */
    private String setting;

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
}
