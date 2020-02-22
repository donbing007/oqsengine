package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 表示一个条件结点,有可能是表示条件有可能表示条件中的连接符号.
 * @author dongbin
 * @version 0.1 2020/2/20 15:56
 * @since 1.8
 */
public abstract class ConditionNode {

    private ConditionNode left;
    private ConditionNode right;
    private boolean closed;

    public ConditionNode(ConditionNode left, ConditionNode right) {
        this.left = left;
        this.right = right;
    }

    public ConditionNode getLeft() {
        return left;
    }

    public ConditionNode getRight() {
        return right;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
