package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;

/**
 * 字符串值表示.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class StringValue extends AbstractValue<String> {

    public StringValue(IEntityField field, String value) {
        super(field, value);
    }

    @Override
    String fromString(String value) {
        return value;
    }

    @Override
    public long valueToLong() {
        throw new UnsupportedOperationException("A string cannot be represented by a number.");
    }

    @Override
    protected void checkType(IEntityField newFiled) {
        if (newFiled.type() != FieldType.STRING) {
            throw new IllegalArgumentException(
                String.format("Field that doesn't fit.[newFieldId=%d, oldFieldId=%d, newType=%s, oldType=%s]",
                    newFiled.id(), getField().id(), newFiled.type().name(), getField().type().name()));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getField(), getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringValue)) {
            return false;
        }

        StringValue that = (StringValue) o;

        return Objects.equals(getField(), that.getField()) && Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public IValue<String> copy(IEntityField newField) {
        checkType(newField);

        return new StringValue(newField, getValue());
    }
}
