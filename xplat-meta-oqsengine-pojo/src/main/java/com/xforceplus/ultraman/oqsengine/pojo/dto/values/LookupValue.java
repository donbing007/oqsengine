package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示指向一个外部对象的字段.
 *
 * @author dongbin
 * @version 0.1 2021/06/28 10:13
 * @since 1.8
 */
public class LookupValue extends AbstractValue<Long> {

    /**
     * 构造一个新的外部字段指向.
     *
     * @param field 目标字段元信息.
     * @param value 实际值.
     */
    public LookupValue(IEntityField field, long value) {
        super(field, value);
    }

    @Override
    Long fromString(String value) {
        return Long.parseLong(value);
    }

    @Override
    public long valueToLong() {
        return getValue();
    }

    @Override
    public IValue<Long> shallowClone() {
        return new LookupValue(getField(), getValue());
    }
}
