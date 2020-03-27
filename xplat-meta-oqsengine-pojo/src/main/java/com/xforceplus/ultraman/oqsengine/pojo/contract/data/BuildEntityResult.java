package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

/**
 * 保存数据对象返回结果.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class BuildEntityResult extends Result implements Serializable {

    /**
     * Entity对象id
     */
    private Long id;

    public BuildEntityResult(Object status) {
        super(status);
    }

    public BuildEntityResult(Object status, String message) {
        super(status, message);
    }

    public BuildEntityResult(Object status, Collection values, String message) {
        super(status, values, message);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildEntityResult)) return false;
        if (!super.equals(o)) return false;
        BuildEntityResult that = (BuildEntityResult) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }
}
