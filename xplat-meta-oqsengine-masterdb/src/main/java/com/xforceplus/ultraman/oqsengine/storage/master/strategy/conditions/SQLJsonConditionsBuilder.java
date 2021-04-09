package com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.SQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;

/**
 * 基于json的搜索构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:49
 * @since 1.8
 */
public class SQLJsonConditionsBuilder implements ConditionsBuilder<String>, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private SQLConditionQueryBuilderFactory sqlConditionQueryBuilderFactory;

    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {
        if (conditions.isEmtpy()) {
            return "";
        }

        StringBuilder sql = new StringBuilder();
        conditions.scan(
            link -> sql.append(" ").append(link.getLink().name()).append(" "),
            value -> {
                Condition condition = value.getCondition();
                ConditionBuilder<String> cb = sqlConditionQueryBuilderFactory.getQueryBuilder(condition);
                sql.append(cb.build(condition));
            },
            parenthese -> sql.append(parenthese.toString())
        );

        return sql.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;

        this.sqlConditionQueryBuilderFactory = new SQLConditionQueryBuilderFactory(this.storageStrategyFactory);
    }

    private void build(StringBuilder buff, ConditionNode node) {
        if (Conditions.isLinkNode(node)) {
            LinkConditionNode linkNode = (LinkConditionNode) node;
            if (linkNode.isClosed()) {
                buff.append("(");
            }

            build(buff, linkNode.getLeft());
            buff.append(" ").append(linkNode.getLink().name());
            if (linkNode.getRight() != null) {
                buff.append(" ");
                build(buff, linkNode.getRight());
            }

            if (linkNode.isClosed()) {
                buff.append(")");
            }
        } else {
            ValueConditionNode vNode = (ValueConditionNode) node;
            Condition condition = vNode.getCondition();
            ConditionBuilder<String> cb = sqlConditionQueryBuilderFactory.getQueryBuilder(condition);
            buff.append(cb.build(condition));
        }
    }
}
