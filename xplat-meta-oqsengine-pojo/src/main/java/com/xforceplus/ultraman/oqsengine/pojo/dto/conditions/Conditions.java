package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 表示一系列条件组合.只支持以 And 方式进行组合.
 * OR 只有在没有封闭且只有字符串字段类型的条件才允许.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 13:26
 * @since 1.8
 */
public class Conditions implements Serializable {

    /**
     * 条件数量.
     */
    private int size;

    /**
     * 是否含有 or 连接符.
     */
    private boolean or;

    /**
     * 是否含有范围查询.
     */
    private boolean range;

    /**
     * 条件树根结点.
     */
    private ConditionNode head;

    private Conditions() {
        size = 0;
        range = false;
        or = false;
    }

    public static Conditions buildEmtpyConditions() {
        return new Conditions();
    }

    public Conditions(Condition condition) {
        head = new ValueConditionNode(condition);
        checkRange(condition);
        size = 1;
    }

    /**
     * 增加新的条件,可以设定和已有条件的连接符号和是否隔离.
     *
     * @param condition 新的条件.
     */
    public Conditions addAnd(Condition condition) {
        return doAdd(ConditionLink.AND, condition);
    }

    /**
     * 增加一组条件.
     *
     * @param conditions 条件组.
     * @param isolation  是否要封闭新的条件组.true 封闭,false 不封闭.
     */
    public Conditions addAnd(Conditions conditions, boolean isolation) {
        return doAdd(ConditionLink.AND, conditions, isolation);
    }

    /**
     * 以 OR 连接一个新的条件.
     * 只有当所有条件都是 string 并且没有封闭时才可以.
     *
     * @param condition 新条件.
     */
    public Conditions addOr(Condition condition) {

        return doAdd(ConditionLink.OR, condition);
    }

    /**
     * 以 OR 连接一组新的条件.
     *
     * @param conditions 新条件.
     * @param isolation  true 封闭新条件,false 不封闭.
     */
    public Conditions addOr(Conditions conditions, boolean isolation) {

        return doAdd(ConditionLink.OR, conditions, isolation);
    }

    public boolean haveOrLink() {
        return or;
    }

    public boolean haveRangeCondition() {
        return range;
    }

    /**
     * 条件迭代器.
     *
     * @return 迭代器.
     */
    public Iterator<ConditionNode> iterator() {
        return new ConditionIterator(head);
    }

    /**
     * 封闭已有条件.
     */
    public void insulate() {
        if (head != null) {
            head.setClosed(true);
        }
    }

    @Override
    public String toString() {
        return head.toString();
    }

    /**
     * 获取条件数量.
     *
     * @return 数量.
     */
    public int size() {
        return size;
    }

    /**
     * 是否条件值结点.
     *
     * @param node 目标结点.
     * @return true 是条件值结点,false 不是.
     */
    public static boolean isValueNode(ConditionNode node) {
        return node instanceof ValueConditionNode;
    }

    /**
     * 是否连接结点.
     *
     * @param node 目标结点.
     * @return true 是连接结点,false 不是.
     */
    public static boolean isLinkNode(ConditionNode node) {
        return node instanceof LinkConditionNode;
    }

    private Conditions doAdd(ConditionLink link, Condition condition) {

        size++;
        ConditionNode newValueNode = new ValueConditionNode(condition);
        ConditionNode newNode = new LinkConditionNode(head, newValueNode, link);
        head = newNode;

        if (link == ConditionLink.OR) {
            or = true;
        }

        checkRange(condition);

        return this;
    }

    // 判断是否含有范围查询符号.
    private void checkRange(Condition condition) {
        switch (condition.getOperator()) {
            case MINOR_THAN:
            case GREATER_THAN:
            case MINOR_THAN_EQUALS:
            case GREATER_THAN_EQUALS:
                range = true;
        }
    }

    private Conditions doAdd(ConditionLink link, Conditions conditions, boolean isolation) {

        size += conditions.size();
        if (isolation) {
            conditions.insulate();
        }
        ConditionNode newNode = new LinkConditionNode(head, conditions.head, link);
        head = newNode;

        // 如果本条件没有 OR 连接,那么查看新条件中是否有条件连接.
        if (!or) {
            or = conditions.haveOrLink();
        }

        if (!range) {
            range = conditions.haveRangeCondition();
        }

        return this;
    }

    // 条件迭代器.
    private static class ConditionIterator implements Iterator<ConditionNode> {

        private Deque<ConditionNode> stack = new LinkedList<>();

        public ConditionIterator(ConditionNode head) {
            init(head);
        }

        private void init(ConditionNode point) {
            if (point == null) {
                return;
            }

            if (Conditions.isValueNode(point)) {
                stack.push(point);
            } else {

                while (true) {

                    if (Conditions.isValueNode(point)) {
                        break;
                    } else {
                        stack.push(point);
                        point = point.getLeft();
                        stack.push(point);
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public ConditionNode next() {
            ConditionNode node = stack.pop();
            if (Conditions.isLinkNode(node)) {
                stack.push(node.getRight());
            }

            return node;
        }
    }
}
