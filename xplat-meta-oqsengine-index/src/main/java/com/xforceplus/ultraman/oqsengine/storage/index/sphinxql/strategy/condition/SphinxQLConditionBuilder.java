package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Objects;

/**
 * SphinxQL 的条件构造器.
 *
 * @author dongbin
 * @version 0.1 2020/3/25 18:14
 * @since 1.8
 */
public abstract class SphinxQLConditionBuilder implements ConditionBuilder<String> {

    /**
     * 生成条件时是否使用物理值组名称.
     */
    private boolean useStorageGroupName;
    /**
     * 目标字段类型.
     */
    private FieldType fieldType;
    /**
     * 目标的条件操作符.
     */
    private ConditionOperator operator;
    /**
     * 是否使用全文索引匹配.
     */
    private boolean match;
    /**
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

    public SphinxQLConditionBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator) {
        this(storageStrategyFactory, fieldType, operator, false, false);
    }

    public SphinxQLConditionBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator, boolean match) {
        this(storageStrategyFactory, fieldType, operator, match, false);
    }

    public SphinxQLConditionBuilder(
        StorageStrategyFactory storageStrategyFactory,
        FieldType fieldType,
        ConditionOperator operator,
        boolean match,
        boolean useStorageGroupName) {
        this.fieldType = fieldType;
        this.operator = operator;
        this.match = match;
        this.storageStrategyFactory = storageStrategyFactory;
        this.useStorageGroupName = useStorageGroupName;
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

    /**
     * 是否使用物理值组名称匹配.
     *
     * @return true 使用组名称,false 不使用.
     */
    public boolean isUseStorageGroupName() {
        return useStorageGroupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SphinxQLConditionBuilder)) {
            return false;
        }
        SphinxQLConditionBuilder that = (SphinxQLConditionBuilder) o;
        return isUseStorageGroupName() == that.isUseStorageGroupName() &&
            isMatch() == that.isMatch() &&
            fieldType == that.fieldType &&
            operator == that.operator &&
            Objects.equals(getStorageStrategyFactory(), that.getStorageStrategyFactory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isUseStorageGroupName(), fieldType, operator, isMatch(), getStorageStrategyFactory());
    }

    @Override
    public String toString() {
        return "SphinxQLConditionQueryBuilder{" +
            "useStorageGroupName=" + useStorageGroupName +
            ", fieldType=" + fieldType +
            ", operator=" + operator +
            ", match=" + match +
            ", storageStrategyFactory=" + storageStrategyFactory +
            '}';
    }
}
