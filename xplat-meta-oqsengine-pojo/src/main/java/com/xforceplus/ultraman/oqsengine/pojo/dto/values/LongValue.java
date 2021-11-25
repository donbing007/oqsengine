package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示一个整数.
 *
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
}
