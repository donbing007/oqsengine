package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import java.util.Arrays;

/**
 * 处理搜索.
 * 可以跨元信息进行字段查询.
 * 只支持 LIKE,和EQUALS两种操作符.
 *
 * @author dongbin
 * @version 0.1 2021/05/17 17:54
 * @since 1.8
 */
public class CrossSearchConditionsBuilder extends AbstractConditionsBuilder {

    /**
     * 只会读取第一个条件.
     * 并且只处理 EQUALS, NOT_EQUALS, LIKE 操作符.
     *
     * @param conditions    查询条件.
     * @param entityClasses 元信息列表.
     * @return com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere 查询条件.
     * @author dongbin
     */
    @Override
    public SphinxQLWhere build(Conditions conditions, IEntityClass... entityClasses) {
        SphinxQLWhere where = new SphinxQLWhere();
        conditions.scan(
            link -> {
            },
            value -> {
                Condition condition = value.getCondition();
                switch (condition.getOperator()) {
                    case LIKE:
                    case EQUALS:
                    case NOT_EQUALS: {

                        AbstractSphinxQLConditionBuilder
                            builder = getConditionQueryBuilderFactory().getQueryBuilder(condition, true);


                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(
                            String.format("Only the [%s] operator can be used.", Arrays.toString(
                                new String[] {
                                    ConditionOperator.EQUALS.getSymbol(),
                                    ConditionOperator.NOT_EQUALS.getSymbol(),
                                    ConditionOperator.LIKE.getSymbol()
                                }
                            ))
                        );
                    }
                }
            },
            parenthese -> {
            }
        );

        return where;
    }
}
