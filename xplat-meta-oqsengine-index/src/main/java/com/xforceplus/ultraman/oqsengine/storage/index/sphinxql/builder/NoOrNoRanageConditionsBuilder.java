package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

import java.util.Iterator;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNoRanageConditionsBuilder implements ConditionsBuilder<String> {

    // 没有 or 只有 and 不需要关注连接符.
    @Override
    public String build(Conditions conditions) {

        Iterator<ConditionNode> nodes = conditions.iterator();
        ConditionNode node = null;
        ValueConditionNode valueConditionNode = null;

        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('@").append(FieldDefine.FULL_FIELDS).append(" ");

        // 判断是否都是不等于条件. true 全否定,false 有等于条件.
        boolean allNegative = true;
        while (nodes.hasNext()) {
            node = nodes.next();

            if (Conditions.isValueNode(node)) {

                valueConditionNode = (ValueConditionNode) node;

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

                buff.append(SphinxQLHelper.serializeFull(valueConditionNode.getCondition().getValue()));
                buff.append(" ");

            }
        }

        //判断是否都是不等于条件,是的话需要补充一下 F*.
        // -F123 F* 表示从所有字段中排除掉 F123.
        if (allNegative) {
            buff.append(SphinxQLHelper.FULL_FIELD_PREFIX).append("*");
        } else {
            // 去除最后的空格.
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("')");

        return buff.toString();
    }
}
