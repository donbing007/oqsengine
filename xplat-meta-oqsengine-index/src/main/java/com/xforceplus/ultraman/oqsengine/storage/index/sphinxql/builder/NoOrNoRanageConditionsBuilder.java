package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Iterator;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNoRanageConditionsBuilder implements ConditionsBuilder<String>, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;

    // 没有 or 只有 and 不需要关注连接符.
    @Override
    public String build(Conditions conditions) {

        Iterator<ConditionNode> nodes = conditions.iterator();
        ConditionNode node = null;
        ValueConditionNode valueConditionNode = null;

        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('@").append(FieldDefine.FULL_FIELDS).append(" ");
        // 用以判断是否还没有条件,方便条件之间的空格.
        int emtpyLen = buff.length();

        // 判断是否都是不等于条件. true 全否定,false 有等于条件.
        boolean allNegative = true;
        IValue logicValue;
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        while (nodes.hasNext()) {
            node = nodes.next();

            if (Conditions.isValueNode(node)) {

                valueConditionNode = (ValueConditionNode) node;

                logicValue = valueConditionNode.getCondition().getValue();
                storageStrategy = this.storageStrategyFactory.getStrategy(logicValue.getField().type());
                storageValue = storageStrategy.toStorageValue(logicValue);

                while (storageValue != null) {

                    if (buff.length() > emtpyLen) {
                        buff.append(" ");
                    }


                    switch (valueConditionNode.getCondition().getOperator()) {
                        case EQUALS: {
                            buff.append("=");
                            allNegative = false;
                            break;
                        }
                        case NOT_EQUALS: {
                            buff.append("-");
                            break;
                        }
                        case LIKE: {
                            allNegative = false;
                        }
                    }

                    buff.append(SphinxQLHelper.encodeFullText(storageValue));

                    // 多值可能
                    storageValue = storageValue.next();

                }
            }
        }

        //判断是否都是不等于条件,是的话需要补充一下 F*.
        // -F123 F* 表示从所有字段中排除掉 F123.
        if (allNegative) {
            buff.append(" ").append(SphinxQLHelper.FULL_FIELD_PREFIX).append("*");
        }
        buff.append("')");

        return buff.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

}
