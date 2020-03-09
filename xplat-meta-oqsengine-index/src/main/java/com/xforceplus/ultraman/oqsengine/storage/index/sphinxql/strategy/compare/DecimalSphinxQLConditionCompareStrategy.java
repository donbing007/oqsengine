package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 浮点数的条件比较构造实现.
 * @author dongbin
 * @version 0.1 2020/3/6 14:02
 * @since 1.8
 */
public class DecimalSphinxQLConditionCompareStrategy implements SphinxQLConditionCompareStrategy {

    @Override
    public String build(String fieldPrefix, Condition condition, StorageStrategy storageStrategy) {
        if (condition.getField().type() != FieldType.DECIMAL) {
            throw new IllegalStateException("Field types other than Decimal are not supported.");
        }

        StorageValue storageValue = storageStrategy.toStorageValue(condition.getValue());

        switch(condition.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
                return doBuild(fieldPrefix, storageValue, condition.getOperator().getSymbol()) + " and " +
                    doBuild(fieldPrefix, storageValue.next(), condition.getOperator().getSymbol());
            case GREATER_THAN:
                return doBuild(fieldPrefix, storageValue, ConditionOperator.GREATER_THAN_EQUALS.getSymbol()) + " and " +
                    doBuild(fieldPrefix, storageValue.next(), ConditionOperator.GREATER_THAN.getSymbol());
            case GREATER_THAN_EQUALS:
                return doBuild(fieldPrefix, storageValue, ConditionOperator.GREATER_THAN_EQUALS.getSymbol()) + " and " +
                    doBuild(fieldPrefix, storageValue.next(), ConditionOperator.GREATER_THAN_EQUALS.getSymbol());
            case MINOR_THAN:
                return doBuild(fieldPrefix, storageValue, ConditionOperator.MINOR_THAN_EQUALS.getSymbol()) + " and " +
                    doBuild(fieldPrefix, storageValue.next(), ConditionOperator.MINOR_THAN.getSymbol());
            case MINOR_THAN_EQUALS:
                return doBuild(fieldPrefix, storageValue, ConditionOperator.MINOR_THAN_EQUALS.getSymbol()) + " and " +
                    doBuild(fieldPrefix, storageValue.next(), ConditionOperator.MINOR_THAN_EQUALS.getSymbol());
            default:
                throw new IllegalStateException(
                    String.format("%s does not support conditional comparison notation (%s), which should be a BUG.",
                        condition.getField().type().getType(),condition.getOperator().getSymbol()));

        }
    }

    private String doBuild(String fieldPrefix, StorageValue value, String operator) {
        return fieldPrefix + "." + value.storageName() + " " + operator + " " + value.value().toString();
    }
}
