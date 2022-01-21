package com.xforceplus.ultraman.oqsengine.pojo.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionLink;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.LinkConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ParentheseConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 内存中查詢.
 */
public class MemQuery {

    /**
     * 查詢.
     *
     * @param entities   需要過濾數據
     * @param conditions 條件
     * @return 過濾完的數據
     */
    public static Collection<IEntity> query(Collection<IEntity> entities, Conditions conditions) {
        PredicateHolder holder = new PredicateHolder();
        conditions.scan(holder::accept, holder::accept, holder::accept);

        Predicate<IEntity> predicate = holder.getPredicate();

        return Optional.ofNullable(entities)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(predicate).collect(Collectors.toList());
    }

    /**
     * visitor 來構建predicate.
     */
    static class PredicateHolder {

        private Deque<Object> stack = new LinkedList<>();

        void accept(LinkConditionNode linkConditionNode) {
            stack.push(linkConditionNode.getLink());
        }

        void accept(ValueConditionNode valueConditionNode) {
            Predicate<Entity> next = toPredicate(valueConditionNode);
            if (!stack.isEmpty()) {
                Object peek = stack.peek();
                if (peek != null) {
                    if (peek instanceof ConditionLink) {
                        ConditionLink linkNode = (ConditionLink) peek;
                        stack.pop();
                        Object nextPeek = stack.peek();
                        if (nextPeek instanceof Predicate) {
                            stack.pop();
                            if (linkNode.equals(ConditionLink.AND)) {
                                Predicate<Entity> and = ((Predicate<Entity>) nextPeek).and(next);
                                next = and;
                            } else if (linkNode.equals(ConditionLink.OR)) {
                                Predicate<Entity> or = ((Predicate<Entity>) nextPeek).or(next);
                                next = or;
                            }
                        }
                    }
                }
            }
            stack.push(next);
        }

        void accept(ParentheseConditionNode parentheseConditionNode) {
            if (parentheseConditionNode.isLeft()) {
                stack.push(parentheseConditionNode);
            } else if (parentheseConditionNode.isRight()) {
                Predicate<Entity> unWrapper = null;
                while (stack.size() > 0) {
                    Object pop = stack.pop();
                    if (pop instanceof ParentheseConditionNode) {
                        break;
                    } else if (pop instanceof Predicate) {
                        unWrapper = (Predicate<Entity>) pop;
                    }
                }

                if (unWrapper != null) {
                    stack.push(unWrapper);
                }
            }
        }

        private Predicate<Entity> toPredicate(ValueConditionNode node) {
            Condition condition = node.getCondition();
            IEntityField field = condition.getField();
            ConditionOperator operator = condition.getOperator();
            Predicate<Entity> predicate = operator.getPredicate(field, condition.getValues());
            return predicate;
        }

        /**
         * get predicate.
         *
         * @return 生成出来的Predicate
         */
        public Predicate<IEntity> getPredicate() {
            while (stack.size() > 1) {
                Object expectedPredicate = stack.pop();

                Predicate<Entity> next = null;
                if (expectedPredicate instanceof Predicate) {

                    if (stack.isEmpty()) {
                        return (Predicate<IEntity>) expectedPredicate;
                    }

                    Object expectedLinkOrNull = stack.peek();
                    if (expectedLinkOrNull instanceof ConditionLink) {
                        Object link = stack.pop();
                        if (!stack.isEmpty()) {
                            Object expectedPredicateNext = stack.peek();
                            if (expectedPredicateNext instanceof Predicate) {
                                stack.pop();
                                if (link.equals(ConditionLink.AND)) {
                                    Predicate<Entity> and = ((Predicate<Entity>) expectedPredicate).and(
                                        (Predicate<? super Entity>) expectedPredicateNext);
                                    next = and;
                                } else if (link.equals(ConditionLink.OR)) {
                                    Predicate<Entity> or = ((Predicate<Entity>) expectedPredicate).or(
                                        (Predicate<? super Entity>) expectedPredicateNext);
                                    next = or;
                                }
                            }
                        }
                    } else {
                        throw new RuntimeException("Syntax error");
                    }
                } else {
                    throw new RuntimeException("Syntax error");
                }
                if (next != null) {
                    stack.push(next);
                }
            }

            return (Predicate<IEntity>) stack.pop();
        }
    }

}
