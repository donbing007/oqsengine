package com.xforceplus.ultraman.oqsengine.pojo.dto;

import java.util.Objects;

/**
 * 页面对象配置对象.
 *
 * @author wangzheng
 * @version 0.1 2020/2/13 15:30
 * @since 1.8
 */
public class UltPageBo {

    /**
     * 配置的id
     */
    private Long settingId;

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

    /**
     * 显示位置
     */
    private Long sortPlace;

    /**
     * 说明信息
     */
    private String remark;

    /**
     * 构造方法
     */
    public UltPageBo() {
    }

    /**
     * 构造方法
     *
     * @param settingId
     * @param boName
     * @param boCode
     * @param setting
     */
    public UltPageBo(Long settingId, String boName, String boCode, String setting) {
        this.settingId = settingId;
        this.boName = boName;
        this.boCode = boCode;
        this.setting = setting;
    }

    public Long getSettingId() {
        return settingId;
    }

    public void setSettingId(Long settingId) {
        this.settingId = settingId;
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

    public Long getSortPlace() {
        return sortPlace;
    }

    public void setSortPlace(Long sortPlace) {
        this.sortPlace = sortPlace;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UltPageBo)) return false;
        UltPageBo ultPageBo = (UltPageBo) o;
        return Objects.equals(getSettingId(), ultPageBo.getSettingId()) &&
                Objects.equals(getBoName(), ultPageBo.getBoName()) &&
                Objects.equals(getBoCode(), ultPageBo.getBoCode()) &&
                Objects.equals(getSetting(), ultPageBo.getSetting());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSettingId(), getBoName(), getBoCode(), getSetting());
    }
}
