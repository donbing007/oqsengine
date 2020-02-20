package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.util.*;

public class EntityValue implements IEntityValue {
    /**
     * 元数据boId
     */
    private long id;

    /**
     * Entity的值集合
     */
    private Map<Field, IValue> values;

    public EntityValue(long id) {
        this.id = id;
        values = new HashMap();
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public IValue getValue(String fieldName) {
        return values.get(fieldName);
    }

    @Override
    public IEntityValue addValue(IValue value) {
        values.put(value.getField(), value);
        return this;
    }

    @Override
    public Collection<IValue> values() {
        return values.values();
    }

    @Override
    public IEntityValue setValues(List<IValue> values) {
        values.stream().forEach(v -> {
            this.values.put(v.getField(), v);
        });
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityValue)) {
            return false;
        }
        EntityValue that = (EntityValue) o;
        return id == that.id &&
            Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, values);
    }

    @Override
    public String toString() {
        return "EntityValue{" +
            "id=" + id +
            ", values=" + values +
            '}';
    }
}
