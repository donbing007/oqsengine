package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.io.Serializable;

/**
 * 表示一个查询条件.
 */
public class Condition implements Serializable {

    private IEntityField field;

    private IValue value;

    private ConditionOperator operator;

    /**
     * 构造一个查询条件.
     *
     * @param field    目标字段.
     * @param operator 操作符.
     * @param value    值.
     */
    public Condition(IEntityField field, ConditionOperator operator, IValue value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public IEntityField getField() {
        return field;
    }

    public IValue getValue() {
        return value;
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return field.name() + operator.getSymbol() + value.getValue().toString();
    }
}
