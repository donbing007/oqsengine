package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

import java.util.Iterator;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNorRanageConditionsBuilder implements ConditionsBuilder<String> {

    // 没有 or 只有 and 不需要关注连接符.
    @Override
    public String build(Conditions conditions) {

        Iterator<ConditionNode> nodes = conditions.iterator();
        ConditionNode node = null;
        ValueConditionNode valueConditionNode = null;
        IValue conditionValue;

        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('@").append(FieldDefine.FULL_FIELDS).append(" ");
        while (nodes.hasNext()) {
            node = nodes.next();

            if (Conditions.isValueNode(node)) {

                valueConditionNode = (ValueConditionNode) node;

                switch (valueConditionNode.getCondition().getOperator()) {
                    case EQUALS: {
                        buff.append("=");
                        break;
                    }
                    case NOT_EQUALS: {
                        buff.append("-");
                    }
                }

                buff.append(SphinxQLHelper.serializeFull(valueConditionNode.getCondition().getValue()));
                buff.append(" ");

            }
        }

        //判断是否只有一个条件,并且是不等于时需要增加一个全局条件用以排除.类似如下.
        // -F123 F* 表示从所有字段中排除掉 F123.
        final int onlyOne = 1;
        if (conditions.size() == onlyOne && ConditionOperator.NOT_EQUALS == valueConditionNode.getCondition().getOperator()) {
            buff.append(SphinxQLHelper.FULL_FIELD_PREFIX).append("*");
        } else {
            // 去除最后的空格.
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("')");

        return buff.toString();
    }
}
