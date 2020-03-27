package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionQueryBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Objects;

/**
 * SphinxQL 的条件构造器.
 *
 * @author dongbin
 * @version 0.1 2020/3/25 18:14
 * @since 1.8
 */
public abstract class SphinxQLConditionQueryBuilder implements ConditionQueryBuilder<String> {

    private FieldType fieldType;
    private ConditionOperator operator;
    private boolean match;
    private StorageStrategyFactory storageStrategyFactory;

    public SphinxQLConditionQueryBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator) {
        this.storageStrategyFactory = storageStrategyFactory;
        this.fieldType = fieldType;
        this.operator = operator;
    }

    public SphinxQLConditionQueryBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator, boolean match) {
        this.fieldType = fieldType;
        this.operator = operator;
        this.match = match;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
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
        if (condition.getField().type() != fieldType
            || condition.getOperator() != operator) {
            throw new IllegalStateException(
                String.format("Unable to construct a query condition.[%s %s]",
                    condition.getOperator().getSymbol(), condition.getField().type().getType())
            );
        }

        return doBuild(condition);
    }

    protected abstract String doBuild(Condition condition);

    /**
     * 是否用于在 sphinxQL 中的 match 匹配中.
     *
     * @return 是 match 中的匹配条件,false 不是.
     */
    public boolean isMatch() {
        return match;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SphinxQLConditionQueryBuilder)) return false;
        SphinxQLConditionQueryBuilder that = (SphinxQLConditionQueryBuilder) o;
        return isMatch() == that.isMatch() &&
            fieldType == that.fieldType &&
            operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, operator, isMatch());
    }

    @Override
    public String toString() {
        return "SphinxQLConditionQueryBuilder{" +
            "fieldType=" + fieldType +
            ", operator=" + operator +
            ", match=" + match +
            ", storageStrategyFactory=" + storageStrategyFactory +
            '}';
    }
}
