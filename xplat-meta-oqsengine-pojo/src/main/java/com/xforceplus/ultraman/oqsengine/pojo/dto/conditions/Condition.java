package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 表示一个查询条件.
 */
public class Condition implements Serializable {

    private IEntityField field;

    private IValue[] values;

    private ConditionOperator operator;

    private boolean range;

    /**
     * 构造一个查询条件.
     *
     * @param field    目标字段.
     * @param operator 操作符.
     * @param values   值.
     */
    public Condition(IEntityField field, ConditionOperator operator, IValue ...values) {
        this.field = field;
        this.operator = operator;
        this.values = values;

        checkRange();

    }

    public IEntityField getField() {
        return field;
    }

    public IValue getValue() {
        return values[0];
    }

    public IValue[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    public boolean isRange() {
        return range;
    }

    // 判断是否含有范围查询符号.
    private void checkRange() {
        switch (getOperator()) {
            case MINOR_THAN:
            case GREATER_THAN:
            case MINOR_THAN_EQUALS:
            case GREATER_THAN_EQUALS:
            case MULTIPLE_EQUALS:
                range = true;
                break;
            default:
                range = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Condition)) {
            return false;
        }
        Condition condition = (Condition) o;
        return isRange() == condition.isRange() &&
            Objects.equals(getField(), condition.getField()) &&
            Arrays.equals(getValues(), condition.getValues()) &&
            getOperator() == condition.getOperator();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getField(), getOperator(), isRange());
        result = 31 * result + Arrays.hashCode(getValues());
        return result;
    }

    @Override
    public String toString() {
        return "Condition{" +
            "field=" + field +
            ", values=" + Arrays.toString(values) +
            ", operator=" + operator +
            ", range=" + range +
            '}';
    }
}
