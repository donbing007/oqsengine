package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 大于等于.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 17:26
 * @since 1.8
 */
public class GtEqDecimalJsonConditionBuilder extends AbstractJsonDecimalConditionBuilder {

    public GtEqDecimalJsonConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.GREATER_THAN_EQUALS, storageStrategyFactory);
    }

    @Override
    public ConditionOperator intOperator() {
        return ConditionOperator.EQUALS;
    }

    @Override
    public ConditionOperator decOperator() {
        return ConditionOperator.GREATER_THAN_EQUALS;
    }

    @Override
    public ConditionOperator orOperator() {
        return ConditionOperator.GREATER_THAN;
    }
}
