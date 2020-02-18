package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

/**
 *  枚举值.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class EnumValue extends AbstractValue<String> {

    public EnumValue(String name, String value) {
        super(name, value);
    }

    @Override
    public long valueToLong() {
        return 0;
    }
}
