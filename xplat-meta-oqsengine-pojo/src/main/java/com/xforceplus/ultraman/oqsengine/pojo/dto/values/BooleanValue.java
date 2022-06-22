package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示 boolean 的属性名.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class BooleanValue extends AbstractValue<Boolean> {


    public BooleanValue(IEntityField field, Boolean value) {
        super(field, value);
    }

    public BooleanValue(IEntityField field, Boolean value, String attachment) {
        super(field, value, attachment);
    }

    @Override
    Boolean fromString(String value) {

        if (value.equalsIgnoreCase("1")) {
            return true;
        }

        if (value.equalsIgnoreCase("0")) {
            return false;
        }

        return Boolean.valueOf(value);
    }

    @Override
    public long valueToLong() {
        return getValue() ? 1L : 0;
    }

    @Override
    protected IValue<Boolean> doCopy(IEntityField newField, String attachment) {
        return new BooleanValue(newField, getValue(), attachment);
    }

    @Override
    protected IValue<Boolean> doCopy(Boolean value) {
        return new BooleanValue(getField(), value, getAttachment().orElse(null));
    }

}
