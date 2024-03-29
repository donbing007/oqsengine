package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 大于.
 *
 * @author dongbin
 * @version 0.1 2020/11/5 10:42
 * @since 1.8
 */
public class GtDecimalJsonConditionBuilder extends AbstractJsonDecimalConditionBuilder {

    public GtDecimalJsonConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.GREATER_THAN, storageStrategyFactory);
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
