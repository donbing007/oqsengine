package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare.ConditionCompareStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare.SphinxQLConditionCompareStrategy;

/**
 * 所有连接符都是 and,但是比较符号出现了大于小于等.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:28
 * @since 1.8
 */
public class NoOrHaveRanageConditionsBuilder extends NoOrNoRanageConditionsBuilder {

    @Override
    public String build(Conditions conditions) {

        StringBuilder buff = new StringBuilder();

        // 非范围的,将交由父类处理.
        Conditions eqConditions = Conditions.buildEmtpyConditions();
        conditions.collection().stream().forEach(cn -> {

            if (Conditions.isValueNode(cn)) {
                Condition condition = ((ValueConditionNode) cn).getCondition();

                if (condition.isRange()) {
                    if (buff.length() != 0) {
                        buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
                    }
                    SphinxQLConditionCompareStrategy compareStrategy =
                        ConditionCompareStrategyFactory.getStrategy(condition.getField().type());
                    buff.append(compareStrategy.build(FieldDefine.JSON_FIELDS, condition, getStorageStrategyFactory()));
                } else {
                    eqConditions.addAnd(condition);
                }
            }
        });

        if (!eqConditions.isEmtpy()) {
            buff.append(" ").append(SqlKeywordDefine.AND).append(" ").append(super.build(eqConditions));
        }
        return buff.toString();
    }
}
