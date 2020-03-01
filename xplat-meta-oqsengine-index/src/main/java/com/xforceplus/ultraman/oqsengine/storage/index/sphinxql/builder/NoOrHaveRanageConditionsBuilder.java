package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.helper.StorageTypeHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;

import java.util.Iterator;

/**
 * 所有连接符都是 and,但是比较符号出现了大于小于等.
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
        IValue value;
        Conditions preciseConditions = null;
        while (nodes.hasNext()) {
            node = nodes.next();

            if (Conditions.isValueNode(node)) {

                valueConditionNode = (ValueConditionNode) node;

                condition = valueConditionNode.getCondition();
                value = condition.getValue();

                if (isRange(condition)) {

                    if (buff.length() != 0) {
                        buff.append(" and ");
                    }
                    buff.append(FieldDefine.JSON_FIELDS).append(".")
                        .append(condition.getField().id())
                        .append(" ")
                        .append(condition.getOperator().getSymbol())
                        .append(" ");
                    field = condition.getValue().getField();
                    StorageType storageType = StorageTypeHelper.findStorageType(field.type());
                    switch(storageType) {
                        case STRING: {
                            buff.append("\'").append(value.valueToString()).append("\'");
                            break;
                        }
                        case LONG: {
                            buff.append(value.valueToLong());
                        }
                    }

                } else {

                    if (preciseConditions == null) {
                        preciseConditions = new Conditions(condition);
                    } else {
                        preciseConditions.addAnd(condition);
                    }

                }

            }
        }

        if (preciseConditions != null) {
            buff.append(" and ").append(super.build(preciseConditions));
        }
        return buff.toString();
    }

    private boolean isRange(Condition condition) {
        switch(condition.getOperator()) {
            case LIKE:
            case EQUALS:
            case NOT_EQUALS:
                return false;
            default:
                return true;
        }
    }
}
