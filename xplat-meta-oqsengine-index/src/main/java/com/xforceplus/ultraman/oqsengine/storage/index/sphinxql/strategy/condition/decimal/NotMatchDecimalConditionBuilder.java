package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 14:14
 * @since 1.8
 */
public abstract class NotMatchDecimalConditionBuilder extends SphinxQLConditionBuilder {

    public NotMatchDecimalConditionBuilder(StorageStrategyFactory storageStrategyFactory, ConditionOperator operator) {
        super(storageStrategyFactory, FieldType.DECIMAL, operator, false);
    }

    public abstract ConditionOperator intOperator();

    public abstract ConditionOperator decOperator();

    public abstract ConditionOperator orOperator();

    @Override
    protected String doBuild(Condition condition) {
        IValue logicValue = condition.getFirstValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(logicValue.getField().type());
        StorageValue iStorageValue = storageStrategy.toStorageValue(logicValue);

        StorageValue dStoragetValue = iStorageValue.next();
        if (dStoragetValue == null) {
            throw new IllegalStateException("Unexpected decimal places.");
        }
        String firstName = iStorageValue.shortStorageName().toString();
        String secondName = dStoragetValue.shortStorageName().toString();
        // andy.zhou 20200721
        StringBuilder buff = new StringBuilder();
        buff.append("(");
        buff.append("(");
        buff.append(FieldDefine.ATTRIBUTE).append(".").append(firstName).append(" ")
                .append(orOperator().getSymbol()).append(" ").append(iStorageValue.value());
        buff.append(") ");
        buff.append(SqlKeywordDefine.OR).append(" ");
        // end andy.zhou 20200721

        buff.append("(");
        buff.append(FieldDefine.ATTRIBUTE).append(".").append(firstName).append(" ")
                .append(intOperator().getSymbol()).append(" ").append(iStorageValue.value());
        buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
        buff.append(FieldDefine.ATTRIBUTE).append(".").append(secondName).append(" ")
                .append(decOperator().getSymbol()).append(" ").append(dStoragetValue.value());
        buff.append(")");
        buff.append(")");
        return buff.toString();
    }
}
