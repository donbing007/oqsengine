package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.io.Serializable;

/**
 * 所有Value 对的超类实现.
 *
 * @param <V> 值实际类型.
 * @author dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public abstract class AbstractValue<V> implements IValue<V>, Serializable {

    private IEntityField field;
    private V value;

    /**
     * 构造一个新的逻辑值.
     *
     * @param field 目标字段元信息.
     * @param value 实际值.
     */
    public AbstractValue(IEntityField field, V value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public IEntityField getField() {
        return this.field;
    }

    @Override
    public void setField(IEntityField field) {
        this.field = field;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void setStringValue(String value) {
        this.value = fromString(value);
    }

    abstract V fromString(String value);

    @Override
    public String valueToString() {
        return value.toString();
    }

    @Override
    public abstract long valueToLong();

    @Override
    public IValue<V> copy() {
        return copy(getField());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("{");
        sb.append("field=").append(field);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

    protected void checkType(IEntityField newFiled) {
        if (newFiled.type() != getField().type()) {
            throw new IllegalArgumentException(
                String.format("Field that doesn't fit.[newFieldId=%d, oldFieldId=%d, newType=%s, oldType=%s]",
                    newFiled.id(), getField().id(), newFiled.type().name(), getField().type().name()));
        }
    }
}
