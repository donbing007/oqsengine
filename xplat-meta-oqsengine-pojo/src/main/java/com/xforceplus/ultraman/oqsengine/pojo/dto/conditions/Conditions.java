package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype.ConditionOperatorFieldValidationFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        validate(condition);
        head = new ValueConditionNode(condition);
        range = condition.isRange();
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

    /**
     * 是否有 or 连接符.
     *
     * @return true 有,false 没有.
     */
    public boolean haveOrLink() {
        return or;
    }

    /**
     * 是否有范围条件.
     *
     * @return true 有范围条件.,false 没有.
     */
    public boolean haveRangeCondition() {
        return range;
    }

    /**
     * 返回顺序的集合.
     * c1 = 1 and c2 =2 or c3 = 3
     * 将会如下顺序
     * c1=1 -> and -> c2=2 -> or -> c3=3.
     *
     * @return 集合.
     */
    public Collection<ConditionNode> collection() {
        List<ConditionNode> nodes = new ArrayList(size);
        load(head, nodes, c -> true);
        return nodes;
    }

    /**
     * 只返回所有条件结点,忽略连接结点.
     *
     * @return 所有条件平面返回.
     */
    public Collection<Condition> collectionCondition() {
        List<ConditionNode> nodes = new ArrayList(size);
        load(head, nodes, c -> Conditions.isValueNode(c));
        return nodes.stream().map(n -> ((ValueConditionNode) n).getCondition()).collect(Collectors.toList());
    }

    private void load(ConditionNode point, List<ConditionNode> nodes, Predicate<? super ConditionNode> predicate) {
        if (Conditions.isValueNode(point)) {

            if (predicate.test(point)) {
                nodes.add(point);
            }

        } else {

            LinkConditionNode link = (LinkConditionNode) point;
            load(link.getLeft(), nodes, predicate);
            if (predicate.test(point)) {
                nodes.add(point);
            }
            load(link.getRight(), nodes, predicate);
        }
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
     * 判断是否为空,没有任何条件.
     *
     * @return true 为空,false 非空.
     */
    public boolean isEmtpy() {
        return size == 0;
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

    /**
     * 实际增加条件处理.
     */
    private Conditions doAdd(ConditionLink link, Condition condition) {

        validate(condition);

        ConditionNode newValueNode = new ValueConditionNode(condition);
        if (size == 0) {
            head = newValueNode;

        } else {

            ConditionNode newLinkNode = new LinkConditionNode(head, newValueNode, link);
            head = newLinkNode;

        }
        size++;

        if (link == ConditionLink.OR) {
            or = true;
        }

        if (!range) {
            range = condition.isRange();
        }

        return this;
    }

    private void validate(Condition condition) {
        ConditionValidation validation =
            ConditionOperatorFieldValidationFactory.getValidation(condition.getField().type());

        if (!validation.validate(condition)) {
            throw new IllegalArgumentException(String.format("Wrong conditions.[%s]", condition.toString()));
        }
    }

    private Conditions doAdd(ConditionLink link, Conditions conditions, boolean isolation) {

        if (size == 0) {

            head = conditions.head;

        } else {

            if (isolation) {
                conditions.insulate();
            }

            ConditionNode newNode = new LinkConditionNode(head, conditions.head, link);
            head = newNode;

        }

        if (ConditionLink.OR == link) {
            or = true;
        } else if (!or) {

            or = conditions.haveOrLink();
        }

        if (!range) {
            range = conditions.haveRangeCondition();
        }

        size += conditions.size();
        return this;
    }
}
