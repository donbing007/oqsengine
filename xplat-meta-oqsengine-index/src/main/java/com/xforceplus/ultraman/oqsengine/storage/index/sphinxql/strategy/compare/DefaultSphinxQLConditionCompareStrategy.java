package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Arrays;

/**
 * 默认的条件生成策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/6 14:14
 * @since 1.8
 */
public class DefaultSphinxQLConditionCompareStrategy implements SphinxQLConditionCompareStrategy {

    @Override
    public String build(String prefix, Condition condition, StorageStrategyFactory storageStrategyFactory) {

        StringBuilder buff = new StringBuilder();

        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(condition.getField().type());

        // 多值处理
        if (condition.getOperator() == ConditionOperator.MULTIPLE_EQUALS) {

            // 第一个值.
            final int first = 0;
            IValue[] values = condition.getValues();
            StorageValue storageValue = storageStrategy.toStorageValue(values[first]);

            if (condition.getField().config().isIdentifie()) {
                buff.append("id IN (");
            } else {
                buff.append(prefix).append(".")
                    .append(storageValue.storageName())
                    .append(" IN (");
            }

            int emptyLen = buff.length();
            // 首个值.
            buff.append(buildConditionValue(storageValue, storageStrategy));

            Arrays.stream(values).skip(1).map(v -> storageStrategy.toStorageValue(v)).forEach(s -> {
                if (buff.length() > emptyLen) {
                    buff.append(",");
                }
                buff.append(buildConditionValue(s, storageStrategy));
            });
            buff.append(")");

        } else {

            StorageValue storageValue = storageStrategy.toStorageValue(condition.getValue());

            while (storageValue != null) {

                String value = buildConditionValue(storageValue, storageStrategy);

                if (buff.length() > 0) {
                    buff.append(" ");
                }

                // 如果是系统字段数据标识.
                if (condition.getField().config().isIdentifie()) {
                    buff.append("id ");
                } else {
                    buff.append(prefix).append(".")
                        .append(storageValue.storageName())
                        .append(" ");
                }

                buff.append(condition.getOperator().getSymbol())
                    .append(" ")
                    .append(value);


                storageValue = storageValue.next();

            }

        }


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
