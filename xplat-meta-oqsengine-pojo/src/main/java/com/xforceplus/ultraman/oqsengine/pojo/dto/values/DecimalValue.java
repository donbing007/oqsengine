package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.math.BigDecimal;

/**
 * 表示一个浮点数字段的值.
 *
 * @author dongbin
 * @version 0.1 2020/3/3 16:45
 * @since 1.8
 */
public class DecimalValue extends AbstractValue<BigDecimal> {

    public DecimalValue(IEntityField field, BigDecimal value) {
        this(field, value, null);
    }

    public DecimalValue(IEntityField field, BigDecimal value, String attachment) {
        super(field, buildWellBigDecimal(field, value), attachment);
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
    protected IValue<BigDecimal> doCopy(IEntityField newField, String attachment) {
        return new DecimalValue(newField, getValue(), attachment);
    }

    @Override
    public String valueToString() {
        return getValue().toPlainString();
    }

    // 保证至少有一位数度.
    private static BigDecimal buildWellBigDecimal(IEntityField field, BigDecimal value) {

        BigDecimal wellValue;
        String plainValue = value.toPlainString();

        // 这里校验,其整形长度不能超过Long.MAX_VALUE.
        String[] checkValues = plainValue.split("\\.");
        Long.parseLong(checkValues[0]);

        // 校验小数长度不能超过Long.MAX_VALUE.
        if (checkValues.length > 1) {
            Long.parseLong(checkValues[1]);
        }

        if (plainValue.indexOf(".") < 0) {
            plainValue = value.longValue() + ".0";
            wellValue = new BigDecimal(plainValue);
        } else {
            wellValue = value;
        }

        return wellValue;
    }
}
