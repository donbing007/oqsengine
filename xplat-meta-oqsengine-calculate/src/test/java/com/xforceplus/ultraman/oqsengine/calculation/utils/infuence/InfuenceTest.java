package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 影响力树的测试.
 *
 * @author dongbin
 * @since 1.8
 */
public class InfuenceTest {

    static long baseClassId = Long.MAX_VALUE;

    static IEntityClass A_CLASS = EntityClass.Builder.anEntityClass()
        .withId(baseClassId)
        .withCode("a")
        .withLevel(0)
        .withField(EntityField.CREATE_TIME_FILED)
        .build();

    static IEntityClass B_CLASS = EntityClass.Builder.anEntityClass()
        .withId(baseClassId - 1)
        .withCode("b")
        .withLevel(0)
        .withField(EntityField.CREATE_TIME_FILED)
        .build();

    static IEntityClass C_CLASS = EntityClass.Builder.anEntityClass()
        .withId(baseClassId - 2)
        .withCode("c")
        .withLevel(0)
        .withField(EntityField.CREATE_TIME_FILED)
        .build();

    static IEntityClass D_CLASS = EntityClass.Builder.anEntityClass()
        .withId(baseClassId - 3)
        .withCode("d")
        .withLevel(0)
        .withField(EntityField.CREATE_TIME_FILED)
        .build();

    static IEntityClass E_CLASS = EntityClass.Builder.anEntityClass()
        .withId(baseClassId - 4)
        .withCode("e")
        .withLevel(0)
        .withField(EntityField.CREATE_TIME_FILED)
        .build();

    /**
     * 测试只有一个root结点.
     */
    @Test
    public void testOnlyRoot() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(entity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                entity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        ValueChange expectedChange = new ValueChange(
            entity.id(),
            new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
            new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
        );
        Assertions.assertEquals(entity.id(), infuence.getSourceEntity().id());
        Assertions.assertEquals(expectedChange, infuence.getValueChange());


        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        infuence.scan((parentClass, participant, infuenceInner) -> {

            atomicBoolean.set(true);
            return InfuenceConsumer.Action.OVER;
        });

        Assertions.assertTrue(atomicBoolean.get());
    }

