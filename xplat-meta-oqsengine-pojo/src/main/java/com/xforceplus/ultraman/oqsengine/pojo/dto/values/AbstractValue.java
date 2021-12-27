package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 所有Value 对的超类实现.
 *
 * @param <V> 值实际类型.
 * @author dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public abstract class AbstractValue<V> implements IValue<V>, Serializable {

    private boolean dirty;
    private String attachment;
    private IEntityField field;
    private V value;



    /**
     * 构造一个新的逻辑值.
     * 默认没有附件.
     *
     * @param field 目标字段元信息.
     * @param value 实际值.
     */
    public AbstractValue(IEntityField field, V value) {
        this(field, value, null);
    }

    /**
     * 构造一个新的逻辑值.其会附带一个附件字符串.
     *
     * @param field 目标字段.
     * @param value 值.
     * @param attachment 附件.
     */
    public AbstractValue(IEntityField field, V value, String attachment) {
        this.field = field;
        this.value = value;
        this.attachment = attachment;
        this.dirty = true;
    }

    @Override
    public IEntityField getField() {
        return this.field;
    }

    @Override
    public void setField(IEntityField field) {
        this.field = field;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public Optional<String> getAttachment() {
        return Optional.ofNullable(attachment);
    }

    @Override
    public void setStringValue(String value) {
        this.value = fromString(value);
    }

    abstract V fromString(String value);

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void dirty() {
        this.dirty = true;
    }

    @Override
    public void neat() {
        this.dirty = false;
    }

    @Override
    public String valueToString() {
        if (value != null) {
            return value.toString();
        } else {
            return "NULL";
        }
    }

    @Override
    public abstract long valueToLong();

    @Override
    public IValue<V> copy(IEntityField newField, String attachment) {
        if (!skipTypeCheckWithCopy()) {
            checkType(newField);
        }
        return doCopy(newField, attachment);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("{");
        sb.append("field=").append(field);
        sb.append(", value=").append(value);
        sb.append(", dirty=").append(dirty);
        if (attachment != null) {
            sb.append(", attachment=").append(attachment);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractValue<?> that = (AbstractValue<?>) o;
        return Objects.equals(field.id(), that.field.id()) && Objects.equals(value, that.value)
            && Objects.equals(attachment, that.attachment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value, attachment);
    }

    protected abstract IValue<V> doCopy(IEntityField newField, String attachment);

    protected boolean skipTypeCheckWithCopy() {
        return false;
    }

    /**
     * 检查目标字段元信息的类型和当前是否相符.
     *
     * @param newFiled 目标字段.
     */
    private void checkType(IEntityField newFiled) {
        if (newFiled.type() != getField().type()) {
            throw new IllegalArgumentException(
                String.format("Field that doesn't fit.[newFieldId=%d, oldFieldId=%d, newType=%s, oldType=%s]",
                    newFiled.id(), getField().id(), newFiled.type().name(), getField().type().name()));
        }
    }
}
