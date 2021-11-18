package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import java.util.Collection;

/**
 * 对于右值为空的判断查询构造器.
 * 此实现会忽略所有的字段类型和操作类型.
 *
 * @author dongbin
 * @version 0.1 2021/11/18 17:52
 * @since 1.8
 */
public class NullQueryConditionBuilder extends AbstractSphinxQLConditionBuilder {


    public NullQueryConditionBuilder(FieldType fieldType, ConditionOperator operator) {
        super(fieldType, operator, false);
    }

    @Override
    protected String doBuild(Condition condition) {
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(fieldType());

        StringBuilder buff = new StringBuilder();

        Collection<String> shortNames = storageStrategy.toStorageNames(condition.getField(), true);
        buff.append(FieldDefine.ATTRIBUTE).append(".")
            .append(shortNames.stream().findFirst().get());

        switch (operator()) {
            case IS_NOT_NULL: {
                buff.append(" is not null");
                break;
            }
            case IS_NULL: {
                buff.append(" is null");
                break;
            }
            default: {
                throw new IllegalArgumentException(
                    String.format("Unsupported operation types. Only %s and %s are supported.",
                        ConditionOperator.IS_NOT_NULL.getSymbol(), ConditionOperator.IS_NULL.getSymbol()));
            }
        }

        return buff.toString();
    }
}
