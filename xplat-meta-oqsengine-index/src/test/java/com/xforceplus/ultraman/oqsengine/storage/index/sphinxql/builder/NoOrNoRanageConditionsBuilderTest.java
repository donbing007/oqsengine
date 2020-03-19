package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * NoOrNorRanageConditionsBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/22/2020
 * @since <pre>Feb 22, 2020</pre>
 */
public class NoOrNoRanageConditionsBuilderTest {

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
        NoOrNoRanageConditionsBuilder builder = new NoOrNoRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);

        buildCase().stream().forEach(c -> {
            String where = builder.build(c.conditions);
            Assert.assertEquals(c.expected, where);
        });
    }

    private List<Case> buildCase() {
        String expectPrefix = "MATCH('@" + FieldDefine.FULL_FIELDS + " ";
        String expectAfter = "')";
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                expectPrefix + "=F1L100" + expectAfter
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new Field(1, "c1", FieldType.STRING), "test*")
                    )
                ),
                expectPrefix + "F1Stest*" + expectAfter
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.EQUALS,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test"))),
                expectPrefix + "=F1L100 =F2Stest" + expectAfter
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test"))),
                expectPrefix + "-F1L100 -F2Stest =Sg" + expectAfter
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test"))),
                expectPrefix + "-F2Stest =Sg" + expectAfter + " " + SqlKeywordDefine.AND + " id = 100"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L))),
                "MATCH('@fullfields  =Sg') AND id = 100"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.DECIMAL),
                        ConditionOperator.EQUALS,
                        new DecimalValue(new Field(1, "c1", FieldType.DECIMAL),
                            new BigDecimal("123456.123456")
                        )
                    )
                ),
                expectPrefix + "=F1L0123456 =F1L1123456" + expectAfter
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
