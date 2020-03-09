package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 默认的条件生成策略.
 * @author dongbin
 * @version 0.1 2020/3/6 14:14
 * @since 1.8
 */
public class DefaultSphinxQLConditionCompareStrategy implements SphinxQLConditionCompareStrategy {

    @Override
    public String build(String prefix, Condition condition, StorageStrategy storageStrategy) {
        StorageValue storageValue = storageStrategy.toStorageValue(condition.getValue());

        StringBuilder buff = new StringBuilder();
        while(storageValue != null) {

            String value;
            if (storageStrategy.storageType() == StorageType.STRING) {
                value = "'" + storageValue.value() + "'";
            } else {
                value = storageValue.value().toString();
            }

            if (buff.length() > 0) {
                buff.append(" ");
            }
            buff.append(prefix).append(".")
                .append(storageValue.storageName())
                .append(" ")
                .append(condition.getOperator().getSymbol())
                .append(" ")
                .append(value);

            storageValue = storageValue.next();

        }


        return buff.toString();
    }
}
