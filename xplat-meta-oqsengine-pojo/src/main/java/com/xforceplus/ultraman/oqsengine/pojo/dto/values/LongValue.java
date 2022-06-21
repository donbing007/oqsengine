package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.CalculationsAble;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.NumberPredefinedValueAble;

/**
 * 表示一个整数.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class LongValue extends AbstractValue<Long> implements NumberPredefinedValueAble<Long>, CalculationsAble<Long> {

    public LongValue(IEntityField field, int value) {
        super(field, (long) value);
    }

    public LongValue(IEntityField field, long value) {
        super(field, value);
    }

    public LongValue(IEntityField field, int value, String attachment) {
        super(field, (long) value, attachment);
    }

    public LongValue(IEntityField field, long value, String attachment) {
        super(field, value, attachment);
    }

    @Override
    Long fromString(String value) {
        return value == null ? null : Long.parseLong(value);
    }

    @Override
    public long valueToLong() {
        return getValue();
    }

    @Override
    protected IValue<Long> doCopy(IEntityField newField, String attachment) {
        return new LongValue(newField, getValue(), attachment);
    }

    @Override
    public boolean compareByString() {
        return false;
    }

    @Override
    public IValue<Long> max() {
        return new LongValue(getField(), Long.MAX_VALUE, getAttachment().orElse(null));
    }

    @Override
    public IValue<Long> min() {
        return new LongValue(getField(), Long.MIN_VALUE, getAttachment().orElse(null));
    }

    /**
     * 获取0值表示.
     *
     * @param field 目标字段.
     * @return 0值.
     */
    public static IValue<Long> zero(IEntityField field) {
        if (field.type() != FieldType.LONG) {
            throw new IllegalArgumentException(
                String.format("Incompatible type. Expected %s.", FieldType.LONG.name()));
        }

        return new LongValue(field, 0L);
    }

    @Override
    public CalculationsAble<Long> plus(IValue<Long> other) {
        long left = this.getValue();
        long right = other.getValue();

        return new LongValue(getField(), left + right, getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<Long> subtract(IValue<Long> other) {
        long left = this.getValue();
        long right = other.getValue();

        return new LongValue(getField(), left - right, getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<Long> decrement() {
        return new LongValue(getField(), getValue() - 1, getAttachment().orElse("null"));
    }

    @Override
    public CalculationsAble<Long> increment() {
        return new LongValue(getField(), getValue() + 1, getAttachment().orElse("null"));
    }
}
