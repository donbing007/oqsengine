package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityValue<K,V> implements IEntityValue {
    /**
     * 元数据boId
     */
    private Long id;
    /**
     * 子对象数据信息
     */
    private List<EntityValue> entityValues;
    /**
     * 数据信息
     */
    private Map<K,V> value;

    @Override
    public Long id() {
        return null;
    }

    @Override
    public List<IEntityValue> entityValues() {
        return null;
    }

    @Override
    public Map values() {
        return null;
    }

    public EntityValue() {
    }

    public EntityValue(Long id, List<EntityValue> entityValues, Map<K, V> value) {
        this.id = id;
        this.entityValues = entityValues;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<EntityValue> getEntityValues() {
        return entityValues;
    }

    public void setEntityValues(List<EntityValue> entityValues) {
        this.entityValues = entityValues;
    }

    public Map<K, V> getValue() {
        return value;
    }

    public void setValue(Map<K, V> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityValue)) return false;
        EntityValue<?, ?> that = (EntityValue<?, ?>) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getEntityValues(), that.getEntityValues()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEntityValues(), getValue());
    }

    @Override
    public String toString() {
        return "EntityValue{" +
                "id=" + id +
                ", entityValues=" + entityValues +
                ", value=" + value +
                '}';
    }
}
