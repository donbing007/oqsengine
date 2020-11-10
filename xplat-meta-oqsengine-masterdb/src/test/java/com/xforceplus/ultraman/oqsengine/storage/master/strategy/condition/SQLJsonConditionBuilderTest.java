package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * SQLJsonConditionBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/05/2020
 * @since <pre>Nov 5, 2020</pre>
 */
public class SQLJsonConditionBuilderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testBuildCondition() throws Exception {

        buildCases().stream().forEach(c -> {
            SQLJsonConditionBuilder builder = new SQLJsonConditionBuilder(
                c.condition.getField().type(), c.condition.getOperator(), StorageStrategyFactory.getDefaultFactory());

            Assert.assertEquals(c.expectedSql, builder.build(c.condition));
        });
    }

    static class Case {
        private Condition condition;
        private String expectedSql;

        public Case(Condition condition, String expectedSql) {
            this.condition = condition;
            this.expectedSql = expectedSql;
        }
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(1, "test", FieldType.LONG), 200L)
                ),
                "attribute->>'$.F1L' = 200"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(new EntityField(1, "test", FieldType.LONG), 200L),
                    new LongValue(new EntityField(1, "test", FieldType.LONG), 300L)
                ),
                "attribute->>'$.F1L' IN (200,300)"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.STRING),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(new EntityField(1, "test", FieldType.LONG), "200L"),
                    new StringValue(new EntityField(1, "test", FieldType.LONG), "300L")
                ),
                "attribute->>'$.F1S' IN (\"200L\",\"300L\")"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.LONG),
                    ConditionOperator.LESS_THAN_EQUALS,
                    new LongValue(new EntityField(1, "test", FieldType.LONG), 200L)
                ),
                "attribute->>'$.F1L' <= 200"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.LONG),
                    ConditionOperator.GREATER_THAN_EQUALS,
                    new LongValue(new EntityField(1, "test", FieldType.LONG), 200L)
                ),
                "attribute->>'$.F1L' >= 200"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.STRING),
                    ConditionOperator.LIKE,
                    new StringValue(new EntityField(1, "test", FieldType.LONG), "200L")
                ),
                "attribute->>'$.F1S' LIKE \"%200L%\""
            )
        );
    }
} 
