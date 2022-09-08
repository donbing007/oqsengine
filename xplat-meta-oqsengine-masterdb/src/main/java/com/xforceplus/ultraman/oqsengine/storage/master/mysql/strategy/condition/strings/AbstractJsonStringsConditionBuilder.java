package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.strings;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.AbstractConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * strings 类型的查询条件构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/9 15:22
 * @since 1.8
 */
public abstract class AbstractJsonStringsConditionBuilder extends AbstractConditionBuilder<String> {

    /**
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

    public AbstractJsonStringsConditionBuilder(ConditionOperator operator,
                                               StorageStrategyFactory storageStrategyFactory) {
        super(operator);
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public FieldType fieldType() {
        return FieldType.STRINGS;
    }

    public abstract String actualOperator();

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

    @Override
    public String doBuild(Condition condition) {
        IValue[] logicValues = condition.getValues();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(condition.getField().type());

        StringBuilder buff = new StringBuilder();
        if (operator() == ConditionOperator.MULTIPLE_EQUALS) {

            buff.append('(');
            int emptyLen = buff.length();

            StorageValue storageValue;
            for (IValue logicValue : logicValues) {
                storageValue = storageStrategy.toStorageValue(logicValue);

                if (buff.length() > emptyLen) {
                    buff.append(" OR ");
                }
                buildQuery(storageValue, buff);
            }

            buff.append(')');

        } else {

            final int firstValue = 0;
            buildQuery(storageStrategy.toStorageValue(logicValues[firstValue]), buff);

        }

        return buff.toString();
    }

    private void buildQuery(StorageValue storageValue, StringBuilder buff) {
        String queryValue = storageValue.value().toString();
        buff.append(FieldDefine.ATTRIBUTE).append("->>'$.")
            .append(AnyStorageValue.ATTRIBUTE_PREFIX).append(storageValue.storageName())
            .append("' ");
        buff.append(actualOperator()).append(" '%").append(queryValue).append("%'");
    }
}
