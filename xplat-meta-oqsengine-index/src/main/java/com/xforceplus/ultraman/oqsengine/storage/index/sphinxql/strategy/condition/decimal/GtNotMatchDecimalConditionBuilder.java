package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 13:58
 * @since 1.8
 */
public class GtNotMatchDecimalConditionBuilder extends NotMatchDecimalConditionBuilder {


    public GtNotMatchDecimalConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(storageStrategyFactory, ConditionOperator.GREATER_THAN);
    }

    @Override
    public ConditionOperator intOperator() {
        return ConditionOperator.EQUALS;
    }

    @Override
    public ConditionOperator decOperator() {
        return ConditionOperator.GREATER_THAN;
    }

    @Override
    public ConditionOperator orOperator() {
        return ConditionOperator.GREATER_THAN;
    }
}
