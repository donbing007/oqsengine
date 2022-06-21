package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.CalculationsAble;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.NumberPredefinedValueAble;
import java.math.BigDecimal;

/**
 * 表示一个浮点数字段的值.
 *
 * @author dongbin
 * @version 0.1 2020/3/3 16:45
 * @since 1.8
 */
public class DecimalValue extends AbstractValue<BigDecimal>
    implements NumberPredefinedValueAble<BigDecimal>, CalculationsAble<BigDecimal> {

    public DecimalValue(IEntityField field, BigDecimal value) {
        this(field, value, null);
    }

    public DecimalValue(IEntityField field, BigDecimal value, String attachment) {
        super(field, buildWellBigDecimal(field, value), attachment);
    }

    @Override
    protected BigDecimal fromString(String value) {
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

    @Override
    public IValue<BigDecimal> max() {
        return new DecimalValue(getField(), new BigDecimal(Long.MAX_VALUE), getAttachment().orElse(null));
    }

    @Override
    public IValue<BigDecimal> min() {
        return new DecimalValue(getField(), new BigDecimal(Long.MIN_VALUE), getAttachment().orElse(null));
    }

    /**
     * 获取0值表示.
     *
     * @param field 目标字段.
     * @return 0值.
     */
    public static IValue<BigDecimal> zero(IEntityField field) {
        if (field.type() != FieldType.DECIMAL) {
            throw new IllegalArgumentException(
                String.format("Incompatible type. Expected %s.", FieldType.DECIMAL.name()));
        }

        return new DecimalValue(field, BigDecimal.ZERO);
    }

    @Override
    public CalculationsAble<BigDecimal> plus(IValue<BigDecimal> other) {
        BigDecimal left = this.getValue();
        BigDecimal right = other.getValue();

        BigDecimal reuslt = left.add(right);
        return new DecimalValue(getField(), reuslt, getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<BigDecimal> subtract(IValue<BigDecimal> other) {
        BigDecimal left = this.getValue();
        BigDecimal right = other.getValue();

        BigDecimal reuslt = left.subtract(right);
        return new DecimalValue(getField(), reuslt, getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<BigDecimal> decrement() {
        return new DecimalValue(getField(), this.getValue().subtract(BigDecimal.ONE), getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<BigDecimal> increment() {
        return new DecimalValue(getField(), this.getValue().add(BigDecimal.ONE), getAttachment().orElse("null"));
    }
}
