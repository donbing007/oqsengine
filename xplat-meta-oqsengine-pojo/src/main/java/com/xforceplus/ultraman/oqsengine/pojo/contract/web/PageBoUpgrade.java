package com.xforceplus.ultraman.oqsengine.pojo.contract.web;

import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 页面对象配置查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class PageBoUpgrade implements Serializable {
    private Long id;
    private String version;
    private List<PageBo> pageBos;

    public PageBoUpgrade() {
    }

    public PageBoUpgrade(Long id, String version, List<PageBo> pageBos) {
        this.id = id;
        this.version = version;
        this.pageBos = pageBos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<PageBo> getPageBos() {
        return pageBos;
    }

    public void setPageBos(List<PageBo> pageBos) {
        this.pageBos = pageBos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageBoUpgrade)) return false;
        PageBoUpgrade that = (PageBoUpgrade) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getPageBos(), that.getPageBos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion(), getPageBos());
    }
}