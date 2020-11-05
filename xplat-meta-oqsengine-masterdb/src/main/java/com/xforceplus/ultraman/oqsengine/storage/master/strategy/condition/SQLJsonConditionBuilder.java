package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * @author dongbin
 * @version 0.1 2020/11/4 15:56
 * @since 1.8
 */
public class SQLJsonConditionBuilder implements ConditionBuilder<String> {

    private FieldType fieldType;
    private ConditionOperator operator;
    /**
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

    public SQLJsonConditionBuilder(
        FieldType fieldType, ConditionOperator operator, StorageStrategyFactory storageStrategyFactory) {
        this.fieldType = fieldType;
        this.operator = operator;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public FieldType fieldType() {
        return fieldType;
    }

    @Override
    public ConditionOperator operator() {
        return operator;
    }

    @Override
    public String build(Condition condition) {
        IEntityField field = condition.getField();
        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(field.type());
        StorageValue storageValue = storageStrategy.toStorageValue(condition.getFirstValue());

        StringBuilder sql = new StringBuilder();
        while (storageValue != null) {
            sql.append(FieldDefine.ATTRIBUTE)
                .append("->>'$.")
                .append(FieldDefine.ATTRIBUTE_PREFIX).append(storageValue.storageName())
                .append("\' ")
                .append(condition.getOperator().getSymbol())
                .append(' ');
            if (storageValue.type() == StorageType.STRING) {
                sql.append("\"").append(storageValue.value()).append("\"");
            } else {
                sql.append(storageValue.value());
            }

            storageValue = storageValue.next();
        }

        return sql.toString();
    }
}
