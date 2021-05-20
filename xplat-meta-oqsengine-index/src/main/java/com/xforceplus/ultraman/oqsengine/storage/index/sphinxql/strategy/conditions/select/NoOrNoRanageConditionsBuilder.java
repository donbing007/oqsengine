package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.AbstractConditionsBuilder;

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
     * 针对不等于,这里直接排除了关键字.并没有在一个范围内排除.
     * 原因是须有调用者追加entityclass的条件.
     */
    @Override
    public SphinxQLWhere build(Conditions conditions, IEntityClass... entityClasses) {
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            link -> {
                where.addMatch(" ");
            },
            value -> {
                Condition condition = value.getCondition();
                AbstractSphinxQLConditionBuilder builder =
                    getConditionQueryBuilderFactory().getQueryBuilder(condition, true);
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
