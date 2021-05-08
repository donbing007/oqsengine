package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void testScan() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();

        conditions.addAnd(
            new Condition(
                EntityField.UPDATE_TIME_FILED,
                ConditionOperator.EQUALS,
                new LongValue(EntityField.UPDATE_TIME_FILED, 100L)
            )
        ).addAnd(
            new Condition(
                EntityField.UPDATE_TIME_FILED,
                ConditionOperator.EQUALS,
                new LongValue(EntityField.UPDATE_TIME_FILED, 200L)
            )
        );

        conditions.addAnd(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        EntityField.CREATE_TIME_FILED,
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(EntityField.CREATE_TIME_FILED, 3000L)
                    )
                )
                .addOr(
                    new Condition(
                        EntityField.CREATE_TIME_FILED,
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(EntityField.CREATE_TIME_FILED, 5000L)
                    )
                ),
            true
        );

        AtomicBoolean shadow = new AtomicBoolean(false);
        StringBuilder buff = new StringBuilder();
        conditions.scan(
            c -> {
                shadow.set(c.isShadow());
                buff.append(" ").append(c.getLink().name()).append(" ");
            },
            c -> buff.append(c.toString()),
            c -> buff.append(c.toString())
        );

        /*
         * 验证不能将影子结点返回.
         */
        Assert.assertFalse(shadow.get());

        Assert.assertEquals(
            "updateTime = 100 AND updateTime = 200 AND (createTime != 3000 OR createTime != 5000)", buff.toString());
    }


    @Test
    public void testAndOrFlag() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();

        Assert.assertEquals(false, conditions.haveOrLink());
        Assert.assertEquals(false, conditions.haveRangeCondition());

        IEntityField field = new EntityField(1, "test", FieldType.STRING);
        conditions.addAnd(
            new Condition(field, ConditionOperator.EQUALS, new StringValue(field, "test")));

        Assert.assertEquals(false, conditions.haveOrLink());
        Assert.assertEquals(false, conditions.haveRangeCondition());


        Conditions orConnditons = Conditions.buildEmtpyConditions().addAnd(
            new Condition(field, ConditionOperator.EQUALS, new StringValue(field, "test"))
        );
        conditions.addOr(orConnditons, true);
        Assert.assertEquals(true, conditions.haveOrLink());
        Assert.assertEquals(false, conditions.haveRangeCondition());
    }

    @Test
    public void testFuzzyFlag() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Assert.assertFalse(conditions.haveFuzzyCondition());

        IEntityField field = new EntityField(1, "test", FieldType.STRING);

        // 增加一个等值条件.
        conditions.addAnd(
            new Condition(
                field,
                ConditionOperator.EQUALS,
                new StringValue(field, "test")
            )
        );
        Assert.assertFalse(conditions.haveFuzzyCondition());

        // 增加一个模糊查询条件.
        conditions.addAnd(
            new Condition(
                field,
                ConditionOperator.LIKE,
                new StringValue(field, "test")
            )
        );
        Assert.assertTrue(conditions.haveFuzzyCondition());

        // 增加一个条件组,条件组本身没有模糊,但当前条件组已经含有模糊,最终结果仍为模糊.
        conditions.addAnd(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        field,
                        ConditionOperator.EQUALS,
                        new StringValue(field, "test")
                    )
                ), false
        );
        Assert.assertTrue(conditions.haveFuzzyCondition());

    }

    @Test
    public void testValidation() throws Exception {
        Condition wrongCondition = new Condition(
            new EntityField(1, "test", FieldType.STRING),
            ConditionOperator.GREATER_THAN,
            new StringValue(new EntityField(1, "test", FieldType.STRING), "test.value"));

        try {
            new Conditions(wrongCondition);
            Assert.fail("Attempt to add error condition, but no error.");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        Condition correctCondition = new Condition(
            new EntityField(1, "test", FieldType.STRING),
            ConditionOperator.LIKE,
            new StringValue(new EntityField(1, "test", FieldType.STRING), "test.value")
        );
        Conditions conditions = new Conditions(correctCondition);
        Assert.assertEquals(1, conditions.size());

        try {
            conditions.addAnd(wrongCondition);
            Assert.fail("Attempt to add error condition, but no error.");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testIteratorSubStree() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    new EntityField(1, "c1", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L))
            )
            .addOr(
                new Condition(
                    new EntityField(2, "c2", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(2, "c2", FieldType.LONG), 100L))
            )
            .addAnd(
                new Condition(
                    new EntityField(3, "c3", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(3, "c3", FieldType.LONG), 100L)
                )
            );

        List<AbstractConditionNode> nodes = new ArrayList(conditions.collectSubTree(c -> !c.isRed(), true));
        Assert.assertEquals(2, nodes.size());
        String[] expectedStrings = new String[] {
            "c1 = 100",
            "c2 = 100 AND c3 = 100"
        };
        for (int i = 0; i < expectedStrings.length; i++) {
            Assert.assertEquals(expectedStrings[i], nodes.get(i).toString());
        }


        Condition expectedCondtiton = new Condition(
            new EntityField(1, "c1", FieldType.LONG),
            ConditionOperator.EQUALS,
            new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L));
        Assert.assertEquals(expectedCondtiton,
            ((ValueConditionNode) nodes.stream().filter(c -> Conditions.isValueNode(c)).findFirst().get())
                .getCondition());
        Assert.assertEquals(ConditionLink.AND,
            ((LinkConditionNode) nodes.stream().filter(c -> Conditions.isLinkNode(c)).findFirst().get()).getLink());

        conditions = Conditions.buildEmtpyConditions()
            .addOr(
                new Condition(
                    new EntityField(1, "c1", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L))
            ).addOr(
                new Condition(
                    new EntityField(2, "c2", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(2, "c2", FieldType.LONG), 100L))
            ).addOr(
                new Condition(
                    new EntityField(3, "c3", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(3, "c3", FieldType.LONG), 100L)
                )
            ).addAnd(
                new Condition(
                    new EntityField(4, "c4", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(4, "c4", FieldType.LONG), 100L))
            );

        nodes = new ArrayList(conditions.collectSubTree(c -> !c.isRed(), true));
        Assert.assertEquals(3, nodes.size());
        expectedStrings = new String[] {
            "c1 = 100",
            "c2 = 100",
            "c3 = 100 AND c4 = 100"
        };
        for (int i = 0; i < expectedStrings.length; i++) {
            Assert.assertEquals(expectedStrings[i], nodes.get(i).toString());
        }
    }

    @Test
    public void testIterator() throws Exception {

        buildIteratorCase().stream().forEach(c -> {

            Assert.assertEquals(c.expected, c.conditions.toPrefixExpression());

        });
    }

    /**
     * 测试从一个条件结点构造新的Conditons.
     */
    @Test
    public void testInitFromNode() throws Exception {
        Conditions expectedConditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    EntityClassRef.Builder.anEntityClassRef().withEntityClassId(1).withEntityClassCode("driver")
                        .build(),
                    EntityField.CREATE_TIME_FILED,
                    ConditionOperator.EQUALS,
                    1L,
                    new LongValue(EntityField.CREATE_TIME_FILED, 100L)
                )
            )
            .addAnd(
                new Condition(
                    EntityField.UPDATE_TIME_FILED,
                    ConditionOperator.EQUALS,
                    new LongValue(EntityField.UPDATE_TIME_FILED, 3)
                )
            );

        Conditions newConditions = new Conditions(expectedConditions.collectConditionTree());
        Assert.assertEquals(newConditions.size(), expectedConditions.size());
        Assert.assertEquals(newConditions.isEmtpy(), expectedConditions.isEmtpy());
        Assert.assertEquals(newConditions.haveFuzzyCondition(), expectedConditions.haveFuzzyCondition());
        Assert.assertEquals(newConditions.haveRangeCondition(), expectedConditions.haveRangeCondition());
        Assert.assertEquals(newConditions.haveOrLink(), expectedConditions.haveOrLink());
    }

    private Collection<Case> buildIteratorCase() {
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L))),
                "c1 = 100"
            ),
            new Case(
                new Conditions(
                    new Condition(
                        new EntityField(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new EntityField(1, "c2", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new EntityField(1, "c2", FieldType.LONG), 100L))),
                "AND c1 = 100 c2 = 100"
            ),
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    Conditions.buildEmtpyConditions().addAnd(
                        new Condition(
                            new EntityField(1, "c1", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                        )
                    ), false
                ),
                "c1 = 100"
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(1, "c2", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(1, "c2", FieldType.LONG), 100L))
                    )
                    .addAnd(
                        Conditions.buildEmtpyConditions().addAnd(
                            new Condition(
                                new EntityField(1, "c1", FieldType.LONG),
                                ConditionOperator.EQUALS,
                                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                            )
                        ), false
                    ),
                "AND c2 = 100 c1 = 100"
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(2, "c2", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(2, "c2", FieldType.LONG), 100L))
                    )
                    .addOr(
                        Conditions.buildEmtpyConditions().addAnd(
                            new Condition(
                                new EntityField(1, "c1", FieldType.LONG),
                                ConditionOperator.EQUALS,
                                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                            )
                        ), false
                    ).addAnd(
                    Conditions.buildEmtpyConditions().addAnd(
                        new Condition(
                            new EntityField(3, "c3", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(3, "c3", FieldType.LONG), 100L)
                        )
                    ), false
                ),
                "OR(r) c2 = 100 AND c1 = 100 c3 = 100"
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(2, "c2", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(2, "c2", FieldType.LONG), 100L))
                    )
                    .addOr(
                        Conditions.buildEmtpyConditions().addAnd(
                            new Condition(
                                new EntityField(1, "c1", FieldType.LONG),
                                ConditionOperator.EQUALS,
                                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                            )
                        ), false
                    ).addAnd(
                    Conditions.buildEmtpyConditions().addAnd(
                        new Condition(
                            new EntityField(3, "c3", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(3, "c3", FieldType.LONG), 100L)
                        )
                    ).addAnd(
                        new Condition(
                            new EntityField(3, "c3", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(3, "c3", FieldType.LONG), 200L))
                    ), true
                ),
                "OR(r) c2 = 100 AND c1 = 100 (AND c3 = 100 c3 = 200)"
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new EntityField(2, "c2", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(2, "c2", FieldType.LONG), 100L))
                    )
                    .addOr(
                        Conditions.buildEmtpyConditions().addAnd(
                            new Condition(
                                new EntityField(1, "c1", FieldType.LONG),
                                ConditionOperator.EQUALS,
                                new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                            )
                        ), false
                    ).close().addAnd(
                    Conditions.buildEmtpyConditions().addAnd(
                        new Condition(
                            new EntityField(3, "c3", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(3, "c3", FieldType.LONG), 100L)
                        )
                    ).addAnd(
                        new Condition(
                            new EntityField(3, "c3", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(3, "c3", FieldType.LONG), 200L))
                    ), true
                ),
                "AND(r) (OR(r) c2 = 100 c1 = 100) (AND c3 = 100 c3 = 200)"
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
