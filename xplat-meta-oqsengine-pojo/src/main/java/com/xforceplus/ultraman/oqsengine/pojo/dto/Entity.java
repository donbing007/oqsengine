package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;

import java.util.Objects;

public class Entity implements IEntity {

    /**
     * 数据id
     */
    private Long id;
    /**
     * 数据结构
     */
    private EntityClass entityClass;
    /**
     * 数据集合
     */
    private EntityValue entityValue;


    @Override
    public Long id() {
        return id;
    }

    @Override
    public IEntityClass entityClass() {
        return entityClass;
    }

    @Override
    public IEntityValue entityValue() {
        return entityValue;
    }

    public Entity() {
    }

    public Entity(Long id, EntityClass entityClass, EntityValue entityValue) {
        this.id = id;
        this.entityClass = entityClass;
        this.entityValue = entityValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(EntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public EntityValue getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(EntityValue entityValue) {
        this.entityValue = entityValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return Objects.equals(getId(), entity.getId()) &&
                Objects.equals(getEntityClass(), entity.getEntityClass()) &&
                Objects.equals(getEntityValue(), entity.getEntityValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEntityClass(), getEntityValue());
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", entityClass=" + entityClass +
                ", entityValue=" + entityValue +
                '}';
    }
}
