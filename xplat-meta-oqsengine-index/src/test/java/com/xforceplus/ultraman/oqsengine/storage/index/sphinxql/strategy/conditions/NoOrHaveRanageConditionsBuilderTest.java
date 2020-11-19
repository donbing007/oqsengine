package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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
 * NoOrHaveRanageConditionsBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/28/2020
 * @since <pre>Feb 28, 2020</pre>
 */
public class NoOrHaveRanageConditionsBuilderTest {

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test", Arrays.asList(longField));

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
            String where = builder.build(entityClass, c.conditions);
            Assert.assertEquals(c.expected, where);
        });

    }

    private List<Case> buildCase() {
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(1, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                FieldDefine.JSON_FIELDS + ".1L > 100 AND entity = 9223372036854775807"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(2, "c2", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(2, "c2", FieldType.STRING), "test")
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(1, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                FieldDefine.JSON_FIELDS + ".1L > 100 AND entity = 9223372036854775807 "
                    + SqlKeywordDefine.AND + " MATCH('(@" + FieldDefine.FULL_FIELDS + " \"*test*\")')"
            ),

            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(3, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(3, "c3", FieldType.DECIMAL), new BigDecimal("123.56789"))
                    )
                ),
                "((" + FieldDefine.JSON_FIELDS + ".3L0 > 123) OR (" + FieldDefine.JSON_FIELDS + ".3L0 = 123 AND " + FieldDefine.JSON_FIELDS + ".3L1 > 567890000000000000)) AND entity = 9223372036854775807"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(3, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(3, "c3", FieldType.DECIMAL), new BigDecimal("123.56789"))
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(2, "c2", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(2, "c2", FieldType.STRING), "test")
                    )
                ),
                "((" + FieldDefine.JSON_FIELDS + ".3L0 > 123) OR (" + FieldDefine.JSON_FIELDS + ".3L0 = 123 AND " + FieldDefine.JSON_FIELDS + ".3L1 > 567890000000000000))" +
                    " AND entity = 9223372036854775807 AND MATCH('(@" + FieldDefine.FULL_FIELDS + " \"*test*\")')"
            )
            ,
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(1, "c1", FieldType.STRING),
                            ConditionOperator.MULTIPLE_EQUALS,
                            new StringValue(new EntityField(1, "c1", FieldType.STRING), "v1"),
                            new StringValue(new EntityField(1, "c1", FieldType.STRING), "v2")
                        )
                    ).addAnd(
                    new Condition(
                        new EntityField(2, "c2", FieldType.STRING),
                        ConditionOperator.EQUALS,
                        new StringValue(new EntityField(2, "c2", FieldType.STRING), "v3")
                    )
                ),
                "entity = 9223372036854775807 AND jsonfields.2S = 'v3' AND MATCH('(@" + FieldDefine.FULL_FIELDS + " (\"v1F1S\" | \"v2F1S\") \"v3F2S\")')"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(3, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(3, "c3", FieldType.DECIMAL), new BigDecimal("123.56789"))
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(2, "c2", FieldType.STRING),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(2, "c2", FieldType.STRING), "test")
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(new EntityField(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 1L),
                        new LongValue(new EntityField(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 2L),
                        new LongValue(new EntityField(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 3L)
                    )
                ),
                "((" + FieldDefine.JSON_FIELDS + ".3L0 > 123) OR (" + FieldDefine.JSON_FIELDS + ".3L0 = 123 AND "
                    + FieldDefine.JSON_FIELDS + ".3L1 > 567890000000000000)) " + SqlKeywordDefine.AND + " entity = 9223372036854775807 AND id IN (1,2,3)" +
                    " AND MATCH('(@" + FieldDefine.FULL_FIELDS + " \"*test*\")')"
            )
            ,
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(1, "c1", FieldType.LONG),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                        ))
                    .addAnd(
                        new Condition(
                            new EntityField(2, "c2", FieldType.LONG),
                            ConditionOperator.NOT_EQUALS,
                            new LongValue(new EntityField(2, "c1", FieldType.LONG), 200L)
                        )),
                "jsonfields.1L > 100 AND jsonfields.2L != 200 AND MATCH('(@fullfields -\"200F2L\") (@entityf =\"9223372036854775807\")')"
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
