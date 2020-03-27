package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
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
 * NotMatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class NotMatchConditionQueryBuilderTest {

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
            NotMatchConditionQueryBuilder builder = new NotMatchConditionQueryBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new Field(Long.MAX_VALUE, "test", FieldType.STRING), "test")
                ),
                FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "S = 'test'"
            ),
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new Field(Long.MAX_VALUE, "test", FieldType.LONG), 200L)
                ),
                FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "L = 200"
            ),
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new Field(Long.MAX_VALUE, "test", FieldType.DECIMAL), new BigDecimal("123.246"))
                ),
                FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "L0 = 123 AND " +
                    FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "L1 = 246"
            ),
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new Field(Long.MAX_VALUE, "test", FieldType.STRING), "test")
                ),
                FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "S != 'test'"
            ),
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.LONG),
                    ConditionOperator.GREATER_THAN,
                    new LongValue(new Field(Long.MAX_VALUE, "test", FieldType.LONG), 200L)
                ),
                FieldDefine.JSON_FIELDS + "." + Long.MAX_VALUE + "L > 200"
            ),
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.GREATER_THAN,
                    new LongValue(
                        new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        200L)
                ),
                "id > 200"
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
