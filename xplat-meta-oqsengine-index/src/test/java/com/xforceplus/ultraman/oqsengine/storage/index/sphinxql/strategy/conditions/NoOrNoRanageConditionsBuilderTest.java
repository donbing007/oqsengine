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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
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
        NoOrNoRanageConditionsBuilder builder = new NoOrNoRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);
        builder.init();


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
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG), 100L)
                    )
                ),
                String.format("MATCH('(@%s 1y2p0ij10032e8e7L) (@%s =\"%d\")')",
                    FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.STRING,
                            FieldConfig.Builder.aFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()),
                        ConditionOperator.LIKE,
                        new StringValue(new EntityField(9223372036854775807L, "c1", FieldType.STRING,
                            FieldConfig.Builder.aFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()), "test")
                    )
                ),
                String.format("MATCH('(@%s 1y2p0ijtest32e8e7S) (@%s =\"%d\")')",
                    FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING),
                        ConditionOperator.EQUALS,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING), "test"))),
                String.format("MATCH('(@%s 1y2p0ij10032e8e7L 1y2p0ijtest32e8e6S) (@%s =\"%d\")')",
                    FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING), "test"))),
                String.format("MATCH('(@%s -1y2p0ij10032e8e7L -1y2p0ijtest32e8e6S) (@%s =\"%d\")')"
                    , FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 100L)))
                    .addAnd(new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.STRING),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(new EntityField(9223372036854775806L, "c2", FieldType.STRING), "test"))),
                String.format("id = 100 AND MATCH('(@%s -1y2p0ijtest32e8e6S) (@%s =\"%d\")')"
                    , FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 100L))),
                "id = 100"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775807L, "c1", FieldType.LONG, FieldConfig.build().identifie(true)), 100L))
                ).addAnd(
                    new Condition(
                        new EntityField(9223372036854775806L, "c2", FieldType.LONG, FieldConfig.build().identifie(true)),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(9223372036854775806L, "c2", FieldType.LONG, FieldConfig.build().identifie(true)), 200L))
                ),
                "id = 100 AND id = 200"
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.DECIMAL),
                        ConditionOperator.EQUALS,
                        new DecimalValue(new EntityField(9223372036854775807L, "c1", FieldType.DECIMAL),
                            new BigDecimal("123456.123456")
                        )
                    )
                ),
                String.format("MATCH('(@%s (1y2p0ij12345632e8e7L0 1y2p0ij12345600000000000032e8e7L1)) (@%s =\"%d\")')"
                    , FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            )
            ,
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.STRINGS),
                        ConditionOperator.EQUALS,
                        new StringsValue(new EntityField(9223372036854775807L, "c1", FieldType.STRINGS), "v1")
                    )),
                String.format("MATCH('(@%s 1y2p0ijv132e8e7S*) (@%s =\"%d\")')"
                    , FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            )
            ,
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        new EntityField(9223372036854775807L, "c1", FieldType.STRINGS),
                        ConditionOperator.NOT_EQUALS,
                        new StringsValue(new EntityField(9223372036854775807L, "c1", FieldType.STRINGS), "v1")
                    )),
                String.format("MATCH('(@%s -1y2p0ijv132e8e7S*) (@%s =\"%d\")')"
                    , FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
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
