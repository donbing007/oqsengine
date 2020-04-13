package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 表示一个浮点数字段的值.
 *
 * @author dongbin
 * @version 0.1 2020/3/3 16:45
 * @since 1.8
 */
public class DecimalValue extends AbstractValue<BigDecimal> {

    public DecimalValue(IEntityField field, BigDecimal value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        return getValue().longValue();
    }

    @Override
    public String valueToString() {
        String value = getValue().toPlainString();
        // 补足小数.
        if (value.indexOf(".") < 0) {
            return value + ".0";
        } else {
            return value;
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
        if (!(o instanceof DecimalValue)) {
            return false;
        }

        DecimalValue that = (DecimalValue) o;

        return Objects.equals(getField(), that.getField()) &&
            Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public String toString() {
        return "DecimalValue{" +
            "field=" + getField() +
            ", value=" + getValue() +
            '}';
    }
}
