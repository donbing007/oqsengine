package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 空值表示.
 */
public class EmptyTypedValue extends AbstractValue {

    private static final String NULL_VALUE = "NULL";

    public EmptyTypedValue(IEntityField field) {
        super(field, NULL_VALUE);
    }

    @Override
    Object fromString(String value) {
        return NULL_VALUE;
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
