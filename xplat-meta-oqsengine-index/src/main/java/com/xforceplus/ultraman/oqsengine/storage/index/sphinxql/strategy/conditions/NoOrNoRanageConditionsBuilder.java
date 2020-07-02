package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;

import java.util.ArrayList;
import java.util.List;

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

        /**
         * issue #14
         * 在 match 结束后增加同样的条件,利用属性进行二次过滤保证结果正确.
         */
        List<Condition> secondaryFilterConditions = new ArrayList<>(conditions.size());
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

                    // issue #14
                    secondaryFilterConditions.add(condition);

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

        // issue #14
        if (!secondaryFilterConditions.isEmpty()) {
            String condtitonStr = buildSecondFilterConditions(secondaryFilterConditions);
            if (!condtitonStr.isEmpty()) {
                buff.append(" ")
                    .append(SqlKeywordDefine.AND)
                    .append(" ")
                    .append(condtitonStr);
            }
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

    // issue #14
    private String buildSecondFilterConditions(List<Condition> conditions) {
        StringBuilder buff = new StringBuilder();

        SphinxQLConditionBuilder conditionQueryBuilder;
        for (Condition condition : conditions) {
            if (isIgnoreSecondaryFiltering(condition)) {
                continue;
            }
            conditionQueryBuilder = conditionQueryBuilderFactory.getQueryBuilder(condition, false);
            if (buff.length() > 0) {
                buff.append(" ").append(SqlKeywordDefine.AND).append(" ");
            }

            buff.append(conditionQueryBuilder.build(condition));
        }

        return buff.toString();
    }

    private boolean isIgnoreSecondaryFiltering(Condition condition) {
        if (ConditionOperator.LIKE == condition.getOperator()) {
            return true;
        } else if (ConditionOperator.MULTIPLE_EQUALS == condition.getOperator()) {
            return true;
        } else if (FieldType.STRINGS == condition.getField().type()) {
            return true;
        } else if (FieldType.DECIMAL == condition.getField().type()) {
            return true;
        }
        return false;
    }
}
