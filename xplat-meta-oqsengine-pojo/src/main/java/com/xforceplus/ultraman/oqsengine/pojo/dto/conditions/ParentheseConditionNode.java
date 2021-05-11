package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.io.Serializable;

/**
 * 表示括号的结点,主要在中序迭代条件树时临时使用.
 * 不会实际出现在条件结点树中.
 *
 * @author dongbin
 * @version 0.1 2021/04/07 14:11
 * @since 1.8
 */
public class ParentheseConditionNode extends AbstractConditionNode implements Serializable {

    private static AbstractConditionNode LEFT = new ParentheseConditionNode(true);
    private static AbstractConditionNode RIGHT = new ParentheseConditionNode(false);

    /**
     * true 表示左括号, false表示右括号.
     */
    private boolean left;

    public static AbstractConditionNode buildLeft() {
        return LEFT;
    }

    public static AbstractConditionNode buildRight() {
        return RIGHT;
    }

    /**
     * 实例化.
     *
     * @param left true左手号,false右括号.
     */
    public ParentheseConditionNode(boolean left) {
        super(null, null);
        this.left = left;
        setRed(true);
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return !left;
    }

    @Override
    public String toString() {
        if (isLeft()) {
            return "(";
        } else {
            return ")";
        }
    }

    @Override
    public String toPrefixExpression() {
        return toString();
    }
}
