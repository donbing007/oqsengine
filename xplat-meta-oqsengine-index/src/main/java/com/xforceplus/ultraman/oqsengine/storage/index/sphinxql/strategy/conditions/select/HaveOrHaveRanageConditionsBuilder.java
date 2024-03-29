package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.AbstractConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;

/**
 * 含有OR同时含有范围查询.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:29
 * @since 1.8
 */
public class HaveOrHaveRanageConditionsBuilder extends AbstractConditionsBuilder {

    @Override
    public SphinxQLWhere build(Conditions conditions, IEntityClass... entityClasses) {
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            linkNode -> {
                switch (linkNode.getLink()) {
                    case AND: {
                        where.addAttrFilter(" ").addAttrFilter(SqlKeywordDefine.AND).addAttrFilter(" ");
                        break;
                    }
                    case OR: {
                        where.addAttrFilter(" ").addAttrFilter(SqlKeywordDefine.OR).addAttrFilter(" ");
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected conditional operation symbol.");
                    }
                }
            },
            valueNode -> {

                Condition condition = valueNode.getCondition();

                /*
                 * 为了和 HaveOrNoRange 保持一致,所有含有OR的查询不能使用ID.
                 */
                if (condition.getField().config().isIdentifie()) {
                    throw new IllegalArgumentException("Cannot use primary key queries in queries containing OR.");
                }

                ConditionBuilder<Condition, String> builder =
                    getConditionQueryBuilderFactory().getQueryBuilder(condition, false);
                where.addAttrFilter(builder.build(condition));

            },
            parentheseNode -> where.addAttrFilter(parentheseNode.toString())
        );

        return where;
    }
}
