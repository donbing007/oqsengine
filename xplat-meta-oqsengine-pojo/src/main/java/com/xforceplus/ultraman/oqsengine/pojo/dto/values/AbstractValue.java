package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.Objects;

/**
 * 所有Value 对的超类实现.
 * @author dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public abstract class AbstractValue<V> implements IValue<V> {

    private Long id;
    private String name;
    private V value;

    public AbstractValue(Long id, String name, V value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    @Override
    public Long getId() {return id;}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public String valueToString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractValue)) return false;
        AbstractValue<?> that = (AbstractValue<?>) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getValue());
    }

    @Override
    public abstract long valueToLong();
}
