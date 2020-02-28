package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.*;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Arrays;
import java.util.Collection;

/**
 * DefaultSphinxQLQueryOptimizer Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/28/2020
 * @since <pre>Feb 28, 2020</pre>
 */
public class DefaultSphinxQLQueryOptimizerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: optimizeConditions(Conditions conditions)
     */
    @Test
    public void testOptimizeConditions() throws Exception {
        DefaultSphinxQLQueryOptimizer optimizer = new DefaultSphinxQLQueryOptimizer();
        buildCases().stream().forEach(c -> {
            ConditionsBuilder builder = optimizer.optimizeConditions(c.conditions);
            Assert.assertEquals(c.expectedClass, builder.getClass());
        });

    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                Conditions.buildEmtpyConditions(),
                EmptyConditionsBuilder.class
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "test", FieldType.BOOLEAN),
                        ConditionOperator.EQUALS,
                        new BooleanValue(new Field(1, "test", FieldType.BOOLEAN), true)
                    )
                ),
                NoOrNoRanageConditionsBuilder.class
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "test", FieldType.BOOLEAN),
                        ConditionOperator.MINOR_THAN,
                        new BooleanValue(new Field(1, "test", FieldType.BOOLEAN), true)
                    )
                ),
                NoOrHaveRanageConditionsBuilder.class
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "test", FieldType.BOOLEAN),
                        ConditionOperator.EQUALS,
                        new BooleanValue(new Field(1, "test", FieldType.BOOLEAN), true)
                    )
                ).addAnd(
                    new Condition(
                        new Field(2, "test2", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(2, "test", FieldType.LONG), 100L)
                    )
                ),
                NoOrNoRanageConditionsBuilder.class
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "test", FieldType.BOOLEAN),
                        ConditionOperator.EQUALS,
                        new BooleanValue(new Field(1, "test", FieldType.BOOLEAN), true)
                    )
                ).addOr(
                    new Condition(
                        new Field(2, "test2", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(2, "test", FieldType.LONG), 100L)
                    )
                ),
                HaveOrNoRanageConditionsBuilder.class
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "test", FieldType.BOOLEAN),
                        ConditionOperator.GREATER_THAN,
                        new BooleanValue(new Field(1, "test", FieldType.BOOLEAN), true)
                    )
                ).addOr(
                    new Condition(
                        new Field(2, "test2", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(2, "test", FieldType.LONG), 100L)
                    )
                ),
                HaveOrHaveRanageConditionsBuilder.class
            )
        );
    }

    private static class Case {
        private Conditions conditions;
        private Class expectedClass;

        public Case(Conditions conditions, Class expectedClass) {
            this.conditions = conditions;
            this.expectedClass = expectedClass;
        }

    }

} 
