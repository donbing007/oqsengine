package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import io.vavr.Tuple2;

/**
 * in 查询的全文方式.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 14:33
 * @since 1.8
 */
public class MeqMatchConditionBuilder extends AbstractSphinxQLConditionBuilder {

    public MeqMatchConditionBuilder(FieldType fieldType, boolean useGroupName) {
        super(fieldType, ConditionOperator.MULTIPLE_EQUALS, true, useGroupName);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue[] values = condition.getValues();
        StringBuilder buff = new StringBuilder("(");
        int emptyLen = buff.length();

        Tuple2<String, Boolean> res;
        for (IValue v : values) {

            /*
            跳过可能的空值.
            注意: 如果所有值都为空值那么应该触发Condition的校验异常.
                 所以执行到这里必定有一个不符合如下条件.
             */
            if (v.getValue() == null || v.getValue().toString().isEmpty()) {
                continue;
            }

            StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(v.getField().type());
            StorageValue storageValue = storageStrategy.toStorageValue(v);

            res = SphinxQLHelper.buildPreciseQuery(storageValue, condition.getField().type(), isUseStorageGroupName());

            if (buff.length() > emptyLen) {
                buff.append(" | ");
            }

            buff.append(res._1);
        }
        buff.append(")");

        return buff.toString();
    }
}
