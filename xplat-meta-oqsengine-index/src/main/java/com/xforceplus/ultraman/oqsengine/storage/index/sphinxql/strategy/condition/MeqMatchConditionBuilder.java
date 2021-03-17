package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 14:33
 * @since 1.8
 */
public class MeqMatchConditionBuilder extends SphinxQLConditionBuilder {

    public MeqMatchConditionBuilder(StorageStrategyFactory storageStrategyFactory, FieldType fieldType, boolean useGroupName) {
        super(storageStrategyFactory, fieldType, ConditionOperator.MULTIPLE_EQUALS, true, useGroupName);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue[] values = condition.getValues();
        StringBuilder buff = new StringBuilder("(");
        int emptyLen = buff.length();

        String query;
        for (IValue v : values) {
            StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(v.getField().type());
            StorageValue storageValue = storageStrategy.toStorageValue(v);

            query = SphinxQLHelper.buildPreciseQuery(storageValue, isUseStorageGroupName());

            if (buff.length() > emptyLen) {
                buff.append(" | ");
            }

            buff.append(query);
        }
        buff.append(")");

        return buff.toString();
    }
}
