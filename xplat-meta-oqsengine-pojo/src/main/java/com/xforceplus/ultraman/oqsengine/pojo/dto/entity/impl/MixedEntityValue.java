package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * a mixed EntityValue can hold multi field from different entity
 */
public class MixedEntityValue implements IEntityValue, Cloneable, Serializable {

    /**
     * Entity的值集合
     */
    private Map<String, IValue> values;

    /**
     * make sure entityValue has a complete field
     * @param entityValue
     */
    public MixedEntityValue(IEntityValue entityValue) {
        entityValue.values().forEach(this::addValue);
    }

    @Override
    public Optional<IValue> getValue(String fieldName) {
        return Optional.ofNullable(values.get(fieldName));
    }

    @Override
    public Optional<IValue> getValue(long fieldId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IEntityValue addValue(IValue value) {
        lazyInit();
        values.put(value.getField().name(), value);
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
            this.values.put(v.getField().name(), v);

        });
        return this;
    }

    @Override
    public Optional<IValue> remove(IEntityField field) {
        lazyInit();

        return Optional.ofNullable(values.remove(field.name()));
    }

    @Override
    public void filter(Predicate<? super IValue> predicate) {
        values.entrySet().removeIf(entry -> !predicate.test(entry.getValue()));
    }

    @Override
    public IEntityValue clear() {
        if (values != null) {
            values.clear();
        }
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EntityValue cloneValue = new EntityValue();
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
        if (!(o instanceof MixedEntityValue)) {
            return false;
        }
        MixedEntityValue that = (MixedEntityValue) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
