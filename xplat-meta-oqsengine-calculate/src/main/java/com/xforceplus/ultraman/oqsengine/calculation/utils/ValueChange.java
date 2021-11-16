package com.xforceplus.ultraman.oqsengine.calculation.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示IValue的变化.
 *
 * @author dongbin
 * @version 0.1 2021/9/30 16:37
 * @since 1.8
 */
public class ValueChange {

    private long entityId;
    private IEntityField field;
    private IValue oldValue;
    private IValue newValue;

    /**
     * 构造一个新的ValueChanage实例.
     *
     * @param entityId 对象标识.
     * @param old      旧值.
     * @param newValue 新值.
     * @return 实例.
     */
    public static ValueChange build(long entityId, IValue old, IValue newValue) {
        return new ValueChange(entityId, old, newValue);
    }

    /**
     * 构造一个IValue的变化.
     *
     * @param entityId 对像标识.
     * @param oldValue 旧值.
     * @param newValue 新值.
     */
    public ValueChange(long entityId, IValue oldValue, IValue newValue) {
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;

        if (oldValue == null || newValue == null) {
            throw new NullPointerException("Invalid value object.");
        }

        if (this.oldValue.getField().id() != newValue.getField().id()) {
            throw new IllegalArgumentException(
                String.format("Different fields cannot be changed.[%s => %s]",
                    this.oldValue.getField().name(), this.newValue.getField().name()));
        }

        field = oldValue.getField();
    }

    public IEntityField getField() {
        return field;
    }

    public Optional<IValue> getOldValue() {
        return Optional.ofNullable(oldValue);
    }

    public Optional<IValue> getNewValue() {
        return Optional.ofNullable(newValue);
    }

    public long getEntityId() {
        return entityId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(oldValue.getField().name()).append(" { ");
        sb.append(oldValue != null ? oldValue.valueToString() : "NULL")
            .append(" => ")
            .append(newValue != null ? newValue.valueToString() : "NULL");
        sb.append(" }");

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
        ValueChange that = (ValueChange) o;
        return Objects.equals(entityId, that.entityId)
            && Objects.equals(field, that.field)
            && Objects.equals(oldValue, that.oldValue)
            && Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, field, oldValue, newValue);
    }
}
