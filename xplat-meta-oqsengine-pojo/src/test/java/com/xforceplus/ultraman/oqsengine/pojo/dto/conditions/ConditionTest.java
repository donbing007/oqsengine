package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 条件测试.
 */
public class ConditionTest {

    @Test
    public void testisNullQuery() throws Exception {
        Condition condition = new Condition(
            EntityField.ID_ENTITY_FIELD,
            ConditionOperator.IS_NOT_NULL,
            new LongValue(EntityField.ID_ENTITY_FIELD, 100L));

        Assertions.assertTrue(condition.isNullQuery());

        condition = new Condition(
            EntityField.ID_ENTITY_FIELD,
            ConditionOperator.IS_NULL,
            new LongValue(EntityField.ID_ENTITY_FIELD, 100L));

        Assertions.assertTrue(condition.isNullQuery());

        condition = new Condition(
            EntityField.ID_ENTITY_FIELD,
            ConditionOperator.EQUALS,
            new LongValue(EntityField.ID_ENTITY_FIELD, 100L));

        Assertions.assertFalse(condition.isNullQuery());
    }

}