    /**
     * 测试构造树.单分支.
     * ····A
     * ····|
     * ····B
     * ····|
     * ····c
     */
    @Test
    public void testBuildSingleBranch() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        // 出现在root结点下.
        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS).withField(EntityField.CREATE_TIME_FILED).build());

        // 出现在B_Class之下.
        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS).withField(EntityField.CREATE_TIME_FILED).build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS).withField(EntityField.CREATE_TIME_FILED).build()
        );

        List<IEntityClass> scanResults = new ArrayList(2);
        infuence.scan((parent, participant, infuence1) -> {

            scanResults.add(participant.getEntityClass());

            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(A_CLASS, scanResults.get(0));
        Assertions.assertEquals(B_CLASS, scanResults.get(1));
        Assertions.assertEquals(C_CLASS, scanResults.get(2));
    }

    /**
     * 测试中止当前被当前影响力影响的所有影响,但是不受当前结点影响的将继续迭代.目标影响如下.
     * .....A
     * .....|
     * ...|-----|
     * ...B     D
     * ...|     |
     * ...C     E
     */
    @Test
    public void testOverSelf() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build()
        );
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build()
        );

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(E_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());

        List<IEntityClass> resultsClass = new ArrayList<>();
        infuence.scan((parentParticipantOp, participant, infuenceInner) -> {
            resultsClass.add(participant.getEntityClass());
            if (participant.getEntityClass().id() == B_CLASS.id()) {
                return InfuenceConsumer.Action.OVER_SELF;
            } else {
                return InfuenceConsumer.Action.CONTINUE;
            }
        });

        Assertions.assertEquals(4, resultsClass.size());
        Assertions.assertEquals(0, resultsClass.stream().filter(ec -> ec.id() == C_CLASS.id()).count());
    }

    /**
     * 创建多分支.
     * .....A
     * .....|
     * ...|-----|
     * ...B     D
     * ...|     |
     * ...C     E
     */
    @Test
    public void testBuildManyBranches() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build()
        );
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build()
        );

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(E_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());

        List<IEntityClass> scanResults = new ArrayList(4);
        infuence.scan((parentClass, participant, infuence1) -> {

            scanResults.add(participant.getEntityClass());

            return InfuenceConsumer.Action.CONTINUE;
        });

        Collections.sort(scanResults, (o1, o2) -> {
            if (o1.id() < o2.id()) {
                return 1;
            } else if (o1.id() > o2.id()) {
                return -1;
            } else {
                return 0;
            }
        });

        Assertions.assertEquals(A_CLASS, scanResults.get(0));
        Assertions.assertEquals(B_CLASS, scanResults.get(1));
        Assertions.assertEquals(C_CLASS, scanResults.get(2));
        Assertions.assertEquals(D_CLASS, scanResults.get(3));
        Assertions.assertEquals(E_CLASS, scanResults.get(4));

    }

    /**
     * 测试扫描的同时增加. 基础树如下.
     * ...........A
     * ...........|
     * ........|-----|
     * ........B     D
     * .......|
     * .......C
     * 目标为
     * .........A
     * .........|
     * ......|-----|
     * ......B     D
     * ......|     |
     * ......C     E
     */
    @Test
    public void testScanWithAdd() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity, CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.CREATE_TIME_FILED)
            .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());


        infuence.scan((parent, participant, infuenceInner) -> {

            if (participant.getEntityClass().id() == D_CLASS.id()) {
                infuenceInner.impact(
                    parent.get(),
                        CalculationParticipant.Builder.anParticipant()
                        .withEntityClass(E_CLASS)
                        .withField(EntityField.CREATE_TIME_FILED)
                        .build());
            }

            return InfuenceConsumer.Action.CONTINUE;
        });

        List<IEntityClass> scanResults = new ArrayList(4);
        infuence.scan((parentClass, participant, infuence1) -> {

            scanResults.add(participant.getEntityClass());

            return InfuenceConsumer.Action.CONTINUE;
        });

        Collections.sort(scanResults, (o1, o2) -> {
            if (o1.id() < o2.id()) {
                return 1;
            } else if (o1.id() > o2.id()) {
                return -1;
            } else {
                return 0;
            }
        });
        Assertions.assertEquals(A_CLASS, scanResults.get(0));
        Assertions.assertEquals(B_CLASS, scanResults.get(1));
        Assertions.assertEquals(C_CLASS, scanResults.get(2));
        Assertions.assertEquals(D_CLASS, scanResults.get(3));
        Assertions.assertEquals(E_CLASS, scanResults.get(4));

    }

    /**
     * 测试是否以广度优先方式遍历.
     * ...........A
     * ...........|
     * ........|-----|
     * ........B     D
     * ........|     |
     * ........C     E
     */
    @Test
    public void testBfsIter() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(E_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());

        List<IEntityClass> results = new ArrayList<>();
        infuence.scan((parentClass, participant, infuenceInner) -> {

            results.add(participant.getEntityClass());

            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(A_CLASS.id(), results.get(0).id());
        Assertions.assertEquals(B_CLASS.id(), results.get(1).id());
        Assertions.assertEquals(D_CLASS.id(), results.get(2).id());
        Assertions.assertEquals(C_CLASS.id(), results.get(3).id());
        Assertions.assertEquals(E_CLASS.id(), results.get(4).id());
    }

    /**
     * 测试同样的参与者不能同时处于同一个双亲结点下.
     */
    @Test
    public void testDifferentFieldInSameClass() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.UPDATE_TIME_FILED).build()
        );

        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.UPDATE_TIME_FILED).build()
        );

        List<IEntityClass> results = new ArrayList<>();
        infuence.scan((parent, participant, infuenceInner) -> {
            if (parent.isPresent()) {
                results.add(participant.getEntityClass());
            }
            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(B_CLASS, results.get(0));
    }

    @Test
    public void testToString() throws Exception {
        IEntity rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        Infuence infuence = new Infuence(rootEntity,
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));
        infuence.impact(
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.UPDATE_TIME_FILED).build()
        );

        Assertions.assertEquals("\n(a,createTime)··\n |- (b,updateTime)\n", infuence.toString());

        // 多结点
        rootEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref()).build();
        infuence = new Infuence(rootEntity, CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.CREATE_TIME_FILED)
            .build(),
            new ValueChange(
                rootEntity.id(),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
            ));

        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());
        infuence.impact(CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build(),
                CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build());

        Assertions.assertEquals(
            "\n(a,createTime)······\n |- (d,createTime)····\n |- (b,createTime)····\n·· |- (c,createTime)··\n",
            infuence.toString());
    }


    /**
     * 测试nextParticipant是否符合预期.
     * ...........A
     * ...........|
     * ........|-----|
     * ........B     D
     * ........|     |
     * ........C     E
     */
    @Test
    public void testGetNextParticipants() {
        IEntity rootEntity = Entity.Builder.anEntity()
                .withId(Long.MAX_VALUE)
                .withEntityClassRef(A_CLASS.ref()).build();
        CalculationParticipant a = CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build();
        Infuence infuence = new Infuence(rootEntity, a,
                new ValueChange(
                        rootEntity.id(),
                        new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX),
                        new DateTimeValue(EntityField.CREATE_TIME_FILED, LocalDateTime.MAX)
                ));
        CalculationParticipant b = CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build();
        infuence.impact(a, b);

        CalculationParticipant c = CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build();
        infuence.impact(b, c);

        CalculationParticipant d = CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build();
        infuence.impact(a, d);
        CalculationParticipant e = CalculationParticipant.Builder.anParticipant()
                .withEntityClass(E_CLASS)
                .withField(EntityField.CREATE_TIME_FILED)
                .build();
        infuence.impact(d, e);

        Collection<Participant> nextAbstractParticipants = infuence.getNextParticipants(a).get();
        Assertions.assertTrue(nextAbstractParticipants.contains(b));
        Assertions.assertTrue(nextAbstractParticipants.contains(d));

        Collection<Participant> nextParticipants1 = infuence.getNextParticipants(b).get();
        Assertions.assertTrue(nextParticipants1.contains(c));

        Collection<Participant> nextParticipants2 = infuence.getNextParticipants(d).get();
        Assertions.assertTrue(nextParticipants2.contains(e));
    }
}