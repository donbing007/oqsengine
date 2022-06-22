package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 一个空值表示.
 */
public class EmptyTypedValue extends AbstractValue {

    public EmptyTypedValue(IEntityField field) {
        super(field, ValueWithEmpty.EMPTY_VALUE);
    }

    @Override
    Object fromString(String value) {
        return ValueWithEmpty.EMPTY_VALUE;
    }

    @Override
    public long valueToLong() {
        return 0;
    }

    @Override
    protected IValue doCopy(IEntityField newField, String attachment) {
        return new EmptyTypedValue(newField);
    }

    @Override
    protected IValue doCopy(Object value) {
        return this;
    }

    @Override
    protected boolean skipTypeCheckWithCopy() {
        return true;
    }
}
