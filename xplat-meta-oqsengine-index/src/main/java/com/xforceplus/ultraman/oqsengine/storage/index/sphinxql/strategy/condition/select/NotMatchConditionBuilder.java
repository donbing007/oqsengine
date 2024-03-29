package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 不用于 match 函数中的单值查询构造器.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 10:07
 * @since 1.8
 */
public class NotMatchConditionBuilder extends AbstractSphinxQLConditionBuilder {

    public NotMatchConditionBuilder(FieldType fieldType, ConditionOperator operator) {
        super(fieldType, operator, false);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue logicValue = condition.getFirstValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(logicValue.getField().type());
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        StringBuilder buff = new StringBuilder();

        while (storageValue != null) {
            if (buff.length() > 0) {
                buff.append(" ")
                    .append(SqlKeywordDefine.AND)
                    .append(" ");
            }
            if (logicValue.getField().config().isIdentifie()) {
                buff.append(FieldDefine.ID);

            } else {

                FieldConfig fieldConfig = logicValue.getField().config();
                switch (fieldConfig.getFieldSense()) {
                    case CREATE_TIME: {
                        buff.append(FieldDefine.CREATE_TIME);
                        break;
                    }
                    case UPDATE_TIME: {
                        buff.append(FieldDefine.UPDATE_TIME);
                        break;
                    }
                    default: {
                        buff.append(FieldDefine.ATTRIBUTE).append(".")
                            .append(storageValue.shortStorageName().toString());
                    }
                }
            }
            buff.append(" ").append(this.operator().getSymbol());

            if (storageValue.type() == StorageType.STRING) {
                buff.append(" '");
                buff.append(SphinxQLHelper.encodeJsonCharset((String) storageValue.value()));
            } else {
                buff.append(" ");
                buff.append(storageValue.value());
            }

            if (storageValue.type() == StorageType.STRING) {
                buff.append("'");
            }

            storageValue = storageValue.next();
        }


        return buff.toString();
    }
}
