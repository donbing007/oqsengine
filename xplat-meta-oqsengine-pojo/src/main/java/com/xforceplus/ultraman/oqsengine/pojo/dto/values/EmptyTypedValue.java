package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * empty value will never use in real process.
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
    protected void checkType(IEntityField newFiled) {
        return;
    }

    @Override
    public IValue copy(IEntityField newField) {
        return new EmptyTypedValue(newField);
    }
}
