package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import java.io.Serializable;
import java.util.Objects;

/**
 * 元数据对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class BoSearch implements Serializable {
    private Long id;
    private String code;

    public BoSearch() {
    }

    public BoSearch(Long id, String code) {
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
        if (!(o instanceof BoSearch)) return false;
        BoSearch boSearch = (BoSearch) o;
        return Objects.equals(getId(), boSearch.getId()) &&
                Objects.equals(getCode(), boSearch.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCode());
    }
}