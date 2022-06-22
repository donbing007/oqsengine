package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.CalculationsAble;

/**
 * 表示一个整数.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class LongValue extends AbstractValue<Long> implements CalculationsAble<Long> {

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
    protected IValue<Long> doCopy(Long value) {
        return new LongValue(getField(), value, getAttachment().orElse(null));
    }

    @Override
    public boolean compareByString() {
        return false;
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
