package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * AbstractConditionBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 01/12/2021
 * @since <pre>Jan 12, 2021</pre>
 */
public class AbstractConditionBuilderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testTypeEq() throws Exception {
        MockConditionBuilder builder = new MockConditionBuilder(ConditionOperator.EQUALS, FieldType.BOOLEAN);

        Assert.assertEquals(ConditionOperator.EQUALS, builder.operator());

        Condition condition = new Condition(
            new EntityField(1, "code", FieldType.BOOLEAN),
            ConditionOperator.EQUALS,
            new BooleanValue(new EntityField(1, "code", FieldType.BOOLEAN), true)
        );

        Assert.assertEquals("test", builder.build(condition));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypeNotEq() throws Exception {
        MockConditionBuilder builder = new MockConditionBuilder(ConditionOperator.EQUALS, FieldType.STRING);

        Condition condition = new Condition(
            new EntityField(1, "code", FieldType.BOOLEAN),
            ConditionOperator.EQUALS,
            new BooleanValue(new EntityField(1, "code", FieldType.BOOLEAN), true)
        );

        builder.build(condition);

    }

    static class MockConditionBuilder extends AbstractConditionBuilder<String> {

        private FieldType fieldType;

        public MockConditionBuilder(ConditionOperator operator, FieldType fieldType) {
            super(operator);
            this.fieldType = fieldType;
        }

        @Override
        public String doBuild(Condition condition) {
            return "test";
        }

        @Override
        public FieldType fieldType() {
            return fieldType;
        }
    }

} 
