package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AbstractConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;

/**
 * 所有连接符都是 and,但是比较符号出现了大于小于等.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:28
 * @since 1.8
 */
public class NoOrHaveRanageConditionsBuilder extends NoOrNoRanageConditionsBuilder {

    @Override
    public SphinxQLWhere build(Conditions conditions, IEntityClass... entityClasses) {
        Conditions eqConditions = Conditions.buildEmtpyConditions();
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            linkNode -> {
                if (where.attrFilterSize() > 0) {
                    /*
                     * 检查下个节点是否为值结点同时为非range查询.
                     * 只有后继节点是range查询或者非值结点时才打印.
                     */
                    AbstractConditionNode rightNode = linkNode.getRight();
                    if (rightNode != null) {
                        if (Conditions.isLinkNode(rightNode)) {
                            // 连接结点
                            where.addAttrFilter(" ").addAttrFilter(SqlKeywordDefine.AND).addAttrFilter(" ");
                        } else if (Conditions.isValueNode(rightNode)) {
                            // 值结点,只有范围查询才输出连接符.
                            Condition condition = ((ValueConditionNode) rightNode).getCondition();
                            if (condition.isRange()) {
                                where.addAttrFilter(" ").addAttrFilter(SqlKeywordDefine.AND).addAttrFilter(" ");
                            }
                        }
                    }
                }
            },
            valueNode -> {

                Condition condition = valueNode.getCondition();
                if (condition.isRange()) {
                    AbstractSphinxQLConditionBuilder builder =
                        getConditionQueryBuilderFactory().getQueryBuilder(condition, false);
                    where.addAttrFilter(builder.build(condition));
                } else {
                    eqConditions.addAnd(condition);
                }

            },
            parentheseNode -> {
            }
        );

        if (!eqConditions.isEmtpy()) {
            where.addWhere(super.build(eqConditions, entityClasses), true);
        }

        return where;
    }
}
