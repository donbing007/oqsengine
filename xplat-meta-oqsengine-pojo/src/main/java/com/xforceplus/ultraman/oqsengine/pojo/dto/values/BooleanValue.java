package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;

/**
 * 表示 boolean 的属性名.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class BooleanValue extends AbstractValue<Boolean> {


    public BooleanValue(IEntityField field, Boolean value) {
        super(field, value);
    }

    @Override
    Boolean fromString(String value) {

        if (value.equalsIgnoreCase("1")) {
            return true;
        }

        if (value.equalsIgnoreCase("0")) {
            return false;
        }

        return Boolean.valueOf(value);
    }

    @Override
    public long valueToLong() {
        return getValue() ? 1L : 0;
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
        if (!(o instanceof BooleanValue)) {
            return false;
        }

        BooleanValue that = (BooleanValue) o;

        return Objects.equals(getField(), that.getField()) && Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public IValue<Boolean> shallowClone() {
        return new BooleanValue(this.getField(), getValue());
    }

    @Override
    public String toString() {
        return "BooleanValue{" + "field=" + getField() + ", value=" + getValue() + '}';
    }
}
