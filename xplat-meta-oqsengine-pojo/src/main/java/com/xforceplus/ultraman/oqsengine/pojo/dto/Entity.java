package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;

import java.util.List;
import java.util.Objects;

public class Entity implements IEntity {

    /**
     * 数据id
     */
    private Long id;
    /**
     * 数据结构
     */
    private IEntityClass entityClass;
    /**
     * 数据集合
     */
    private List<IEntityValue> entityValues;


    @Override
    public Long id() {
        return id;
    }

    @Override
    public IEntityClass entityClass() {
        return entityClass;
    }

    @Override
    public List<IEntityValue> entityValue() {
        return entityValues;
    }

    public Entity() {
    }

    public Entity(Long id, EntityClass entityClass, EntityValue entityValue) {
        this.id = id;
        this.entityClass = entityClass;
        this.entityValues = entityValues;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public List<IEntityValue> getEntityValues() {
        return entityValues;
    }

    public void setEntityValues(List<IEntityValue> entityValues) {
        this.entityValues = entityValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return Objects.equals(getId(), entity.getId()) &&
                Objects.equals(getEntityClass(), entity.getEntityClass()) &&
                Objects.equals(getEntityValues(), entity.getEntityValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEntityClass(), getEntityValues());
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", entityClass=" + entityClass +
                ", entityValues=" + entityValues +
                '}';
    }
}
