package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.EmptyValue;

/**
 * empty value will never use in real process
 */
public class EmptyTypedValue extends AbstractValue {

    public EmptyTypedValue(IEntityField field) {
        super(field, EmptyValue.emptyValue);
    }

    @Override
    Object fromString(String value) {
        return EmptyValue.emptyValue;
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
