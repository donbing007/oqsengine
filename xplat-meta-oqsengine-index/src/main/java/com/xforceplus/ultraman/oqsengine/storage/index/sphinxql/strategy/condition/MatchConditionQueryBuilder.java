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
 * 用于 match 函数中的匹配条件构造器.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 10:13
 * @since 1.8
 */
public class MatchConditionQueryBuilder extends SphinxQLConditionQueryBuilder {


    public MatchConditionQueryBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator) {
        super(storageStrategyFactory, fieldType, operator, true);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue logicValue = condition.getValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(logicValue.getField().type());
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        StringBuilder buff = new StringBuilder();

        String symbol;
        while (storageValue != null) {
            switch (operator()) {
                case NOT_EQUALS:
                    symbol = "-";
                    break;
                case EQUALS:
                    symbol = "=";
                    break;
                case LIKE:
                    symbol = "";
                    break;
                default:
                    throw new IllegalStateException(String.format("Unsupported operator.[%s]", operator().getSymbol()));
            }

            if (buff.length() > 0) {
                buff.append(" ");
            }
            buff.append(symbol).append(SphinxQLHelper.encodeFullText(storageValue));

            storageValue = storageValue.next();
        }

        return buff.toString();
    }
}
