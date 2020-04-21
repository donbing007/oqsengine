package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 所有Value 对的超类实现.
 * @author dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 * @param <V>
 */
public abstract class AbstractValue<V> implements IValue<V> {

    private IEntityField field;
    private V value;

    public AbstractValue(IEntityField field, V value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public IEntityField getField() {
        return this.field;
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
    public abstract long valueToLong();
}
