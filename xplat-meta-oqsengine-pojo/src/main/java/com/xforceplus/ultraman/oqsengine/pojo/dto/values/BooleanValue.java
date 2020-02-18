package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

/**
 * 表示 boolean 的属性名.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class BooleanValue extends AbstractValue<Boolean> {


    public BooleanValue(String name, Boolean value) {
        super(name, value);
    }

    @Override
    public long valueToLong() {
        return getValue() ? 1L : 0;
    }
}
