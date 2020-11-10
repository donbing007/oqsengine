package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * @author dongbin
 * @version 0.1 2020/11/9 15:40
 * @since 1.8
 */
public abstract class AbstractConditionBuilder<T> implements ConditionBuilder<T> {

    private ConditionOperator operator;

    public AbstractConditionBuilder(ConditionOperator operator) {
        this.operator = operator;
    }

    @Override
    public ConditionOperator operator() {
        return this.operator;
    }

    @Override
    public T build(Condition condition) {
        IEntityField field = condition.getField();
        if (fieldType() != field.type()) {
            throw new IllegalArgumentException("Invalid value type.");
        }

        return doBuild(condition);
    }

    public abstract T doBuild(Condition condition);
}
