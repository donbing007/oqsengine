package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;

/**
 * 小于.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 14:20
 * @since 1.8
 */
public class LtNotMatchDecimalConditionBuilder extends AbstractNotMatchDecimalConditionBuilder {

    public LtNotMatchDecimalConditionBuilder() {
        super(ConditionOperator.LESS_THAN);
    }

    @Override
    public ConditionOperator intOperator() {
        return ConditionOperator.EQUALS;
    }

    @Override
    public ConditionOperator decOperator() {
        return ConditionOperator.LESS_THAN;
    }

    @Override
    public ConditionOperator orOperator() {
        return ConditionOperator.LESS_THAN;
    }
}
