package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.NotMatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NotMatchConditionQueryBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class NotMatchConditionBuilderTest {

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
     * Method: build(Condition condition).
     */
    @Test
    public void testBuild() throws Exception {

        buildCases().stream().forEach(c -> {
            NotMatchConditionBuilder builder = new NotMatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "S = 'test'"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 200L)
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "L = 200"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                        new BigDecimal("123.246"))
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "L0 = 123 AND "
                    + FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "L1 = 246000000000000000"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "S != 'test'"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.GREATER_THAN,
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 200L)
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "L > 200"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.GREATER_THAN,
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        200L)
                ),
                "id > 200"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(
                        new EntityField(9223372036854775807L, "test", FieldType.STRING),
                        "!@#$%^&*()300")
                ),
                FieldDefine.ATTRIBUTE + "." + "1y2p0ij32e8e7" + "S = '!@#$%^&*()300'"
            ),
            new Case(
                new Condition(
                    EntityField.CREATE_TIME_FILED,
                    ConditionOperator.EQUALS,
                    new LongValue(EntityField.CREATE_TIME_FILED, 1000L)
                ),
                FieldDefine.CREATE_TIME + " = 1000"
            ),
            new Case(
                new Condition(
                    EntityField.UPDATE_TIME_FILED,
                    ConditionOperator.EQUALS,
                    new LongValue(EntityField.UPDATE_TIME_FILED, 1000L)
                ),
                FieldDefine.UPDATE_TIME + " = 1000"
            )
        );
    }

    private static class Case {
        private Condition condition;
        private String expected;

        public Case(Condition condition, String expected) {
            this.condition = condition;
            this.expected = expected;
        }
    }

} 
