package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class EntitySearch implements Serializable {
    private EntitySource entitySource;

    public EntitySearch() {
    }

    public EntitySearch(EntitySource entitySource) {
        this.entitySource = entitySource;
    }

    public EntitySource getEntitySource() {
        return entitySource;
    }

    public void setEntitySource(EntitySource entitySource) {
        this.entitySource = entitySource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntitySearch)) return false;
        EntitySearch that = (EntitySearch) o;
        return Objects.equals(getEntitySource(), that.getEntitySource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntitySource());
    }
}