package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * MeqMatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MeqMatchConditionQueryBuilderTest {

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
            MeqMatchConditionQueryBuilder builder = new MeqMatchConditionQueryBuilder(
                storageStrategyFactory, c.condition.getField().type());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.STRING),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test0"),
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test1"),
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test2")
                ),
                "(=F11111S" + SphinxQLHelper.unicode("test0") + " | " +
                    "=F11111S" + SphinxQLHelper.unicode("test1") + " | " +
                    "=F11111S" + SphinxQLHelper.unicode("test2") + ")"
            )
            ,
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(new Field(11111, "test", FieldType.LONG), 1L),
                    new LongValue(new Field(11111, "test", FieldType.LONG), 2L),
                    new LongValue(new Field(11111, "test", FieldType.LONG), 3L)
                ),
                "(=F11111L1 | =F11111L2 | =F11111L3)"
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
