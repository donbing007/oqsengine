package com.xforceplus.ultraman.oqsengine.pojo.contract.web;

import java.io.Serializable;
import java.util.Objects;

/**
 * 页面对象配置查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class PageBoSearch implements Serializable {
    private Long id;
    private String code;

    public PageBoSearch() {
    }

    public PageBoSearch(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageBoSearch)) return false;
        PageBoSearch that = (PageBoSearch) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCode());
    }
}