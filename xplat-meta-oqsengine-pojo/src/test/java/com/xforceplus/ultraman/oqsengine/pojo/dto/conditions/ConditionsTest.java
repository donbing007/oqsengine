package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
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
 * @author <Authors name>
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
