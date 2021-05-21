package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * empty value will never use in real process.
 */
public class EmptyTypedValue extends AbstractValue {

    public EmptyTypedValue(IEntityField field) {
        super(field, EmptyValue.EMPTY_VALUE);
    }

    @Override
    Object fromString(String value) {
        return EmptyValue.EMPTY_VALUE;
    }

    @Override
    public long valueToLong() {
        return 0;
    }

    @Override
    public IValue shallowClone() {
        return this;
    }
}
