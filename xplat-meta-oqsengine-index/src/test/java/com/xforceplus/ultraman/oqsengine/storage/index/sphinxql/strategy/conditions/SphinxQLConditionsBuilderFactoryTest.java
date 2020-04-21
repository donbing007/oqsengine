package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SphinxQLConditionsBuilderFactory Tester.
 *
 * @author <Authors name>
 * @version 1.0 04/21/2020
 * @since <pre>Apr 21, 2020</pre>
 */
public class SphinxQLConditionsBuilderFactoryTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getBuilder(Conditions conditions)
     */
    @Test
    public void testGetBuilder() throws Exception {
        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(StorageStrategyFactory.getDefaultFactory());

        // no or no ranage
        Conditions conditions = new Conditions(
            new Condition(
                new EntityField(1, "c1", FieldType.LONG),
                ConditionOperator.EQUALS,
                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)));

        sphinxQLConditionsBuilderFactory.init();

        ConditionsBuilder builder = sphinxQLConditionsBuilderFactory.getBuilder(conditions);
        Assert.assertEquals(NoOrNoRanageConditionsBuilder.class, builder.getClass());

        // no or have ranage
        conditions = new Conditions(
            new Condition(
                new EntityField(1, "c1", FieldType.LONG),
                ConditionOperator.GREATER_THAN,
                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)));
        builder = sphinxQLConditionsBuilderFactory.getBuilder(conditions);
        Assert.assertEquals(NoOrHaveRanageConditionsBuilder.class, builder.getClass());

        // have or no range
        conditions = new Conditions(
            new Condition(
                new EntityField(1, "c1", FieldType.LONG),
                ConditionOperator.EQUALS,
                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)));
        conditions.addOr(new Condition(
            new EntityField(1, "c1", FieldType.LONG),
            ConditionOperator.EQUALS,
            new LongValue(new EntityField(1, "c2", FieldType.LONG), 100L)));
        builder = sphinxQLConditionsBuilderFactory.getBuilder(conditions);
        Assert.assertEquals(HaveOrNoRanageConditionsBuilder.class, builder.getClass());

        // have or have range
        conditions = new Conditions(
            new Condition(
                new EntityField(1, "c1", FieldType.LONG),
                ConditionOperator.GREATER_THAN,
                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)));
        conditions.addOr(new Condition(
            new EntityField(1, "c1", FieldType.LONG),
            ConditionOperator.LESS_THAN,
            new LongValue(new EntityField(1, "c2", FieldType.LONG), 100L)));
        builder = sphinxQLConditionsBuilderFactory.getBuilder(conditions);
        Assert.assertEquals(HaveOrHaveRanageConditionsBuilder.class, builder.getClass());
    }


} 
