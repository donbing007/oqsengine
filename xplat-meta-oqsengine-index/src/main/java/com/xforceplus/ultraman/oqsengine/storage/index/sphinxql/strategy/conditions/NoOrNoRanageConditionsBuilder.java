package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNoRanageConditionsBuilder extends AbstractConditionsBuilder {

    /**
     * 没有 or 只有 and 不需要关注连接符.
     */
    @Override
    public SphinxQLWhere build(IEntityClass entityClass, Conditions conditions) {
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            link -> {
                where.addMatch(" ");
            },
            value -> {
                Condition condition = value.getCondition();
                SphinxQLConditionBuilder builder = getConditionQueryBuilderFactory().getQueryBuilder(condition, true);
                where.addMatch("(@")
                    .addMatch(FieldDefine.ATTRIBUTEF)
                    .addMatch(" ")
                    .addMatch(builder.build(value.getCondition()))
                    .addMatch(")");
            },
            parenthese -> {

            }
        );

        return where;
    }
}
