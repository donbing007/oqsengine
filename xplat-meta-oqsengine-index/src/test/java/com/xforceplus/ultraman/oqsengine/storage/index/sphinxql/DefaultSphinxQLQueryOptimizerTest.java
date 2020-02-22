package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.HaveOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.HaveOrNoRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.NoOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.NoOrNorRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer.DefaultSphinxQLQueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * SphinxQLQueryOptimizer Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/22/2020
 * @since <pre>Feb 22, 2020</pre>
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

        // no or no ranage
        Conditions conditions = new Conditions(
            new Condition(
                new Field(1, "c1", FieldType.LONG),
                ConditionOperator.EQUALS,
                new LongValue(new Field(1, "c1", FieldType.LONG), 100L)));

        ConditionsBuilder builder = optimizer.optimizeConditions(conditions);
        Assert.assertEquals(NoOrNorRanageConditionsBuilder.class, builder.getClass());

        // no or have ranage
        conditions = new Conditions(
            new Condition(
                new Field(1, "c1", FieldType.LONG),
                ConditionOperator.GREATER_THAN,
                new LongValue(new Field(1, "c1", FieldType.LONG), 100L)));
        builder = optimizer.optimizeConditions(conditions);
        Assert.assertEquals(NoOrHaveRanageConditionsBuilder.class, builder.getClass());

        // have or no range
        conditions = new Conditions(
            new Condition(
                new Field(1, "c1", FieldType.LONG),
                ConditionOperator.EQUALS,
                new LongValue(new Field(1, "c1", FieldType.LONG), 100L)));
        conditions.addOr(new Condition(
            new Field(1, "c1", FieldType.LONG),
            ConditionOperator.EQUALS,
            new LongValue(new Field(1, "c2", FieldType.LONG), 100L)));
        builder = optimizer.optimizeConditions(conditions);
        Assert.assertEquals(HaveOrNoRanageConditionsBuilder.class, builder.getClass());

        // have or have range
        conditions = new Conditions(
            new Condition(
                new Field(1, "c1", FieldType.LONG),
                ConditionOperator.GREATER_THAN,
                new LongValue(new Field(1, "c1", FieldType.LONG), 100L)));
        conditions.addOr(new Condition(
            new Field(1, "c1", FieldType.LONG),
            ConditionOperator.MINOR_THAN,
            new LongValue(new Field(1, "c2", FieldType.LONG), 100L)));
        builder = optimizer.optimizeConditions(conditions);
        Assert.assertEquals(HaveOrHaveRanageConditionsBuilder.class, builder.getClass());
    }


} 
