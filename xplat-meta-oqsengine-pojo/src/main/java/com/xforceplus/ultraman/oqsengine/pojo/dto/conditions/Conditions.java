package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype.ConditionOperatorFieldValidationFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
     * 是否含有模糊查询.
     */
    private boolean fuzzy;

    /**
     * 条件树根结点.
     */
    private ConditionNode head;

    public static Conditions buildEmtpyConditions() {
        return new Conditions();
    }

    /**
     * 构造一个空条件.
     */
    private Conditions() {
        size = 0;
        range = false;
        or = false;
    }

    /**
     * 构造一个唯一条件.
     *
     * @param condition 新条件.
     */
    public Conditions(Condition condition) {
        validate(condition);
        head = new ValueConditionNode(condition);
        range = condition.isRange();
        fuzzy = condition.getOperator() == ConditionOperator.LIKE;
        size = 1;
    }

    /**
     * 使用结点树构造一个新的条件.
     *
     * @param head 条件树.
     */
    public Conditions(ConditionNode head) {
        this.head = head;
        Collection<Condition> conditionCollection = collectCondition();
        this.size = conditionCollection.size();
        this.range = conditionCollection.stream().mapToInt(c -> c.isRange() ? 1 : 0).sum() > 0 ? true : false;
        this.fuzzy = conditionCollection.stream()
            .mapToInt(c -> c.getOperator() == ConditionOperator.LIKE ? 1 : 0).sum() > 0 ? true : false;
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
     * @param close      是否要封闭新的条件组.true 封闭,false 不封闭.
     */
    public Conditions addAnd(Conditions conditions, boolean close) {
        return doAdd(ConditionLink.AND, conditions, close);
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
     * @param close      true 封闭新条件,false 不封闭.
     */
    public Conditions addOr(Conditions conditions, boolean close) {

        return doAdd(ConditionLink.OR, conditions, close);
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
     * 是否含有模糊查询条件.
     *
     * @return ture 有, false没有.
     */
    public boolean haveFuzzyCondition() {
        return fuzzy;
    }

    /**
     * 扫描所有条件结点,结点以左中右的顺序出现.并对每一个结点执行预定的动作.
     *
     * @param linkAction       连接结点处理.
     * @param valueAction      值结点处理.
     * @param parentheseAction 括号结点处理.
     */
    public void scan(
        Consumer<LinkConditionNode> linkAction,
        Consumer<ValueConditionNode> valueAction,
        Consumer<ParentheseConditionNode> parentheseAction) {
        iterTree(c -> true, c -> {
            if (Conditions.isLinkNode(c)) {
                linkAction.accept((LinkConditionNode) c);
            }
            if (Conditions.isValueNode(c)) {
                valueAction.accept((ValueConditionNode) c);
            }
            if (Conditions.isParentheseNode(c)) {
                parentheseAction.accept((ParentheseConditionNode) c);
            }
        }, false);
    }

    /**
     * 迭代
     *
     * @return 集合.
     */
    public Collection<ConditionNode> collect() {
        List<ConditionNode> nodes = new LinkedList<>();
        iterTree(c -> true, c -> nodes.add(c), false);
        return nodes;
    }

    /**
     * 只返回所有条件结点,忽略连接结点.
     *
     * @return 所有条件平面返回.
     */
    public Collection<Condition> collectCondition() {
        List<Condition> conditionList = new LinkedList<>();
        iterTree(c -> isValueNode(c), c -> conditionList.add(((ValueConditionNode) c).getCondition()), false);
        return conditionList;
    }

    /**
     * 查找符合条件的子树.
     * <p>
     * 假如这样的一个条件树.
     * and(red)               //1
     * c1        or(red)            //2
     * c2              and(green)        //3
     * c4                c5
     * <p>
     * 给出条件 c -> !c.isRed()
     * 表示所有红色结点的开始的子树
     * 那么将返回 //3 处的那个结点开始的树.
     *
     * @param predicate 断言.匹配需要的结点.
     * @param brake     是否匹配首个后当前结点后的所有不再匹配.
     * @return 收集结果.
     */
    public Collection<ConditionNode> collectSubTree(Predicate<? super ConditionNode> predicate, boolean brake) {
        List<ConditionNode> nodes = new ArrayList<>(size);
        iterTree(predicate, c -> nodes.add(c), brake);
        return nodes;
    }

    /**
     * 得到原始条件树.
     *
     * @return 条件树.
     */
    public ConditionNode collectConditionTree() {
        return head;
    }

    /**
     * 封闭已有条件.
     */
    public Conditions close() {
        if (head != null) {
            head.setClosed(true);
        }
        return this;
    }

    @Override
    public String toString() {
        return head.toString();
    }

    /**
     * 前辍表达式.
     *
     * @return 表达式.
     * @see ConditionNode
     */
    public String toPrefixExpression() {
        return head.toPrefixExpression();
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
     * 是否为一个括号结点.
     *
     * @param node 目标结点.
     * @return true 括号结点,false不是.
     */
    public static boolean isParentheseNode(ConditionNode node) {
        return node instanceof ParentheseConditionNode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conditions)) {
            return false;
        }
        Conditions that = (Conditions) o;
        boolean result = size == that.size &&
            or == that.or &&
            range == that.range;
        if (result) {
            if (head == null) {
                return that.head == null ? true : false;
            } else {
                return that.head == null ? false : Objects.equals(head, that.head);
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, or, range, head.toString());
    }

    /**
     * 实际增加条件处理.
     */
    private Conditions doAdd(ConditionLink link, Condition condition) {

        validate(condition);

        ConditionNode newValueNode = new ValueConditionNode(condition);
        doAddNode(newValueNode, link, false);
        size++;

        if (link == ConditionLink.OR) {
            or = true;
        }

        if (!range) {
            range = condition.isRange();
        }

        if (!fuzzy) {
            fuzzy = condition.getOperator() == ConditionOperator.LIKE;
        }

        return this;
    }

    private void validate(Condition condition) {
        ConditionValidation validation =
            ConditionOperatorFieldValidationFactory.getValidation(condition.getField().type());

        if (!validation.validate(condition)) {
            throw new IllegalArgumentException(String.format("Wrong conditions.[%s]", condition));
        }
    }

    private Conditions doAdd(ConditionLink link, Conditions conditions, boolean close) {

        doAddNode(conditions.head, link, close);

        if (ConditionLink.OR == link && size > 0) {

            or = true;

        } else if (!or) {

            or = conditions.haveOrLink();
        }

        if (!range) {
            range = conditions.haveRangeCondition();
        }

        if (!fuzzy) {
            fuzzy = conditions.haveFuzzyCondition();
        }

        size += conditions.size();
        return this;
    }

    /**
     * 增加新的结点.
     */
    private void doAddNode(ConditionNode newNode, ConditionLink link, boolean close) {
        if (size == 0) {
            head = newNode;
        } else {

            if (close) {
                if (isLinkNode(newNode)) {
                    newNode.setClosed(true);
                }
            }

            final int onlyOneCondition = 1;
            if (size == onlyOneCondition) {

                /**
                 * 目标为只有一个条件的树.
                 *    c1
                 *
                 *  and 方式增加新的条件c2
                 *
                 *     and
                 *  c1     c2
                 */
                ConditionNode newLinkNode = new LinkConditionNode(head, newNode, link);
                if (ConditionLink.OR == link) {
                    // 设置为红色结点,因为是 or.
                    newLinkNode.setRed(true);
                }
                head = newLinkNode;
            } else {

                /**
                 * 目标为这样的结构类型.
                 *       or
                 * c1        and
                 *       c2       c3
                 *
                 * 增加新的结点 and c4
                 *
                 *         or
                 *  c1         and
                 *        and        c4
                 *     c2      c3
                 * 追加在 or 下方.
                 */
                LinkConditionNode linkHead = (LinkConditionNode) head;
                if (!linkHead.isClosed() && linkHead.getLink() == ConditionLink.OR && ConditionLink.AND == link) {

                    ConditionNode newLinkNode =
                        new LinkConditionNode(linkHead.getRight(), newNode, ConditionLink.AND);
                    linkHead.setRight(newLinkNode);

                } else {

                    ConditionNode newLinkNode = new LinkConditionNode(head, newNode, link);
                    head = newLinkNode;

                }
            }
        }
    }

    /**
     * 迭代条件树.
     * brake true 时表示是否匹配某个结点就直接停止迭代之后的子结点退回上层结点选择另一个分支进行迭代.
     *          or
     *     c1          and
     *            c2        c3
     *  如果设置的条件为非or结点,那么第一个OR结点之后的结点不会再迭代,反之会继承迭代到 and c2 c3这个子树.
     *
     *  最终迭代的目的是保持这样一个顺序.
     *   左结点 当前结点  右结点. 以上述的树为例,最后迭代顺序如下.
     *   c1 or c2 and c3
     */
    private void iterTree(Predicate<? super ConditionNode> predicate, Consumer<? super ConditionNode> consumer, boolean brake) {
        if (head == null) {
            return;
        }
        Deque<ConditionNode> stack = new ArrayDeque<>(size());
        stack.push(head);
        ConditionNode node;
        while (!stack.isEmpty()) {
            node = stack.pop();

            if (Conditions.isLinkNode(node)) {
                LinkConditionNode linkNode = (LinkConditionNode) node;

                // 如果是影子结点,即表示已经迭代过了.直接处理.
                if (linkNode.isShadow()) {
                    if (predicate.test(linkNode)) {
                        consumer.accept(linkNode);
                        if (brake) {
                            break;
                        }
                    }
                } else {

                    if (predicate.test(linkNode) && brake) {
                        stack.push(linkNode.buildShadow());
                    } else {

                        // 封闭,先压入右括号.
                        if (linkNode.isClosed()) {
                            stack.push(ParentheseConditionNode.buildRight());
                        }

                        // 先压入右结点.
                        if (linkNode.hasRight()) {
                            stack.push(linkNode.getRight());
                        }
                        // 将自己的影子结点压入.
                        stack.push(linkNode.buildShadow());

                        // 压入左结点,左结点会被先处理.
                        if (linkNode.hasLeft()) {
                            stack.push(linkNode.getLeft());
                        }

                        if (linkNode.isClosed()) {
                            stack.push(ParentheseConditionNode.buildLeft());
                        }
                    }
                }
            } else {
                if (predicate.test(node)) {
                    consumer.accept(node);
                }
            }

        }
    }
}
