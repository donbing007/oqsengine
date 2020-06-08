package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * MeqMatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MeqMatchConditionBuilderTest {

    private StorageStrategyFactory storageStrategyFactory;

    @Before
    public void before() throws Exception {
        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(Condition condition)
     */
    @Test
    public void testBuild() throws Exception {

        buildCases().stream().forEach(c -> {
            MeqMatchConditionBuilder builder = new MeqMatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.useGroupName);

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.STRING),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test0"),
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test1"),
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test2")
                ),
                "(\"test0F11111S\" | \"test1F11111S\" | \"test2F11111S\")"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(new EntityField(11111, "test", FieldType.LONG), 1L),
                    new LongValue(new EntityField(11111, "test", FieldType.LONG), 2L),
                    new LongValue(new EntityField(11111, "test", FieldType.LONG), 3L)
                ),
                "(\"1F11111L\" | \"2F11111L\" | \"3F11111L\")"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.ENUM),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new EnumValue(new EntityField(11111, "test", FieldType.ENUM), "one")
                ),
                "(\"oneF11111S\")"
            )
            ,
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.STRINGS),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(new EntityField(1, "test", FieldType.STRINGS), "one"),
                    new StringsValue(new EntityField(1, "test", FieldType.STRINGS), "two")
                ),
                "(\"oneF1S*\" | \"twoF1S*\")",
                true
            )
        );
    }

    private static class Case {
        private Condition condition;
        private String expected;
        private boolean useGroupName;

        public Case(Condition condition, String expected) {
            this(condition, expected, false);
        }

        public Case(Condition condition, String expected, boolean useGroupName) {
            this.condition = condition;
            this.expected = expected;
            this.useGroupName = useGroupName;
        }
    }

} 
