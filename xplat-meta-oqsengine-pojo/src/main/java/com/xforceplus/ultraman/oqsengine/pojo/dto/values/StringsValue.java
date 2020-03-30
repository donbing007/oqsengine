package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dongbin
 * @version 0.1 2020/3/27 18:24
 * @since 1.8
 */
public class StringsValue extends AbstractValue<String[]> {

    public StringsValue(IEntityField field, String[] value) {
        super(field, value);
    }

    @Override
    public long valueToLong() {
        throw new UnsupportedOperationException("A string cannot be represented by a number.");
    }

    @Override
    public String valueToString() {
        return String.join(",", getValue());
    }
}
