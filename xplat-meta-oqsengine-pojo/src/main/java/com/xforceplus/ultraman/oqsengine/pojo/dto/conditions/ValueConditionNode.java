package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 表示实际条件结点.
 * @author dongbin
 * @version 0.1 2020/2/20 16:08
 * @since 1.8
 */
public class ValueConditionNode extends ConditionNode {

    private Condition condition;

    public ValueConditionNode(Condition condition) {
        super(null, null);
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        if (isClosed()) {

            return "(" + condition.toString() + ")";

        } else {
            return condition.toString();
        }
    }
}
