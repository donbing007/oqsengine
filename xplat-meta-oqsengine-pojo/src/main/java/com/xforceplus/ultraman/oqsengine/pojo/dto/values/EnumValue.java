package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 *  枚举值.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class EnumValue extends AbstractValue<String> {

    /**
     * 多个值之间的分隔符.
     */
    public static final String DELIMITER = ",";

    public EnumValue(IEntityField field, String value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        return 0;
    }
}
