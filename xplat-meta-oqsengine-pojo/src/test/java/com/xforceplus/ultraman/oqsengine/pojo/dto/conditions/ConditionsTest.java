package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Conditions Tester.
 *
 * @author dongbin
 * @version 1.0 02/22/2020
 * @since <pre>Feb 22, 2020</pre>
 */
public class ConditionsTest {

    private static final IEntityField stringField = EntityField.Builder.anEntityField()
        .withId(1000L)
        .withFieldType(FieldType.STRING)
        .withName("string-filed")
        .build();
    private static final IEntityField stringsField = EntityField.Builder.anEntityField()
        .withId(2000L)
        .withFieldType(FieldType.STRINGS)
        .withName("strings-filed")
        .build();
    private static final IEntityClass matchEntityClass = EntityClass.Builder.anEntityClass()
        .withId(1000L)
        .withField(stringField)
        .withName("matchEntityClass")
        .build();

    @Test
    public void testLikeMatch() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(matchEntityClass.ref())
            .withId(100)
            .withValue(
                new StringValue(stringField, "这是一个测试")
            ).build();

        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(stringField, ConditionOperator.LIKE, new StringValue(stringField, "一个"))
            );
        Assertions.assertTrue(conditions.match(entity));
    }

    @Test
    public void testInMatch() throws Exception {
        Collection<IEntity> entities = Arrays.asList(
            Entity.Builder.anEntity()
                .withEntityClassRef(matchEntityClass.ref())
                .withId(1000)
                .withValue(new StringValue(stringField, "v1"))
                .withValue(new StringsValue(stringsField, "s1", "s2")).build(),
            Entity.Builder.anEntity()
                .withEntityClassRef(matchEntityClass.ref())
                .withId(2000)
                .withValue(new StringValue(stringField, "v2"))
                .withValue(new StringsValue(stringsField, "s2", "s3", "s4")).build(),
            Entity.Builder.anEntity()
                .withEntityClassRef(matchEntityClass.ref())
                .withId(3000)
                .withValue(new StringValue(stringField, "v3"))
                .withValue(new StringsValue(stringsField, "s3", "s4")).build()
        );

        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    stringField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(stringField, "v1"),
                    new StringValue(stringField, "v3"))
            );
        Collection<IEntity> matchEntities = conditions.match(entities);
        Assertions.assertEquals(2, matchEntities.size());
        // id不等于2000的应该有2个.
        Assertions.assertEquals(2, matchEntities.stream().filter(e -> e.id() != 2000).count());

        conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    stringsField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(stringField, "s2"),
                    new StringValue(stringField, "s3"))
            );
        matchEntities = conditions.match(entities);
        Assertions.assertEquals(3, matchEntities.size());
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
        Assertions.assertFalse(shadow.get());

        Assertions.assertEquals(
            "(updateTime = 100 AND updateTime = 200) AND createTime != 3000 OR createTime != 5000", buff.toString());
    }


    @Test
    public void testAndOrFlag() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();

        Assertions.assertEquals(false, conditions.haveOrLink());
        Assertions.assertEquals(false, conditions.haveRangeCondition());

        IEntityField field = new EntityField(1, "test", FieldType.STRING);
        conditions.addAnd(
            new Condition(field, ConditionOperator.EQUALS, new StringValue(field, "test")));

        Assertions.assertEquals(false, conditions.haveOrLink());
        Assertions.assertEquals(false, conditions.haveRangeCondition());


        Conditions orConnditons = Conditions.buildEmtpyConditions().addAnd(
            new Condition(field, ConditionOperator.EQUALS, new StringValue(field, "test"))
        );
        conditions.addOr(orConnditons, true);
        Assertions.assertEquals(true, conditions.haveOrLink());
        Assertions.assertEquals(false, conditions.haveRangeCondition());
    }

    @Test
    public void testFuzzyFlag() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Assertions.assertFalse(conditions.haveFuzzyCondition());

        IEntityField field = new EntityField(1, "test", FieldType.STRING);

        // 增加一个等值条件.
        conditions.addAnd(
            new Condition(
                field,
                ConditionOperator.EQUALS,
                new StringValue(field, "test")
            )
        );
        Assertions.assertFalse(conditions.haveFuzzyCondition());

        // 增加一个模糊查询条件.
        conditions.addAnd(
            new Condition(
                field,
                ConditionOperator.LIKE,
                new StringValue(field, "test")
            )
        );
        Assertions.assertTrue(conditions.haveFuzzyCondition());

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
        Assertions.assertTrue(conditions.haveFuzzyCondition());

    }

    @Test
    public void testValidation() throws Exception {
        Condition wrongCondition = new Condition(
            new EntityField(1, "test", FieldType.STRING),
            ConditionOperator.GREATER_THAN,
            new StringValue(new EntityField(1, "test", FieldType.STRING), "test.value"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Conditions(wrongCondition));

        Condition correctCondition = new Condition(
            new EntityField(1, "test", FieldType.STRING),
            ConditionOperator.LIKE,
            new StringValue(new EntityField(1, "test", FieldType.STRING), "test.value")
        );
        Conditions conditions = new Conditions(correctCondition);
        Assertions.assertEquals(1, conditions.size());

        Assertions.assertThrows(IllegalArgumentException.class, () -> conditions.addAnd(wrongCondition));
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
        Assertions.assertEquals(2, nodes.size());
        String[] expectedStrings = new String[] {
            "c1 = 100",
            "c2 = 100 AND c3 = 100"
        };
        for (int i = 0; i < expectedStrings.length; i++) {
            Assertions.assertEquals(expectedStrings[i], nodes.get(i).toString());
        }


        Condition expectedCondtiton = new Condition(
            new EntityField(1, "c1", FieldType.LONG),
            ConditionOperator.EQUALS,
            new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L));
        Assertions.assertEquals(expectedCondtiton,
            ((ValueConditionNode) nodes.stream().filter(c -> Conditions.isValueNode(c)).findFirst().get())
                .getCondition());
        Assertions.assertEquals(ConditionLink.AND,
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
        Assertions.assertEquals(3, nodes.size());
        expectedStrings = new String[] {
            "c1 = 100",
            "c2 = 100",
            "c3 = 100 AND c4 = 100"
        };
        for (int i = 0; i < expectedStrings.length; i++) {
            Assertions.assertEquals(expectedStrings[i], nodes.get(i).toString());
        }
    }

    @Test
    public void testIterator() throws Exception {

        buildIteratorCase().stream().forEach(c -> {

            Assertions.assertEquals(c.expected, c.conditions.toPrefixExpression());

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
        Assertions.assertEquals(newConditions.size(), expectedConditions.size());
        Assertions.assertEquals(newConditions.isEmtpy(), expectedConditions.isEmtpy());
        Assertions.assertEquals(newConditions.haveFuzzyCondition(), expectedConditions.haveFuzzyCondition());
        Assertions.assertEquals(newConditions.haveRangeCondition(), expectedConditions.haveRangeCondition());
        Assertions.assertEquals(newConditions.haveOrLink(), expectedConditions.haveOrLink());
    }

    private Collection<Case> buildIteratorCase() {
        return Arrays.asList(
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.Builder.anEntityField()
                                .withId(1)
                                .withName("c1")
                                .withFieldType(FieldType.LONG)
                                .build(),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(1, "c1", FieldType.LONG), 100L)
                        )
                    ).addOr(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    EntityField.Builder.anEntityField()
                                        .withId(2)
                                        .withName("c2")
                                        .withFieldType(FieldType.LONG)
                                        .build(),
                                    ConditionOperator.EQUALS,
                                    new LongValue(new EntityField(1, "c1", FieldType.LONG), 200L)
                                )
                            ), true
                    ),
                "(OR(r) c1 = 100 c2 = 200)"
            ),
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
                "(OR(r) c2 = 100 AND c1 = 100 AND c3 = 100 c3 = 200)"
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
                "AND(r) (OR(r) c2 = 100 c1 = 100) AND c3 = 100 c3 = 200"
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
