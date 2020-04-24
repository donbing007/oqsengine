package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Objects;

/**
 * 表示一个整数.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class LongValue extends AbstractValue<Long> {

    public LongValue(IEntityField field, int value) {
        super(field, (long) value);
    }

    public LongValue(IEntityField field, long value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        return getValue();
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
        if (!(o instanceof LongValue)) {
            return false;
        }

        LongValue that = (LongValue) o;

        return Objects.equals(getField(), that.getField()) &&
            Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public String toString() {
        return "LongValue{" +
            "field=" + getField() +
            ", value=" + getValue() +
            '}';
    }
}
