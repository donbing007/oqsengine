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

    /**
     * 构造一个新的DecimalValue实例.
     * 会检查整数位和小数位,两者均不可以大于长整形的最大表示位数,即19位数字.
     *
     * @param field 目标字段.
     * @param value 目标浮点数.
     */
    public DecimalValue(IEntityField field, BigDecimal value) {
        super(field, value);

        // 这里校验,其整形长度不能超过Long.MAX_VALUE
        String checkValue = value.toPlainString();
        String[] checkValues = checkValue.split("\\.");
        Long.parseLong(checkValues[0]);
        if (checkValues.length > 1) {
            Long.parseLong(checkValues[1]);
        }
    }

    @Override
    BigDecimal fromString(String value) {
        if (value != null) {
            return new BigDecimal(value);
        }
        return null;
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

    public long integerValue() {
        String[] values = getValue().toPlainString().split("\\.");
        return Long.parseLong(values[0]);
    }

    public long decValue() {
        String[] values = getValue().toPlainString().split("\\.");
        return Long.parseLong(values[1]);
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
}
