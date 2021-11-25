package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;

/**
 * 表示一个对于字段附件查询的条件.
 *
 * @author dongbin
 * @version 0.1 2021/11/22 14:13
 * @since 1.8
 */
public class AttachmentCondition extends Condition {

    public AttachmentCondition(IEntityField field, boolean eq, String attachment) {
        super(field, eq ? ConditionOperator.EQUALS : ConditionOperator.NOT_EQUALS, new StringValue(field, attachment));
    }
}
