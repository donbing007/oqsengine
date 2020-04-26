package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNoRanageConditionsBuilder implements ConditionsBuilder<String>, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionQueryBuilderFactory conditionQueryBuilderFactory;

    /**
     * 没有 or 只有 and 不需要关注连接符.
     */
    @Override
    public String build(Conditions conditions) {

        StringBuilder idBuff = new StringBuilder();
        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('@").append(FieldDefine.FULL_FIELDS).append(" ");
        // 用以判断是否还没有条件,方便条件之间的空格.
        int idEmptyLen = idBuff.length();
        int emtpyLen = buff.length();
        boolean allNegative = true;
        SphinxQLConditionBuilder conditionQueryBuilder;

        for (ConditionNode node : conditions.collect()) {
            if (Conditions.isValueNode(node)) {
                Condition condition = ((ValueConditionNode) node).getCondition();

                if (condition.getField().config().isIdentifie()) {
                    conditionQueryBuilder = conditionQueryBuilderFactory.getQueryBuilder(condition, false);

                    if (idBuff.length() > idEmptyLen) {
                        idBuff.append(" ").append(SqlKeywordDefine.AND).append(" ");
                    }
                    idBuff.append(conditionQueryBuilder.build(condition));

                } else {
                    conditionQueryBuilder = conditionQueryBuilderFactory.getQueryBuilder(condition, true);
                    if (buff.length() > emtpyLen) {
                        buff.append(" ");
                    }
                    buff.append(conditionQueryBuilder.build(condition));
                    switch (condition.getOperator()) {
                        case EQUALS:
                        case MULTIPLE_EQUALS:
                        case LIKE: {
                            allNegative = false;
                            break;
                        }
                        case NOT_EQUALS: {
                            break;
                        }
                    }
                }


            }
        }

        //判断是否都是不等于条件,是的话需要补充所有字段才能成立排除.
        // -F123 =Sg 表示从所有字段中排除掉 F123.
        if (allNegative) {
            buff.append(" =").append(SphinxQLHelper.ALL_DATA_FULL_TEXT);
        }
        buff.append("')");

        if (idBuff.length() > 0) {
            buff.append(" ").append(SqlKeywordDefine.AND).append(" ").append(idBuff.toString());
        }

        return buff.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;

        this.conditionQueryBuilderFactory = new SphinxQLConditionQueryBuilderFactory(this.storageStrategyFactory);
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

    public SphinxQLConditionQueryBuilderFactory getConditionQueryBuilderFactory() {
        return conditionQueryBuilderFactory;
    }
}
