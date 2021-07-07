package com.xforceplus.ultraman.oqsengine.storage.query;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * AbstractConditionBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 01/12/2021
 * @since <pre>Jan 12, 2021</pre>
 */
public class ConditionBuilderAbstractTest {

    @Test
    public void testTypeEq() throws Exception {
        MockConditionBuilder builder = new MockConditionBuilder(ConditionOperator.EQUALS, FieldType.BOOLEAN);

        Assertions.assertEquals(ConditionOperator.EQUALS, builder.operator());

        Condition condition = new Condition(
            new EntityField(1, "code", FieldType.BOOLEAN),
            ConditionOperator.EQUALS,
            new BooleanValue(new EntityField(1, "code", FieldType.BOOLEAN), true)
        );

        Assertions.assertEquals("test", builder.build(condition));
    }

    @Test
    public void testTypeNotEq() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            MockConditionBuilder builder = new MockConditionBuilder(ConditionOperator.EQUALS, FieldType.STRING);

            Condition condition = new Condition(
                new EntityField(1, "code", FieldType.BOOLEAN),
                ConditionOperator.EQUALS,
                new BooleanValue(new EntityField(1, "code", FieldType.BOOLEAN), true)
            );

            builder.build(condition);
        });
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
