package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Arrays;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 14:42
 * @since 1.8
 */
public class MeqNotMatchConditionQueryBuilder extends SphinxQLConditionQueryBuilder {

    public MeqNotMatchConditionQueryBuilder(StorageStrategyFactory storageStrategyFactory, FieldType fieldType) {
        super(storageStrategyFactory, fieldType, ConditionOperator.MULTIPLE_EQUALS, false);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue firstValue = condition.getValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(condition.getField().type());
        StorageValue sValue = storageStrategy.toStorageValue(firstValue);
        StringBuilder buff = new StringBuilder();

        if (condition.getField().config().isIdentifie()) {
            buff.append(FieldDefine.ID);
        } else {
            buff.append(FieldDefine.JSON_FIELDS).append(".").append(sValue.storageName());
        }
        buff.append(" IN (");
        buff.append(buildConditionValue(sValue, storageStrategy));

        Arrays.stream(condition.getValues()).skip(1).map(v -> storageStrategy.toStorageValue(v)).forEach(s -> {
            buff.append(",").append(buildConditionValue(s, storageStrategy));
        });

        buff.append(")");

        return buff.toString();
    }

    private String buildConditionValue(StorageValue storageValue, StorageStrategy storageStrategy) {
        String conditionValue;
        if (storageStrategy.storageType() == StorageType.STRING) {
            conditionValue = "'" + storageValue.value() + "'";
        } else {
            conditionValue = storageValue.value().toString();
        }
        return conditionValue;
    }
}
