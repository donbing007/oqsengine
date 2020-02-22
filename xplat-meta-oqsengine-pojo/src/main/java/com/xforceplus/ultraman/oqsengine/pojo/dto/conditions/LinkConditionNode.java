package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 表示一个符号操作结点.
 * @author dongbin
 * @version 0.1 2020/2/20 15:58
 * @since 1.8
 */
public class LinkConditionNode extends ConditionNode {

    private ConditionLink link;

    public LinkConditionNode(ConditionNode l, ConditionNode r, ConditionLink link) {
        super(l, r);
        this.link = link;
    }

    public ConditionLink getLink() {
        return link;
    }

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
}
