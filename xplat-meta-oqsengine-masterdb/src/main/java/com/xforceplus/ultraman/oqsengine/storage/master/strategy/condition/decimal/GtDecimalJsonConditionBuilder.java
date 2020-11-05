package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/11/5 10:42
 * @since 1.8
 */
public class GtDecimalJsonConditionBuilder extends AbstractJsonDecimalConditionBuilder {

    public GtDecimalJsonConditionBuilder(ConditionOperator operator, StorageStrategyFactory storageStrategyFactory) {
        super(operator, storageStrategyFactory);
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
