package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * MeqNotMatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
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
     * Method: build(Condition condition)
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
                        new EntityField(9223372036854775807L, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        1L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        2L),
                    new LongValue(
                        new EntityField(9223372036854775807L, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
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
