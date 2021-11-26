package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.AbstractSphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 对于附件的查询条件生成器.
 *
 * @author dongbin
 * @version 0.1 2021/11/23 15:01
 * @since 1.8
 */
public class AttachmentConditionBuilder extends AbstractSphinxQLConditionBuilder {

    /**
     * 构造一个附件的条件构造器.
     *
     * @param operator 操作符.
     */
    public AttachmentConditionBuilder(ConditionOperator operator) {
        super(FieldType.STRING, operator, true);

        switch (operator) {
            case EQUALS:
            case NOT_EQUALS: {
                break;
            }
            default: {
                throw new IllegalArgumentException(
                    "The attachment query condition constructor can only handle equals or not equals.");
            }
        }
    }

    @Override
    public String build(Condition condition) {
        if (!AttachmentCondition.class.isInstance(condition)) {
            throw new IllegalArgumentException(
                "The attachment condition constructor expects an attachment query condition.");
        }

        IValue logicValue = condition.getFirstValue();
        if (!StringValue.class.isInstance(logicValue)) {
            throw new IllegalArgumentException("Attachment query criteria can only accept string values.");
        }
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(FieldType.STRING);
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        return SphinxQLHelper.buildAttachemntQuery(storageValue);
    }

    @Override
    protected String doBuild(Condition condition) {
        return null;
    }

    @Override
    public FieldType fieldType() {
        return FieldType.STRING;
    }
}
