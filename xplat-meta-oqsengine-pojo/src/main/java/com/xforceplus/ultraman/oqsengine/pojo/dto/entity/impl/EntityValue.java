package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

public class EntityValue implements IEntityValue, Cloneable, Serializable {
    /**
     * 元数据boId
     */
    private long id;

    /**
     * Entity的值集合
     */
    private Map<Long, IValue> values;


    public EntityValue(long id) {
        this.id = id;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Optional<IValue> getValue(String fieldName) {
        if (values == null) {
            return Optional.empty();

        }

        for (IValue v : values.values()) {
            if (v.getField().name().equals(fieldName)) {
                return Optional.of(v);
            }
        }

        return Optional.empty();
    }

    @Override
    public IEntityValue addValue(IValue value) {
        lazyInit();

        values.put(value.getField().id(), value);
        return this;
    }

    @Override
    public Collection<IValue> values() {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.values();
    }

    @Override
    public IEntityValue addValues(Collection<IValue> values) {
        lazyInit();
        values.stream().forEach(v -> {
            this.values.put(v.getField().id(), v);

        });
        return this;
    }

    @Override
    public IValue remove(IEntityField field) {
        lazyInit();

        return values.remove(field);
    }

    @Override
    public void filter(Predicate<? super IValue> predicate) {
        values.entrySet().removeIf(entry -> !predicate.test(entry.getValue()));
    }

    @Override
    public IEntityValue clear() {
        values.clear();
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EntityValue cloneValue = new EntityValue(id);
        cloneValue.addValues(values());
        return cloneValue;
    }

    private void lazyInit() {
        if (this.values == null) {
            // 这里为了保存顺序为加入顺序.
            this.values = new LinkedHashMap<>();
        }
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
            ", values=" + (values != null ? values.toString() : "NULL") +
            '}';
    }
}
