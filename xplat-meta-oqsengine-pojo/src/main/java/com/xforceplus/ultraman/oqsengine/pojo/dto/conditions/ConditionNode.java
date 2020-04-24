package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 表示一个条件结点,有可能是表示条件有可能表示条件中的连接符号.
 * 每一个结点都可以使用红或者绿来区分,默认为绿色.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 15:56
 * @since 1.8
 */
public abstract class ConditionNode {

    private ConditionNode left;
    private ConditionNode right;
    private boolean closed;
    private boolean red;

    /**
     * 构造条件结点.
     *
     * @param left  左条件.
     * @param right 右条件.
     */
    public ConditionNode(ConditionNode left, ConditionNode right) {
        this.left = left;
        this.right = right;
        this.closed = false;
        this.red = false;

        // 染色,如果子结点有红色结点就将本身设置为红色.
        setRed(findRed(left));
        if (!isRed()) {
            setRed(findRed(right));
        }
    }

    /**
     * 返回左条件结点.
     *
     * @return 左条件结点.
     */
    public ConditionNode getLeft() {
        return left;
    }

    /**
     * 右条件结点.
     *
     * @return 结点.
     */
    public ConditionNode getRight() {
        return right;
    }

    /**
     * 更新左结点.
     *
     * @param left 左结点.
     */
    protected void setLeft(ConditionNode left) {
        this.left = left;

        if ((!isRed())) {
            setRed(findRed(this.left));
        }
    }

    /**
     * 更新右结点.
     *
     * @param right 右结点.
     */
    protected void setRight(ConditionNode right) {
        this.right = right;
        if (!isRed()) {
            setRed(findRed(this.right));
        }
    }

    /**
     * 是否封闭.
     *
     * @return true 封闭.false 不封闭.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * 设置结点是否封闭.
     *
     * @param closed true 封闭,false 不封闭.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * 结点是否为红色.
     *
     * @return true 红色.false 绿色.
     */
    public boolean isRed() {
        return red;
    }

    /**
     * 设置结点是否为红色.
     *
     * @param red true 红色.false 绿色.
     */
    public void setRed(boolean red) {
        this.red = red;
    }

    @Override
    public abstract String toString();

    /**
     * 前辍表达式.例如
     * OR AND c1=1 c2=3 c3=4 表示这样的条件.
     * c1 = 1 and c2 = 3 or c3 = 4
     *
     * @return 前辍表达式.
     */
    public abstract String toPrefixExpression();

    // 检查是否含有红色结点.
    private boolean findRed(ConditionNode node) {
        if (node == null) {
            return false;
        }
        Deque<ConditionNode> stack = new ArrayDeque<>();
        stack.push(node);

        ConditionNode point;
        while (!stack.isEmpty()) {
            point = stack.pop();
            if (point.isRed()) {
                return true;
            } else {
                if (LinkConditionNode.class.isInstance(point)) {
                    if (point.getLeft() != null) {
                        stack.push(point.getLeft());
                    }
                    if (point.getRight() != null) {
                        stack.push(point.getRight());
                    }
                }
            }
        }

        return false;
    }


}
