package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示指向一个外部对象的字段.
 * 其中持有的IEntityField实例将和普通字段不同.
 * 其学段ID将使用当前字段,但是类型将是目标字段的类型.
 * 所以lookup字段的字段类型是随着目标字面不同而不同的.
 * </p>
 * lookupValue ->   DecimalValue   此lookup字段类型就是浮点型.
 * lookupValue ->   StringValue    此lookup字段类型就是字符串.
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

    /**
     * 构造一个新的外部字段指向.
     *
     * @param field 目标字段元信息.
     * @param value 实际值.
     * @param attachment 附件.
     */
    public LookupValue(IEntityField field, long value, String attachment) {
        super(field, value, attachment);
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
    protected IValue<Long> doCopy(IEntityField newField, String attachment) {
        return new LongValue(newField, getValue(), attachment);
    }

    @Override
    protected boolean skipTypeCheckWithCopy() {
        return true;
    }
}
