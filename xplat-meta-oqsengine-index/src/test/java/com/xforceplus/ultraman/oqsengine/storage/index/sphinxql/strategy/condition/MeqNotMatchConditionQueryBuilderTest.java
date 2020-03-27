package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
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
public class MeqNotMatchConditionQueryBuilderTest {

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
            MeqNotMatchConditionQueryBuilder builder = new MeqNotMatchConditionQueryBuilder(
                storageStrategyFactory, c.condition.getField().type());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(
                        new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        1L),
                    new LongValue(
                        new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        2L),
                    new LongValue(
                        new Field(Long.MAX_VALUE, "test", FieldType.LONG, FieldConfig.build().identifie(true)),
                        3L)
                ),
                "id IN (1,2,3)"
            ),
            new Case(
                new Condition(
                    new Field(1, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(
                        new Field(1, "test", FieldType.LONG),
                        1L),
                    new LongValue(
                        new Field(1, "test", FieldType.LONG),
                        2L),
                    new LongValue(
                        new Field(1, "test", FieldType.LONG),
                        3L)
                ),
                FieldDefine.JSON_FIELDS + ".1L IN (1,2,3)"
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
