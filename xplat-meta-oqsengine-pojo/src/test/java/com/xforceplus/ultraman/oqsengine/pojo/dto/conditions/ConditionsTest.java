package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Conditions Tester.
 *
 * @author dongbin
 * @version 1.0 02/22/2020
 * @since <pre>Feb 22, 2020</pre>
 */
public class ConditionsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testValidation() throws Exception {
        Condition wrongCondition = new Condition(
            new Field(1, "test", FieldType.STRING),
            ConditionOperator.GREATER_THAN,
            new StringValue(new Field(1, "test", FieldType.STRING), "test.value"));

        try {
            new Conditions(wrongCondition);
            Assert.fail("Attempt to add error condition, but no error.");
        } catch (IllegalArgumentException ex) {
        }

        Condition correctCondition = new Condition(
            new Field(1, "test", FieldType.STRING),
            ConditionOperator.LIKE,
            new StringValue(new Field(1, "test", FieldType.STRING), "test.value")
        );
        Conditions conditions = new Conditions(correctCondition);
        Assert.assertEquals(1, conditions.size());

        try {
            conditions.addAnd(wrongCondition);
            Assert.fail("Attempt to add error condition, but no error.");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testIterator() throws Exception {
        buildCase().stream().forEach(c -> {

            Iterator<ConditionNode> iter = c.conditions.iterator();
            StringBuilder buff = new StringBuilder();

            while(iter.hasNext()) {
                ConditionNode node = iter.next();
                if (node instanceof ValueConditionNode) {
                    ValueConditionNode current = (ValueConditionNode) node;
                    buff.append(current.getCondition().getField().name())
                        .append(current.getCondition().getOperator().getSymbol())
                        .append(current.getCondition().getValue().getValue());
                } else {

                    LinkConditionNode current = (LinkConditionNode) node;
                    buff.append(" ").append(current.getLink().toString()).append(" ");
                }
            }

            Assert.assertEquals(c.expected, buff.toString());

        });
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L))),
                "c1=100"
            )
            ,
            new Case(
                new Conditions(
                new Condition(
                    new Field(1, "c1", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new Field(1, "c1", FieldType.LONG), 100L)))
                .addAnd(new Condition(
                    new Field(1, "c2", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new Field(1, "c2", FieldType.LONG), 100L))),
                "c1=100 AND c2=100"
            ),
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    Conditions.buildEmtpyConditions().addAnd(
                        new Condition(
                            new Field(1, "c1", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                        )
                    )
                    ,false
                ),
                "c1=100"
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new Field(1, "c2", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new Field(1, "c2", FieldType.LONG), 100L))
                    )
                    .addAnd(
                        Conditions.buildEmtpyConditions().addAnd(
                            new Condition(
                                new Field(1, "c1", FieldType.LONG),
                                ConditionOperator.EQUALS,
                                new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                            )
                        )
                        ,false
                    ),
                "c2=100 AND c1=100"
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
