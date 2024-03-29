package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.io.Serializable;
import java.util.Objects;

/**
 * 表示实际条件结点.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 16:08
 * @since 1.8
 */
public class ValueConditionNode extends AbstractConditionNode implements Serializable {

    /*
     * 条件设置
     */
    private Condition condition;

    /**
     * 构造方法.
     *
     * @param condition 条件.
     */
    public ValueConditionNode(Condition condition) {
        super(null, null);
        this.condition = condition;
    }

    /**
     * 获取条件信息.
     *
     * @return 条件.
     */
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

    @Override
    public String toPrefixExpression() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValueConditionNode)) {
            return false;
        }

        ValueConditionNode that = (ValueConditionNode) o;
        return Objects.equals(getCondition(), that.getCondition());
    }
}
