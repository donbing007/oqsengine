package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    /*
     * Entity的值集合
     */
    private Map<Long, IValue> values;

    /**
     * 获得值实例.
     *
     * @return 实例.
     */
    public static IEntityValue build() {
        return new EntityValue();
    }

    @Override
    public int size() {
        return values == null ? 0 : values.size();
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
    public Optional<IValue> getValue(long fieldId) {
        if (values == null) {
            return Optional.empty();

        }

        return values.entrySet().stream().filter(x -> x.getKey() == fieldId)
            .map(Map.Entry::getValue).findFirst();
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
    public Optional<IValue> remove(IEntityField field) {
        lazyInit();

        return Optional.ofNullable(values.remove(field.id()));
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
    public Object clone() throws CloneNotSupportedException {
        EntityValue cloneValue = (EntityValue) EntityValue.build().addValues(values());
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
