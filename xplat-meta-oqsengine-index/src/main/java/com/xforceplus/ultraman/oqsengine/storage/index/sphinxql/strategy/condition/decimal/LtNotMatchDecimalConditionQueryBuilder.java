package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 14:20
 * @since 1.8
 */
public class LtNotMatchDecimalConditionQueryBuilder extends NotMatchDecimalConditionQueryBuilder {

    public LtNotMatchDecimalConditionQueryBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(storageStrategyFactory, ConditionOperator.LESS_THAN);
    }

    @Override
    public ConditionOperator intOperator() {
        return ConditionOperator.LESS_THAN_EQUALS;
    }

    @Override
    public ConditionOperator decOperator() {
        return ConditionOperator.LESS_THAN;
    }
}
