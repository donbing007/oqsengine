package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dongbin
 * @version 0.1 2020/4/23 14:32
 * @since 1.8
 */
public class ConditionNodeTest {

    @Test
    public void testEquals() throws Exception {
        IEntityClass aClass = new EntityClass(
            1, "A",
            new EntityField(1, "age", FieldType.LONG),
            new EntityField(3, "b_id", FieldType.LONG));
        IEntityClass bClass = new EntityClass(2, "B", new EntityField(2, "name", FieldType.STRING));

        ConditionNode node1,node2 ;
        node1 = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(aClass, aClass.field(1).get(),
                    ConditionOperator.EQUALS,
                    new LongValue(aClass.field(1).get(), 100L)))
            .addOr(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v1")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v3")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(bClass.field(2).get(), "v4"))).collectConditionTree();

        node2 = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(aClass, aClass.field(1).get(),
                    ConditionOperator.EQUALS,
                    new LongValue(aClass.field(1).get(), 100L)))
            .addOr(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v1")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v3")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(bClass.field(2).get(), "v4"))).collectConditionTree();

        Assert.assertTrue(node1.equals(node2));

        node1 = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(aClass, aClass.field(1).get(),
                    ConditionOperator.EQUALS,
                    new LongValue(aClass.field(1).get(), 100L)))
            .addOr(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v1")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v3")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(bClass.field(2).get(), "v4"))).collectConditionTree();

        node2 = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(aClass, aClass.field(1).get(),
                    ConditionOperator.EQUALS,
                    new LongValue(aClass.field(1).get(), 100L)))
            .addOr(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.EQUALS,
                    new StringValue(bClass.field(2).get(), "v1")))
            .addAnd(
                new Condition(bClass, bClass.field(2).get(),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(bClass.field(2).get(), "v4"))).collectConditionTree();
        Assert.assertFalse(node1.equals(node2));

    }

    @Test
    public void testSetRed() throws Exception {
        ValueConditionNode vNode = new ValueConditionNode(null);
        Assert.assertFalse(vNode.isRed());

        LinkConditionNode expectedNode = new LinkConditionNode(
            new ValueConditionNode(null), new ValueConditionNode(null), ConditionLink.AND);
        Assert.assertFalse(expectedNode.isRed());

        LinkConditionNode redNode = new LinkConditionNode(null, null, ConditionLink.AND);
        redNode.setRed(true);
        expectedNode = new LinkConditionNode(redNode, null, ConditionLink.AND);
        Assert.assertTrue(expectedNode.isRed());

        expectedNode = new LinkConditionNode(null, null, ConditionLink.AND);
        expectedNode.setLeft(redNode);
        Assert.assertTrue(expectedNode.isRed());

        expectedNode = new LinkConditionNode(null, null, ConditionLink.AND);
        expectedNode.setRight(redNode);
        Assert.assertTrue(expectedNode.isRed());
    }
}
