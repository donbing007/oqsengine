package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 小于.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 14:20
 * @since 1.8
 */
public class LtDecimalJsonConditionBuilder extends AbstractJsonDecimalConditionBuilder {

    public LtDecimalJsonConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.LESS_THAN, storageStrategyFactory);
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
