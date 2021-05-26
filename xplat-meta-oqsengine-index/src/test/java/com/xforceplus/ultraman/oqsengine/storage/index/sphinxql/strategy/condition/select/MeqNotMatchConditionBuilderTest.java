package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.MeqNotMatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MeqNotMatchConditionQueryBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MeqNotMatchConditionBuilderTest {

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
            MeqNotMatchConditionBuilder builder = new MeqNotMatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        1L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        2L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG,
                            FieldConfig.build().identifie(true)),
                        3L)
                ),
                "id IN (1,2,3)"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG),
                        1L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG),
                        2L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG),
                        3L)
                ),
                FieldDefine.ATTRIBUTE + ".1y2p0ij32e8e7L IN (1,2,3)"
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(
                        new EntityField(9223372036854775807L, "test", FieldType.STRING),
                        "!@#$%^&*()300")
                ),
                FieldDefine.ATTRIBUTE + ".1y2p0ij32e8e7S IN ('!@#$%^&*()300')"
            ),
            new Case(
                new Condition(
                    EntityField.CREATE_TIME_FILED,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(EntityField.CREATE_TIME_FILED, 300L),
                    new LongValue(EntityField.CREATE_TIME_FILED, 200L)
                ),
                FieldDefine.CREATE_TIME + " IN (300,200)"
            ),
            new Case(
                new Condition(
                    EntityField.UPDATE_TIME_FILED,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(EntityField.UPDATE_TIME_FILED, 300L),
                    new LongValue(EntityField.UPDATE_TIME_FILED, 200L)
                ),
                FieldDefine.UPDATE_TIME + " IN (300,200)"
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