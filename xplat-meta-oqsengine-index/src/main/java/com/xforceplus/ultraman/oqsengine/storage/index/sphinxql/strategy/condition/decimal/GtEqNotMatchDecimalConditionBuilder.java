package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 非 match
 *
 * @author dongbin
 * @version 0.1 2020/3/26 14`:13
 * @since 1.8
 */
public class GtEqNotMatchDecimalConditionBuilder extends NotMatchDecimalConditionBuilder {

    public GtEqNotMatchDecimalConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(storageStrategyFactory, ConditionOperator.GREATER_THAN_EQUALS);
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
