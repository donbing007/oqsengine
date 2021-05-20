package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.AbstractConditionsBuilder;

/**
 * 含有OR但是没有范围查询.
 * 最终会尽量会都使用 SphinxQL中的match来达成查询目标.
 *
 * @author dongbin
 * @version 0.1 2021/4/7 13:53:29
 * @since 1.8
 */
public class HaveOrNoRanageConditionsBuilder extends AbstractConditionsBuilder {

    // 目标元信息索引.
    final int onlyOneEntityClassIndex = 0;

    @Override
    public SphinxQLWhere build(Conditions conditions, IEntityClass... entityClasses) {
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            link -> {
                switch (link.getLink()) {
                    case AND: {
                        where.addMatch(" ");
                        break;
                    }
                    case OR: {
                        where.addMatch(" ").addMatch("|").addMatch(" ");
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected conditional operation symbol.");
                    }
                }
            },
            value -> {
                Condition condition = value.getCondition();

                /*
                 * 含有OR的查询,不能使用id查询.
                 */
                if (condition.getField().config().isIdentifie()) {
                    throw new IllegalArgumentException("Cannot use primary key queries in queries containing OR.");
                }

                AbstractSphinxQLConditionBuilder builder =
                    getConditionQueryBuilderFactory().getQueryBuilder(condition, true);

                if (condition.getOperator() == ConditionOperator.NOT_EQUALS) {

                    where.addMatch(processNotEquals(entityClasses[onlyOneEntityClassIndex], builder.build(condition)));

                } else {
                    where.addMatch("(@")
                        .addMatch(FieldDefine.ATTRIBUTEF)
                        .addMatch(" ")
                        .addMatch(builder.build(value.getCondition()))
                        .addMatch(")");
                }
            },
            parenthese -> where.addMatch(parenthese.toString())
        );

        return where;
    }

    /**
     * 不等于需要特殊处理,在含有OR中不能直接排除需要在一个范围内排除.
     * 1. ( key0 | -key1)   错误的,在OR连接中不能直接使用排除.
     * 2. ( key0 | (key3 -key1) 正确,排除只能在一个已有范围内排除.
     * 这里会将 -key1 转换成 (entityclass -key1).
     */
    private String processNotEquals(IEntityClass entityClass, String source) {
        StringBuilder buff = new StringBuilder();
        buff.append("(@").append(FieldDefine.ENTITYCLASSF).append(" =").append(entityClass.id()).append(' ')
            .append("@").append(FieldDefine.ATTRIBUTEF).append(' ').append(source)
            .append(')');
        return buff.toString();
    }
}
