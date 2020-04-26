package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * MatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MatchConditionBuilderTest {

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
            MatchConditionBuilder builder = new MatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator(), c.useGroupName);

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test")
                ),
                "(ZONESPAN:F11111S \"F11111S test\")"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(11111, "test", FieldType.LONG), 200L)
                ),
                "(ZONESPAN:F11111L \"F11111L 200\")"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new EntityField(11111, "test", FieldType.DECIMAL), new BigDecimal("123.246"))
                ),
                "((ZONESPAN:F11111L \"F11111L0 123\") (ZONESPAN:F11111L \"F11111L1 246\"))"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test")
                ),
                "-(ZONESPAN:F11111S \"F11111S test\")"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.STRING),
                    ConditionOperator.LIKE,
                    new StringValue(new EntityField(11111, "test", FieldType.STRING), "test")
                ),
                "(ZONESPAN:F11111S F11111S \"*test*\")"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.ENUM),
                    ConditionOperator.EQUALS,
                    new EnumValue(new EntityField(11111, "test", FieldType.ENUM), "test")
                ),
                "(ZONESPAN:F11111S \"F11111S test\")"
            ),
            new Case(
                new Condition(
                    new EntityField(11111, "test", FieldType.ENUM),
                    ConditionOperator.NOT_EQUALS,
                    new EnumValue(new EntityField(11111, "test", FieldType.ENUM), "test")
                ),
                "-(ZONESPAN:F11111S \"F11111S test\")"
            ),
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.DECIMAL),
                    ConditionOperator.NOT_EQUALS,
                    new DecimalValue(new EntityField(1, "test", FieldType.DECIMAL), BigDecimal.ZERO)
                ),
                "-((ZONESPAN:F1L \"F1L0 0\") (ZONESPAN:F1L \"F1L1 0\"))"
            ),
            // 多值只处理第一个值.
            new Case(
                new Condition(
                    new EntityField(1, "test", FieldType.STRINGS),
                    ConditionOperator.EQUALS,
                    new StringsValue(new EntityField(1, "test", FieldType.STRINGS), "v1"),
                    new StringsValue(new EntityField(1, "test", FieldType.STRINGS), "v2")
                ),
                "(ZONESPAN:F1S F1S* \"v1\")",
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