package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.util.Objects;

/**
 * 表示不同条件的连接符号.
 * 其可以产生多个影子结点,影子结点并不是真实的连接结点其只在迭代时占位使用.
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
    private boolean shadow;
    /**
     * 只有当shadow为true时,此值才有意义.
     */
    private LinkConditionNode actual;

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
        this.shadow = false;
    }

    /**
     * 构造一个影子结点.
     *
     * @return 影子结点.
     */
    public LinkConditionNode buildShadow() {
        if (!shadow) {
            LinkConditionNode shadowNode = new LinkConditionNode(getLeft(), getRight(), getLink());
            shadowNode.shadow = true;
            shadowNode.actual = this;
            shadowNode.setRed(this.isRed());
            return shadowNode;
        } else {
            return this;
        }
    }

    /**
     * 如果是实际类型,那么将返回自己.
     * 否则返回真实的实体.
     *
     * @return 实体.
     */
    public LinkConditionNode getActual() {
        if (isShadow()) {
            return actual;
        } else {
            return this;
        }
    }

    /**
     * 是否为一个连接结点影子.
     *
     * @return true 是,false不是.
     */
    public boolean isShadow() {
        return shadow;
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
