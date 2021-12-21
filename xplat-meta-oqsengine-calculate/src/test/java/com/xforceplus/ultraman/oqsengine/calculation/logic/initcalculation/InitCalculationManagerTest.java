package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationAbstractParticipant;
import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InitCalculationManagerTest {
    private DefaultInitCalculationManager manager;
    static long baseClassId = Long.MAX_VALUE;
    private MockMetaManager mockMetaManager;
    private MemoryKeyValueStorage kv;
    private static final String INIT_FLAG = "calculationInitField-";
    private SerializeStrategy serializeStrategy;

    static IEntityField A1_FIELD = EntityField.Builder.anEntityField()
            .withId(1)
            .withName("A1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();

    static IEntityField A2_FIELD = EntityField.Builder.anEntityField()
            .withId(2)
            .withName("A2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();

    static IEntityField A3_FIELD = EntityField.Builder.anEntityField()
            .withId(3)
            .withName("A3")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Formula.Builder.anFormula()
                            .withArgs(Stream.of("A1", "A2").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityField A4_FIELD = EntityField.Builder.anEntityField()
            .withId(4)
            .withName("A4")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(AutoFill.Builder.anAutoFill()
                            .withArgs(Stream.of("A1", "A3").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityField A5_FIELD = EntityField.Builder.anEntityField()
            .withId(105)
            .withName("A5")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(AutoFill.Builder.anAutoFill()
                            .withArgs(Stream.of("A4", "A3").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityField A6_FIELD = EntityField.Builder.anEntityField()
            .withId(106)
            .withName("A6")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(AutoFill.Builder.anAutoFill()
                            .withArgs(Stream.of("A4", "A3").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityClass A_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId)
            .withCode("a")
            .withLevel(0)
            .withFields(Stream.of(A1_FIELD, A2_FIELD, A3_FIELD, A4_FIELD, A5_FIELD, A6_FIELD).collect(Collectors.toList()))
            .build();


    static IEntityField B1_FIELD = EntityField.Builder.anEntityField()
            .withId(5)
            .withName("B1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId).withFieldId(4).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityField B2_FIELD = EntityField.Builder.anEntityField()
            .withId(502)
            .withName("B2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId).withFieldId(3).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityClass B_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 1)
            .withCode("b")
            .withLevel(0)
            .withFields(Stream.of(B2_FIELD, B1_FIELD).collect(Collectors.toList()))
            .build();

    static IEntityField C1_FIELD = EntityField.Builder.anEntityField()
            .withId(6)
            .withName("C1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withFieldId(5).withClassId(baseClassId - 1).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityField C2_FIELD = EntityField.Builder.anEntityField()
            .withId(7)
            .withName("C2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();

    static IEntityField C3_FIELD = EntityField.Builder.anEntityField()
            .withId(8)
            .withName("C3")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Formula.Builder.anFormula()
                            .withArgs(Stream.of("C1", "C2").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityField C4_FIELD = EntityField.Builder.anEntityField()
            .withId(9)
            .withName("C4")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(AutoFill.Builder.anAutoFill()
                            .withArgs(Stream.of("C1", "C3").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityField C5_FIELD = EntityField.Builder.anEntityField()
            .withId(205)
            .withName("C5")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withFieldId(5).withClassId(baseClassId - 1).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityClass C_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 2)
            .withCode("c")
            .withLevel(0)
            .withFields(Stream.of(C1_FIELD, C2_FIELD, C3_FIELD, C4_FIELD, C5_FIELD).collect(Collectors.toList()))
            .build();


    static IEntityField D1_FIELD = EntityField.Builder.anEntityField()
            .withId(10)
            .withName("D1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();


    static IEntityClass D_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 3)
            .withCode("d")
            .withLevel(0)
            .withField(D1_FIELD)
            .build();


    static IEntityField E1_FIELD = EntityField.Builder.anEntityField()
            .withId(11)
            .withName("E1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();


    static IEntityClass E_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 4)
            .withCode("e")
            .withLevel(0)
            .withField(E1_FIELD)
            .build();


    static IEntityField F1_FIELD = EntityField.Builder.anEntityField()
            .withId(12)
            .withName("F1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 4).withFieldId(11).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityField F2_FIELD = EntityField.Builder.anEntityField()
            .withId(302)
            .withName("F2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 5).withFieldId(12).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityClass F_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 5)
            .withCode("f")
            .withLevel(0)
            .withFields(Stream.of(F2_FIELD, F1_FIELD).collect(Collectors.toList()))
            .build();


    static IEntityField G1_FIELD = EntityField.Builder.anEntityField()
            .withId(13)
            .withName("G1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 5).withFieldId(12).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityField G2_FIELD = EntityField.Builder.anEntityField()
            .withId(602)
            .withName("G2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 1).withFieldId(502).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityClass G_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 6)
            .withCode("g")
            .withLevel(0)
            .withFields(Stream.of(G2_FIELD, G1_FIELD).collect(Collectors.toList()))
            .build();

    static IEntityField H1_FIELD = EntityField.Builder.anEntityField()
            .withId(14)
            .withName("H1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 6).withFieldId(13).withAggregationType(AggregationType.SUM).build())
                    .build())
            .build();

    static IEntityField H2_FIELD = EntityField.Builder.anEntityField()
            .withId(15)
            .withName("H2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(AutoFill.Builder.anAutoFill().build())
                    .build())
            .build();

    static IEntityField H3_FIELD = EntityField.Builder.anEntityField()
            .withId(16)
            .withName("H3")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(baseClassId - 5).withFieldId(0).withAggregationType(AggregationType.COUNT).build())
                    .build())
            .build();

    static IEntityField H4_FIELD = EntityField.Builder.anEntityField()
            .withId(17)
            .withName("H4")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Formula.Builder.anFormula().withArgs(Stream.of("H1", "H2").collect(Collectors.toList())).build())
                    .build())
            .build();

    static IEntityClass H_CLASS = EntityClass.Builder.anEntityClass()
            .withId(baseClassId - 7)
            .withCode("h")
            .withLevel(0)
            .withField(H1_FIELD)
            .withField(H2_FIELD)
            .withField(H3_FIELD)
            .withField(H4_FIELD)
            .build();


    @BeforeEach
    public void before() throws Exception {
        manager = new DefaultInitCalculationManager();
        mockMetaManager = new MockMetaManager();
        mockMetaManager.setClasses(Stream.of(A_CLASS, B_CLASS, C_CLASS, D_CLASS,
                E_CLASS, F_CLASS, G_CLASS, H_CLASS).collect(Collectors.toList()));
        Field metaManager = DefaultInitCalculationManager.class.getDeclaredField("metaManager");
        metaManager.setAccessible(true);
        metaManager.set(manager, mockMetaManager);

        kv = new MemoryKeyValueStorage();
        Field kvField = DefaultInitCalculationManager.class.getDeclaredField("kv");
        kvField.setAccessible(true);
        kvField.set(manager, kv);

        serializeStrategy = new HessianSerializeStrategy();
        Field strategy = DefaultInitCalculationManager.class.getDeclaredField("serializeStrategy");
        strategy.setAccessible(true);
        strategy.set(manager, serializeStrategy);

    }

    @Test
    public void getParticipant() {
        Collection<AbstractParticipant> abstractParticipant = manager.getParticipant(mockMetaManager.getClasses());
        Assertions.assertEquals(abstractParticipant.size(), 18);
    }

    @Test
    public void generateInfluence() {
        Collection<AbstractParticipant> abstractParticipant = manager.getParticipant(mockMetaManager.getClasses());
        List<Infuence> infuences = manager.generateInfluence(abstractParticipant);
        Assertions.assertEquals(infuences.size(), 4);
    }

    @Test
    public void getNeedInitParticipant() {
        Collection<AbstractParticipant> all = manager.getParticipant(mockMetaManager.getClasses());
        kv.save(INIT_FLAG + 4, serializeStrategy.serialize(CalculationInitStatus.UN_INIT));
        kv.save(INIT_FLAG + A3_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + B2_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + G2_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + F1_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + G1_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + F2_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        kv.save(INIT_FLAG + H1_FIELD.id(), serializeStrategy.serialize(CalculationInitStatus.INIT_DONE));
        Set<AbstractParticipant> needInitAbstractParticipant = manager.getNeedInitParticipant(all);
        InitCalculationInfo info = InitCalculationInfo.Builder.anEmptyBuilder().withAll(all).withNeed(needInitAbstractParticipant).withInfuences(manager.generateInfluence(all)).build();
        Assertions.assertEquals(needInitAbstractParticipant.size(), 11);
        while (!manager.isComplete(info)) {
            Map<IEntityClass, HashSet<AbstractParticipant>> candidate = manager.voteCandidate(info);
            Collection<AbstractParticipant> abstractParticipants = manager.voteRun(info);
            ArrayList<Map<IEntityClass, Collection<InitCalculationAbstractParticipant>>> map = manager.sortRun(abstractParticipants, info);
        }
        Assertions.assertTrue(info.getNeed().isEmpty());
    }

    @Test
    public void generateAppInfo() {
        Collection<AbstractParticipant> all = manager.getParticipant(mockMetaManager.getClasses());
        InitCalculationInfo info = InitCalculationInfo.Builder.anEmptyBuilder().withAll(all).withNeed(new ArrayList<>(all)).withInfuences(manager.generateInfluence(all)).build();

        while (!manager.isComplete(info)) {
            Map<IEntityClass, HashSet<AbstractParticipant>> candidate = manager.voteCandidate(info);
            Collection<AbstractParticipant> abstractParticipants = manager.voteRun(info);
            ArrayList<Map<IEntityClass, Collection<InitCalculationAbstractParticipant>>> map = manager.sortRun(abstractParticipants, info);
        }

        Assertions.assertEquals(info.getAll(), all);
        Assertions.assertTrue(info.getNeed().isEmpty());
    }

    static class MockMetaManager implements MetaManager {
        private Collection<IEntityClass> classes;

        public void setClasses(Collection<IEntityClass> entityClasses) {
            this.classes = entityClasses;
        }

        public Collection<IEntityClass> getClasses() {
            return this.classes;
        }

        @Override
        public Optional<IEntityClass> load(long id, String profile) {
            return classes.stream().filter(e -> e.id() == id).findFirst();
        }

        @Override
        public Optional<IEntityClass> load(EntityClassRef ref) {
            return MetaManager.super.load(ref);
        }

        @Override
        public Optional<IEntityClass> load(long entityClassId, int version, String profile) {
            return Optional.empty();
        }

        @Override
        public Collection<IEntityClass> withProfilesLoad(long entityClassId) {
            return null;
        }

        @Override
        public int need(String appId, String env) {
            return 0;
        }

        @Override
        public void invalidateLocal() {

        }

        @Override
        public boolean metaImport(String appId, String env, int version, String content) {
            return false;
        }

        @Override
        public Optional<MetaMetrics> showMeta(String appId) throws Exception {
            return Optional.empty();
        }

        @Override
        public Collection<MetricsLog> metaLogs(MetricsLog.ShowType showType) {
            return MetaManager.super.metaLogs(showType);
        }

        @Override
        public int reset(String appId, String env) {
            return 0;
        }

        @Override
        public boolean remove(String appId) {
            return false;
        }
    }
}