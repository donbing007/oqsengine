package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.query.MemQuery;
import java.util.Arrays;
import org.junit.Test;

/**
 * 条件测试.
 */
public class ConditionsTest {

    @Test
    public void testConditions() {


        EntityField name = new EntityField(1, "name", FieldType.STRING);

        Conditions innerConditions = Conditions
            .buildEmtpyConditions()
            .addAnd(new Condition(name, ConditionOperator.EQUALS, new StringValue(name, "abc2")));

        Conditions conditions = Conditions
            .buildEmtpyConditions()
            .addAnd(new Condition(name, ConditionOperator.EQUALS, new StringValue(name, "abc")))
            .addAnd(new Condition(name, ConditionOperator.EQUALS, new StringValue(name, "abc2")))
            .addOr(innerConditions, true);

        IValue value1 = new StringValue(name, "abc");
        IValue value2 = new StringValue(name, "abc2");

        Entity entity = Entity.Builder.anEntity()
            .withValue(value1)
            .build();

        Entity entity2 = Entity.Builder.anEntity()
            .withValue(value2)
            .build();

        System.out.println(MemQuery.query(Arrays.asList(entity, entity2), conditions));
    }
}
