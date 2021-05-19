package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

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
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.NoOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NoOrHaveRanageConditionsBuilder Tester.
 *
 * @author dongbin
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
     * Method: build(Conditions conditions).
     */
    @Test
    public void testBuild() throws Exception {

        NoOrHaveRanageConditionsBuilder builder = new NoOrHaveRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);
        builder.init();

        buildCase().stream().forEach(c -> {
            String where = builder.build(c.conditions, entityClass).toString();
            Assert.assertEquals(c.expected, where);
        });

    }

    private List<Case> buildCase() {
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 100L)))
                    .addAnd(new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING), "test"))),
                String.format("(id = 100) AND MATCH('((@%s -1y2p0ijtest32e8e6S))')", FieldDefine.ATTRIBUTEF)
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 100L))
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775806L, "c2", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 200L))
                ),
                "(id = 100 AND id = 200)"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 100L))),
                "(id = 100)"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG), 100L)
                    )
                ),
                "(" + FieldDefine.ATTRIBUTE + ".1y2p0ij32e8e7L > 100)"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(9223372036854775807L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                            "test")
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775806L, "c1", FieldType.LONG),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(new EntityField(9223372036854775806L, "c1", FieldType.LONG), 100L)
                    )
                ),
                String.format(
                    "(%s.1y2p0ij32e8e6L > 100) AND MATCH('((@%s 1y2p0ijtest32e8e7S))')",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTEF)
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                            new BigDecimal("123.56789"))
                    )
                ),
                String.format(
                    "(((%s.1y2p0ij32e8e7L0 > 123) OR (%s.1y2p0ij32e8e7L0 = 123 AND %s.1y2p0ij32e8e7L1 > 567890000000000000)))",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE)
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                            new BigDecimal("123.56789"))
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                            "test")
                    )
                ),
                String.format(
                    "(((%s.1y2p0ij32e8e7L0 > 123) OR (%s.1y2p0ij32e8e7L0 = 123 AND %s.1y2p0ij32e8e7L1 > 567890000000000000))) AND MATCH('((@%s 1y2p0ijtest32e8e6S))')",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTEF)
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(9223372036854775807L, "c1", FieldType.STRING),
                            ConditionOperator.MULTIPLE_EQUALS,
                            new StringValue(new EntityField(9223372036854775807L, "c1", FieldType.STRING), "v1"),
                            new StringValue(new EntityField(9223372036854775807L, "c1", FieldType.STRING), "v2")
                        )
                    )
                    .addAnd(
                        new Condition(
                            new EntityField(9223372036854775806L, "c2", FieldType.STRING),
                            ConditionOperator.EQUALS,
                            new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING), "v3")
                        )
                    ),
                String.format("MATCH('((@%s (1y2p0ijv132e8e7S | 1y2p0ijv232e8e7S)) (@%s 1y2p0ijv332e8e6S))')",
                    FieldDefine.ATTRIBUTEF, FieldDefine.ATTRIBUTEF)
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(new EntityField(9223372036854775807L, "c3", FieldType.DECIMAL),
                            new BigDecimal("123.56789"))
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING,
                                FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                            "test")
                    )
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775805L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(new EntityField(9223372036854775805L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 1L),
                        new LongValue(new EntityField(9223372036854775805L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 2L),
                        new LongValue(new EntityField(9223372036854775805L, "c1", FieldType.LONG,
                            FieldConfig.build().identifie(true)), 3L)
                    )
                ),
                String.format(
                    "(((%s.1y2p0ij32e8e7L0 > 123) OR (%s.1y2p0ij32e8e7L0 = 123 AND %s.1y2p0ij32e8e7L1 > 567890000000000000)) AND id IN (1,2,3)) AND MATCH('((@%s 1y2p0ijtest32e8e6S))')",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTEF
                )
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(9223372036854775807L, "c1", FieldType.LONG),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG), 100L)
                        ))
                    .addAnd(
                        new Condition(
                            new EntityField(9223372036854775806L, "c2", FieldType.LONG),
                            ConditionOperator.NOT_EQUALS,
                            new LongValue(new EntityField(9223372036854775806L, "c1", FieldType.LONG), 200L)
                        )),
                String.format("(%s.1y2p0ij32e8e7L > 100) AND MATCH('((@%s -1y2p0ij20032e8e6L))')",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTEF)
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
