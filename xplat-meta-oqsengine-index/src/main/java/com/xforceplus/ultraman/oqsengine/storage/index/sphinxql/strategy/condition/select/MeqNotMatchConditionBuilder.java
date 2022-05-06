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
import java.util.Arrays;

/**
 * in 非全文.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 14:42
 * @since 1.8
 */
public class MeqNotMatchConditionBuilder extends AbstractSphinxQLConditionBuilder {

    public MeqNotMatchConditionBuilder(FieldType fieldType) {
        super(fieldType, ConditionOperator.MULTIPLE_EQUALS, false);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue firstValue = condition.getFirstValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(condition.getField().type());
        StorageValue storageValue = storageStrategy.toStorageValue(firstValue);
        StringBuilder buff = new StringBuilder();

        FieldConfig fieldConfig = condition.getField().config();
        if (fieldConfig.isIdentifie()) {

            buff.append(FieldDefine.ID);

        } else {

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
                    // nothing.
                }
            }
        }

        if (StorageType.STRING == storageStrategy.storageType()) {
            /*
            字符串由于manticore不支持 in 过滤,所以这里选择使用OR连接.
             */
            buff.append("(");

            // 首个
            buff.append(FieldDefine.ATTRIBUTE).append(".").append(storageValue.shortStorageName().toString())
                .append(" = ")
                .append(buildConditionValue(storageValue, storageStrategy));

            Arrays.stream(condition.getValues()).skip(1).map(v -> storageStrategy.toStorageValue(v)).forEach(s -> {
                buff.append(" ")
                    .append(SqlKeywordDefine.OR)
                    .append(" ")
                    .append(FieldDefine.ATTRIBUTE).append(".").append(storageValue.shortStorageName().toString())
                    .append(" = ").append(buildConditionValue(s, storageStrategy));
            });

            buff.append(")");

        } else {
            buff.append(FieldDefine.ATTRIBUTE).append(".").append(storageValue.shortStorageName().toString());

            buff.append(" IN (");
            buff.append(buildConditionValue(storageValue, storageStrategy));

            Arrays.stream(condition.getValues()).skip(1).map(v -> storageStrategy.toStorageValue(v)).forEach(s -> {
                buff.append(",").append(buildConditionValue(s, storageStrategy));
            });

            buff.append(")");
        }

        return buff.toString();
    }

    private String buildConditionValue(StorageValue storageValue, StorageStrategy storageStrategy) {
        String conditionValue;
        if (storageStrategy.storageType() == StorageType.STRING) {
            conditionValue = "'" + SphinxQLHelper.encodeJsonCharset((String) storageValue.value()) + "'";
        } else {
            conditionValue = storageValue.value().toString();
        }
        return conditionValue;
    }
}
