package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.math.BigDecimal;

/**
 * 表示一个浮点数字段的值.
 *
 * @author dongbin
 * @version 0.1 2020/3/3 16:45
 * @since 1.8
 */
public class DecimalValue extends AbstractValue<BigDecimal> {

    public DecimalValue(IEntityField field, BigDecimal value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        return getValue().longValue();
    }
}
