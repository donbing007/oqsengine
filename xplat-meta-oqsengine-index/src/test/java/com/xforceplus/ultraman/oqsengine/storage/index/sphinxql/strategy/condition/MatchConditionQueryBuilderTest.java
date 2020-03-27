package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
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
 * MatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MatchConditionQueryBuilderTest {

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
            MatchConditionQueryBuilder builder = new MatchConditionQueryBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator());

            Assert.assertEquals(c.expected, builder.build(c.condition));
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test")
                ),
                "=F11111S" + SphinxQLHelper.unicode("test")
            ),
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new Field(11111, "test", FieldType.LONG), 200L)
                ),
                "=F11111L200"
            ),
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new Field(11111, "test", FieldType.DECIMAL), new BigDecimal("123.246"))
                ),
                "=F11111L0123 =F11111L1246"
            ),
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test")
                ),
                "-F11111S" + SphinxQLHelper.unicode("test")
            ),
            new Case(
                new Condition(
                    new Field(11111, "test", FieldType.STRING),
                    ConditionOperator.LIKE,
                    new StringValue(new Field(11111, "test", FieldType.STRING), "test*")
                ),
                "F11111S" + SphinxQLHelper.unicode("test*")
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
