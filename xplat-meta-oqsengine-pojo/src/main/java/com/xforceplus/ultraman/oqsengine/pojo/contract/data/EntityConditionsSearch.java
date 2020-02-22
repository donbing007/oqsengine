package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据条件查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class EntityConditionsSearch implements Serializable {
    private EntitySource entitySource;
    private Conditions conditions;
    private Page page;

    public EntityConditionsSearch() {
    }

    public EntityConditionsSearch(EntitySource entitySource, Conditions conditions, Page page) {
        this.entitySource = entitySource;
        this.conditions = conditions;
        this.page = page;
    }

    public EntitySource getEntitySource() {
        return entitySource;
    }

    public void setEntitySource(EntitySource entitySource) {
        this.entitySource = entitySource;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityConditionsSearch)) return false;
        EntityConditionsSearch that = (EntityConditionsSearch) o;
        return Objects.equals(getEntitySource(), that.getEntitySource()) &&
                Objects.equals(getConditions(), that.getConditions()) &&
                Objects.equals(getPage(), that.getPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntitySource(), getConditions(), getPage());
    }
}