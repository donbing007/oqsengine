package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.util.Objects;

/**
 * 表示一个符号操作结点.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 15:58
 * @since 1.8
 */
public class LinkConditionNode extends ConditionNode {

    /**
     * 条件连接方式
     */
    private ConditionLink link;

    /**
     * 构造方法
     *
     * @param l
     * @param r
     * @param link
     */
    public LinkConditionNode(ConditionNode l, ConditionNode r, ConditionLink link) {
        super(l, r);
        this.link = link;
    }

    /**
     * 获取连接方式
     *
     * @return
     */
    public ConditionLink getLink() {
        return link;
    }

    /**
     * toString方法
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        if (isClosed()) {
            buff.append("(");
        }

        buff.append(getLeft().toString())
            .append(" ")
            .append(link.toString())
            .append(" ")
            .append(getRight().toString());

        if (isClosed()) {
            buff.append(")");
        }

        return buff.toString();
    }

    @Override
    public String toPrefixExpression() {
        StringBuilder buff = new StringBuilder();
        if (isClosed()) {
            buff.append("(");
        }

        buff.append(link.toString())
            .append(isRed() ? "(r)" : "")
            .append(" ")
            .append(getLeft().toPrefixExpression())
            .append(" ")
            .append(getRight().toPrefixExpression());

        if (isClosed()) {
            buff.append(")");
        }

        return buff.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LinkConditionNode)) {
            return false;
        }

        LinkConditionNode other = (LinkConditionNode) o;
        if (link != other.link) {
            return false;
        }

        ConditionNode left = getLeft();
        ConditionNode otherLeft = other.getLeft();
        if ((left == null && otherLeft != null) || (left != null && otherLeft == null)) {
            return false;
        }

        if (!left.equals(otherLeft)) {
            return false;
        }
        ConditionNode right = getRight();
        ConditionNode othetRight = other.getRight();
        if ((right == null && othetRight != null) || (right != null && othetRight == null)) {
            return false;
        }

        if (!right.equals(othetRight)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLink());
    }

}
