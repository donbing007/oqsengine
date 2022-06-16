package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Entity实体值对象.
 *
 * @author wangzheng.
 * @version 1.0 2020/3/26 15:10
 */
public class EntityValue implements IEntityValue, Cloneable, Serializable {

    private IEntity entity;
    /*
     * Entity的值集合
     */
    private Map<Long, IValue> values;

    public EntityValue(IEntity entity) {
        this.entity = entity;
    }

    @Override
    public int size() {
        return values == null ? 0 : values.size();
    }

    @Override
    public Optional<IValue> getValue(IEntityField field) {
        if (field.config().isIdentifie()) {

            return Optional.of(new LongValue(field, entity.id()));

        } else if (field.config().getFieldSense() != FieldConfig.FieldSense.NORMAL) {

            switch (field.config().getFieldSense()) {
                case UPDATE_TIME: {
                    return Optional.of(new DateTimeValue(field, DateTimeValue.toLocalDateTime(entity.time())));
                }
                default: {
                    return Optional.empty();
                }
            }
        } else {
            return getValue(field.id());
        }
    }

    @Override
    public Optional<IValue> getValue(String fieldName) {
        if (values == null) {
            return Optional.empty();

        }

        return values.values().stream().filter(v -> v.getField().name().equals(fieldName)).findFirst();
    }

    @Override
    public Optional<IValue> getValue(long fieldId) {
        if (values == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(values.get(fieldId));
    }

    @Override
    public IEntityValue addValue(IValue value) {
        lazyInit();

        IValue oldValue = values.get(value.getField().id());
        if (oldValue == null || !oldValue.equals(value)) {
            values.put(value.getField().id(), value);
        }

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
            addValue(v);
        });

        return this;
    }

    @Override
    public Optional<IValue> remove(IEntityField field) {
        if (values == null) {
            return Optional.empty();
        }

        IValue oldValue = values.remove(field.id());
        if (oldValue != null) {

            return Optional.of(oldValue);

        } else {

            return Optional.empty();

        }
    }

    @Override
    public void filter(Predicate<? super IValue> predicate) {
        if (values != null) {
            values.entrySet().removeIf(entry -> !predicate.test(entry.getValue()));
        }
    }

    @Override
    public IEntityValue clear() {
        if (values != null) {
            values.clear();
        }
        return this;
    }

    @Override
    public boolean isDirty() {
        if (this.values == null || this.values.isEmpty()) {
            return false;
        } else {
            return this.values.values().stream().filter(v -> v.isDirty()).count() > 0;
        }
    }

    @Override
    public void squeezeEmpty() {
        if (this.values == null || this.values.isEmpty()) {
            return;
        }

        long[] emptyValueFieldIds = this.values.entrySet().stream()
            .filter(entry -> !EmptyTypedValue.class.isInstance(entry.getValue()))
            .mapToLong(entity -> entity.getKey()).toArray();
        for (long id : emptyValueFieldIds) {
            this.values.remove(id);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EntityValue cloneValue = new EntityValue(entity);
        cloneValue.values = new HashMap<>(values);
        return cloneValue;
    }

    private void lazyInit() {
        if (this.values == null) {
            this.values = new HashMap<>();
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
        return equalsValues(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        final int emptyLen = buff.length();
        values.values().stream().forEach(v -> {
            if (buff.length() > emptyLen) {
                buff.append(" , ");
            }
            buff.append("{")
                .append("id: ").append(v.getField().id())
                .append(", ")
                .append("name: ").append(v.getField().name())
                .append(", ")
                .append("type: ").append(v.getField().type().getType())
                .append(", ")
                .append("value: ").append(v.getValue().toString())
                .append("}");
        });
        buff.append("]");
        return buff.toString();
    }

    // 比较两个 map.
    private boolean equalsValues(EntityValue that) {
        if (that == null) {
            return false;
        }

        Map<Long, IValue> thatValues = that.values;
        if (this.values == thatValues) {
            return true;
        }

        if (thatValues.size() != this.values.size()) {
            return false;
        }

        IValue thisValue;
        IValue thatValue;
        for (Long id : this.values.keySet()) {
            thisValue = this.values.get(id);
            thatValue = thatValues.get(id);

            if (!thisValue.equals(thatValue)) {
                return false;
            }
        }

        return true;
    }
}
