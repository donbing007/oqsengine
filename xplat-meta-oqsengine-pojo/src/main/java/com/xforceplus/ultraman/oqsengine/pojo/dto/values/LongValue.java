package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示一个整数.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class LongValue extends AbstractValue<Long> {

    public LongValue(IEntityField field, Long value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        return getValue();
    }
}
