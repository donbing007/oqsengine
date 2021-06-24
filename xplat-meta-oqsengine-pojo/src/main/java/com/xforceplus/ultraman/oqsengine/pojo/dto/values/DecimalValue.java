package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        super(field, buildWellBigDecimal(field, value));
    }

    @Override
    BigDecimal fromString(String value) {
        if (value != null) {
            return buildWellBigDecimal(getField(), new BigDecimal(value));
        }
        return null;
    }

    @Override
    public long valueToLong() {
        return getValue().longValue();
    }

    @Override
    public String valueToString() {
        return getValue().toPlainString();
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

        return Objects.equals(getField(), that.getField()) && Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public IValue<BigDecimal> shallowClone() {
        return new DecimalValue(this.getField(), getValue());
    }

    @Override
    public String toString() {
        return "DecimalValue{" + "field=" + getField() + ", value=" + getValue() + '}';
    }

    // 保证至少有一位数度.
    private static BigDecimal buildWellBigDecimal(IEntityField field, BigDecimal value) {
        String plainValue = value.toPlainString();
        if (plainValue.indexOf(".") < 0) {
            plainValue = Long.toString(value.longValue()) + ".0";
        } else {
            plainValue = value.toPlainString();
        }

        return new BigDecimal(plainValue);
    }
}
