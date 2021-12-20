package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 字符串值表示.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class StringValue extends AbstractValue<String> {

    public StringValue(IEntityField field, String value) {
        super(field, value);
    }

    public StringValue(IEntityField field, String value, String attachment) {
        super(field, value, attachment);
    }

    @Override
    String fromString(String value) {
        return value;
    }

    @Override
    public long valueToLong() {
        throw new UnsupportedOperationException("A string cannot be represented by a number.");
    }

    @Override
    protected IValue<String> doCopy(IEntityField newField, String attachment) {
        return new StringValue(newField, getValue(), attachment);
    }
}
