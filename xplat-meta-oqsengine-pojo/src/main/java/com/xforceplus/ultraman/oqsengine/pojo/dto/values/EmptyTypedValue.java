package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 空值表示,分为含有附件和不含有附件将解读为不同的意义.<br>
 * 不含附件:
 * <ul>
 *     <li>创建时忽略</li>
 *     <li>更新时删除已有</li>
 * </ul>
 * 含有附件:
 * <ul>
 *     <li>只创建附件</li>
 *     <li>只更新附件</li>
 * </ul>
 * 删除时是忽略字段处理,所以不会影响.
 */
public class EmptyTypedValue extends AbstractValue {

    public EmptyTypedValue(IEntityField field) {
        super(field, ValueWithEmpty.EMPTY_VALUE);
    }

    public EmptyTypedValue(IEntityField field, String attachment) {
        super(field, ValueWithEmpty.EMPTY_VALUE, attachment);
    }

    @Override
    Object fromString(String value) {
        return ValueWithEmpty.EMPTY_VALUE;
    }

    @Override
    public long valueToLong() {
        return 0;
    }

    @Override
    protected IValue doCopy(IEntityField newField, String attachment) {
        return new EmptyTypedValue(newField, attachment);
    }

    @Override
    protected IValue doCopy(Object value) {
        return this;
    }

    @Override
    protected boolean skipTypeCheckWithCopy() {
        return true;
    }
}
