package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

/**
 * 字符串值表示.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class StringValue extends AbstractValue<String> {

    public StringValue(Long id, String name, String value) {
        super(id, name, value);
    }

    @Override
    public long valueToLong() {
        throw new UnsupportedOperationException("A string cannot be represented by a number.");
    }
}
