package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author dongbin
 * @version 0.1 2022/7/26 16:22
 * @since 1.8
 */
public class InfuenceGraphTest {

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
     * 测试scan顺序.同一层以加入顺序迭代.
     */
    @Test
    public void testScan() {
        Participant root = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        InfuenceGraph graph = new InfuenceGraph(root);

        Participant bpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, bpar));

        Participant cpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, cpar));

        Participant dpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(bpar, dpar));

        Participant epar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(E_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(cpar, epar));
        Assertions.assertTrue(graph.impact(dpar, epar));

        List<Participant> participantList = new ArrayList<>();
        graph.scan((parent, participant, inner) -> {
            participantList.add(participant);
            return InfuenceGraphConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(root, participantList.get(0));
        Assertions.assertEquals(bpar, participantList.get(1));
        Assertions.assertEquals(cpar, participantList.get(2));
        Assertions.assertEquals(dpar, participantList.get(3));
        Assertions.assertEquals(epar, participantList.get(4));
    }

    @Test
    public void testScanNoSource() throws Exception {
        Participant root = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        InfuenceGraph graph = new InfuenceGraph(root);

        Participant bpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, bpar));

        Participant cpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, cpar));

        Participant dpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(bpar, dpar));

        Participant epar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(E_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(cpar, epar));
        Assertions.assertTrue(graph.impact(dpar, epar));

        List<Participant> participantList = new ArrayList<>();
        graph.scanNoSource((parent, participant, inner) -> {
            participantList.add(participant);
            return InfuenceGraphConsumer.Action.CONTINUE;
        });


        Assertions.assertEquals(bpar, participantList.get(0));
        Assertions.assertEquals(cpar, participantList.get(1));
        Assertions.assertEquals(dpar, participantList.get(2));
        Assertions.assertEquals(epar, participantList.get(3));
    }

    /**
     * 测试不允许出现环.
     */
    @Test
    public void testRing() {
        Participant root = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        InfuenceGraph graph = new InfuenceGraph(root);

        Participant bpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, bpar));

        Participant cpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, cpar));

        Participant dpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(bpar, dpar));

        Participant epar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(E_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(cpar, epar));
        Assertions.assertTrue(graph.impact(dpar, epar));

        // 重复增加,不会真的增加但是返回成功.
        Assertions.assertTrue(graph.impact(dpar, epar));
        // 重复.
        Assertions.assertFalse(graph.impact(epar, bpar));
        Assertions.assertEquals(5, graph.size());
    }

    @Test
    public void testUpdateChildLevel() throws Exception {
        Participant root = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        InfuenceGraph graph = new InfuenceGraph(root);

        Participant bpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, bpar));

        Participant cpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(root, cpar));

        Participant dpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(bpar, dpar));

        Participant epar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(E_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph.impact(cpar, epar));
        Assertions.assertTrue(graph.impact(dpar, epar));
        // 移动 (c, id, 1) 结点至 (d, id, 2)结点后, 应该所有子结点的层次都被更新.
        Assertions.assertTrue(graph.impact(dpar, cpar));

        // 记录迭代顺序.
        List<String> participants = new ArrayList<>();
        graph.scanNoSource((parent, participant, inner) -> {
            participants.add(participant.getEntityClass().code());
            return InfuenceGraphConsumer.Action.CONTINUE;
        });

        List<String> expected = new ArrayList<>();
        expected.add(B_CLASS.code());
        expected.add(D_CLASS.code());
        expected.add(C_CLASS.code());
        expected.add(E_CLASS.code());

        Assertions.assertEquals(expected, participants);
    }

    /**
     * 两个图的比较.
     */
    @Test
    public void testEquals() {
        Participant root = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        InfuenceGraph graph0 = new InfuenceGraph(root);
        InfuenceGraph graph1 = new InfuenceGraph(root);

        Participant bpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph0.impact(root, bpar));
        Assertions.assertTrue(graph1.impact(root, bpar));

        Participant cpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph0.impact(root, cpar));
        Assertions.assertTrue(graph1.impact(root, cpar));

        Participant dpar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph0.impact(bpar, dpar));
        Assertions.assertTrue(graph1.impact(bpar, dpar));

        Participant epar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(E_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        Assertions.assertTrue(graph0.impact(cpar, epar));
        Assertions.assertTrue(graph1.impact(cpar, epar));
        Assertions.assertTrue(graph0.impact(dpar, epar));
        Assertions.assertTrue(graph1.impact(dpar, epar));

        Assertions.assertEquals(graph0, graph1);
    }
}