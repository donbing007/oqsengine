package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * NoOrHaveRanageConditionsBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/28/2020
 * @since <pre>Feb 28, 2020</pre>
 */
public class NoOrHaveRanageConditionsBuilderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(Conditions conditions)
     */
    @Test
    public void testBuild() throws Exception {

        NoOrHaveRanageConditionsBuilder builder = new NoOrHaveRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);

        buildCase().stream().forEach(c -> {
            String where = builder.build(c.conditions);
            Assert.assertEquals(c.expected, where);
        });

    }

    private List<Case> buildCase() {
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                FieldDefine.JSON_FIELDS + ".1L > 100"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test*")
                    )
                ).addAnd(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                FieldDefine.JSON_FIELDS + ".1L > 100 and MATCH('@" + FieldDefine.FULL_FIELDS + " F2Stest*')"
            ),

            new Case(
                new Conditions(
                    new Condition(
                        new Field(3, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new Field(3, "c3", FieldType.DECIMAL), new BigDecimal("123.56789"))
                    )
                ),
                FieldDefine.JSON_FIELDS + ".3L0 >= 123 and " + FieldDefine.JSON_FIELDS + ".3L1 > 56789"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new Field(3, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new Field(3, "c3", FieldType.DECIMAL), new BigDecimal("123.56789"))
                    )
                ).addAnd(
                    new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test*")
                    )
                ),
                FieldDefine.JSON_FIELDS + ".3L0 >= 123 and " +
                    FieldDefine.JSON_FIELDS + ".3L1 > 56789 and " +
                    "MATCH('@" + FieldDefine.FULL_FIELDS + " F2Stest*')"
            )
        );
    }

    static class Case {
        private Conditions conditions;
        private String expected;

        public Case(Conditions conditions, String expected) {
            this.conditions = conditions;
            this.expected = expected;
        }
    }

} 
