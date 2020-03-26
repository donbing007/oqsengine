package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 删除数据对象返回结果.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class SearchEntityResult extends Result implements Serializable {
    private List<IEntity> entities;

    public SearchEntityResult(Object status) {
        super(status);
    }

    public SearchEntityResult(Object status, String message) {
        super(status, message);
    }

    public SearchEntityResult(Object status, Collection values, String message) {
        super(status, values, message);
    }

    public List<IEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<IEntity> entities) {
        this.entities = entities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchEntityResult)) return false;
        if (!super.equals(o)) return false;
        SearchEntityResult that = (SearchEntityResult) o;
        return Objects.equals(getEntities(), that.getEntities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getEntities());
    }
}
