package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare.SphinxQLConditionCompareStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare.ConditionCompareStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

import java.util.Iterator;

/**
 * 所有连接符都是 and,但是比较符号出现了大于小于等.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:28
 * @since 1.8
 */
public class NoOrHaveRanageConditionsBuilder extends NoOrNoRanageConditionsBuilder {

    @Override
    public String build(Conditions conditions) {
        Iterator<ConditionNode> nodes = conditions.iterator();
        ConditionNode node = null;
        ValueConditionNode valueConditionNode = null;

        StringBuilder buff = new StringBuilder();

        Condition condition;
        IEntityField field;
        IValue logicValue;
        Conditions eqConditions = null;
        while (nodes.hasNext()) {
            node = nodes.next();

            if (Conditions.isValueNode(node)) {

                valueConditionNode = (ValueConditionNode) node;

                condition = valueConditionNode.getCondition();
                logicValue = condition.getValue();
                field = condition.getValue().getField();

                if (isRange(condition)) {

                    if (buff.length() != 0) {
                        buff.append(" and ");
                    }
                    SphinxQLConditionCompareStrategy compareStrategy = ConditionCompareStrategyFactory.getStrategy(field.type());
                    StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(field.type());
                    buff.append(compareStrategy.build(FieldDefine.JSON_FIELDS, condition, storageStrategy));

                } else {

                    if (eqConditions == null) {
                        eqConditions = new Conditions(condition);
                    } else {
                        eqConditions.addAnd(condition);
                    }

                }

            }
        }

        if (eqConditions != null) {
            buff.append(" and ").append(super.build(eqConditions));
        }
        return buff.toString();
    }

    private boolean isRange(Condition condition) {
        switch (condition.getOperator()) {
            case LIKE:
            case EQUALS:
            case NOT_EQUALS:
                return false;
            default:
                return true;
        }
    }

}
