package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.decimal;

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
 * 浮点数条件查询构造器抽像.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 17:28
 * @since 1.8
 */
public abstract class AbstractJsonDecimalConditionBuilder extends AbstractConditionBuilder<String> {
    /**
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

    public AbstractJsonDecimalConditionBuilder(ConditionOperator operator, StorageStrategyFactory storageStrategyFactory) {
        super(operator);
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }

    @Override
    public String doBuild(Condition condition) {
        IValue logicValue = condition.getFirstValue();
        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        String[] decimalArry = storageValue.value().toString().split("\\.");
        // 按"."分割后应该长度为2.
        final int decimalLen = 2;
        if (decimalArry.length != decimalLen) {
            throw new IllegalStateException(
                String.format("Invalid floating point number.[%s]", storageValue.value().toString()));
        }

        final int intIndex = 0;
        final int decIndex = 1;
        StringBuilder sql = new StringBuilder();

        sql.append("(");
        sql.append("(");
        sql.append(buildSegment(storageValue.storageName(), true, orOperator(), decimalArry[intIndex]));
        sql.append(") ");
        sql.append(" OR ").append(" ");
        sql.append("(");
        sql.append(buildSegment(storageValue.storageName(), true, intOperator(), decimalArry[intIndex]));
        sql.append(" ").append(" AND ").append(" ");
        sql.append(buildSegment(storageValue.storageName(), false, decOperator(), decimalArry[decIndex]));
        sql.append(")");
        sql.append(")");

        return sql.toString();
    }

    // integer cast(substring_index(attribute->>'$.F123L','.',1) as SIGNED)
    // decimal cast(substring_index(attribute->>'$.F123L','.',-1) as SIGNED)
    private String buildSegment(String field, boolean integer, ConditionOperator operator, String value) {
        StringBuilder buff = new StringBuilder();
        buff.append("CAST(SUBSTRING_INDEX(")
            .append(FieldDefine.ATTRIBUTE).append("->>'$.")
            .append(AnyStorageValue.ATTRIBUTE_PREFIX)
            .append(field).append("','.',")
            .append(integer ? "1" : "-1")
            .append(") AS SIGNED) ")
            .append(operator.getSymbol())
            .append(" ")
            .append(value);
        return buff.toString();
    }

    public abstract ConditionOperator intOperator();

    public abstract ConditionOperator decOperator();

    public abstract ConditionOperator orOperator();
}
