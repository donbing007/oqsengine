package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.JointMask;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
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

    /**
     * 没有 or 只有 and 不需要关注连接符.
     */
    @Override
    public String build(Conditions conditions) {

        StringBuilder idBuff = new StringBuilder();
        StringBuilder attribute = new StringBuilder();
        attribute.append("MATCH('@").append(FieldDefine.FULL_FIELDS).append(" ");
        // 用以判断是否还没有条件,方便条件之间的空格.
        int emtpyLen = attribute.length();
        boolean allNegative = true;

        for (ConditionNode node : conditions.collection()) {
            if (Conditions.isValueNode(node)) {
                Condition condition = ((ValueConditionNode) node).getCondition();

                IValue logicValue = condition.getValue();
                StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
                StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

                if (condition.getField().config().isIdentifie()) {

                    // 主键查询.
                    idBuff.append("id ");
                    switch(condition.getOperator()) {
                        case EQUALS:
                        case NOT_EQUALS: {
                            idBuff.append(condition.getOperator().getSymbol());
                            break;
                        }
                        default:
                            throw new IllegalStateException("Id search can only use eq or not_eq.");
                    }
                        idBuff.append(" ").append(storageValue.value());

                } else {

                    while (storageValue != null) {
                        if (attribute.length() > emtpyLen) {
                            attribute.append(" ");
                        }

                        switch (condition.getOperator()) {
                            case EQUALS: {
                                attribute.append("=");
                                allNegative = false;
                                break;
                            }
                            case NOT_EQUALS: {
                                attribute.append("-");
                                break;
                            }
                            case LIKE: {
                                allNegative = false;
                            }
                        }

                        attribute.append(SphinxQLHelper.encodeFullText(storageValue));


                        // 多值可能
                        storageValue = storageValue.next();

                    }
                }
            }
        }

        //判断是否都是不等于条件,是的话需要补充所有字段才能成立排除.
        // -F123 =Sg 表示从所有字段中排除掉 F123.
        if (allNegative) {
            attribute.append(" =").append(SphinxQLHelper.ALL_DATA_FULL_TEXT);
        }
        attribute.append("')");

        if (idBuff.length() > 0) {
            attribute.append(" ").append(JointMask.AND).append(" ").append(idBuff.toString());
        }

        return attribute.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

}
