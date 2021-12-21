package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationAbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 计算字段计算器测试. 测试构造如下的一个情况.
 * A
 * /     \
 * B(sum)  C(lookup)
 * /
 * D(SUM)
 *
 * @author dongbin
 * @version 0.1 2021/10/14 10:26:24
 * @since 1.8
 */
public class DefaultCalculationImplTest {

    private static IEntityField A_LONG = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 100)
        .withFieldType(FieldType.LONG)
        .withName("a-long").build();

    private static IEntityField B_SUM = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 200)
        .withFieldType(FieldType.LONG)
        .withName("b-sum-a")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(Aggregation.Builder.anAggregation().build()).build()
        )
        .build();

    private static IEntityField C_LOOKUP = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 300)
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(Lookup.Builder.anLookup().build()).build()
        )
        .withName("c-lookup-a").build();

    private static IEntityField D_SUM = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 400)
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(Aggregation.Builder.anAggregation().build()).build()
        )
        .withName("d-sum-a").build();

    private static IEntityClass A_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withCode("a-class")
        .withField(A_LONG).build();

    private static IEntityClass B_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withCode("b-class")
        .withField(B_SUM).build();

    private static IEntityClass C_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withCode("c-class")
        .withField(C_LOOKUP).build();

    private static IEntityClass D_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 3)
        .withCode("d-class")
        .withField(D_SUM).build();

    private IEntity entityA = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE)
        .withEntityClassRef(A_CLASS.ref())
        .withEntityValue(
            EntityValue.build().addValue(
                new LongValue(A_LONG, 100L)
            )
        ).build();

    private IEntity entityB = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 1)
        .withEntityClassRef(B_CLASS.ref())
        .withEntityValue(
            EntityValue.build().addValue(
                new LongValue(B_SUM, 100L)
            )
        ).build();

    private IEntity entityD = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 2)
        .withEntityClassRef(D_CLASS.ref())
        .withEntityValue(
            EntityValue.build().addValue(
                new LongValue(D_SUM, 100L)
            )
        ).build();

    private IEntity entityC = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 3)
        .withEntityClassRef(C_CLASS.ref())
        .withEntityValue(
            EntityValue.build().addValue(
                new LongValue(C_LOOKUP, 100L)
            )
        ).build();

    private MockLogic aggregationLogic;
    private MockLogic lookupLogic;

    private MockMetaManager metaManager;
    private MockMasterStorage masterStorage;
    private DefaultCalculationImpl calculation;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        calculation = new DefaultCalculationImpl();

        aggregationLogic = new MockLogic(CalculationType.AGGREGATION);
        Map<IEntityField, IValue> valueChange = new HashMap<>();
        valueChange.put(B_SUM, new LongValue(B_SUM, 200L));
        valueChange.put(D_SUM, new LongValue(D_SUM, 200L));
        aggregationLogic.setValueChanage(valueChange);
        aggregationLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
            CalculationScenarios.BUILD,
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        });
        Map<AbstractParticipant, AbstractParticipant> scope = new HashMap<>();
        scope.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build()
        );
        scope.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build()
        );
        aggregationLogic.setScope(scope);

        Map<AbstractParticipant, long[]> entityIds = new HashMap<>();
        entityIds.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
            new long[] {entityB.id()}
        );

        entityIds.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build(),
            new long[] {entityD.id()}
        );
        aggregationLogic.setEntityIds(entityIds);


        lookupLogic = new MockLogic(CalculationType.LOOKUP);
        valueChange = new HashMap<>();
        valueChange.put(C_LOOKUP, new LongValue(C_LOOKUP, 200L));
        lookupLogic.setValueChanage(valueChange);
        lookupLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        });
        scope = new HashMap<>();
        scope.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_LOOKUP).build()
        );
        lookupLogic.setScope(scope);

        entityIds = new HashMap<>();
        entityIds.put(
                CalculationAbstractParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_LOOKUP).build(),
            new long[] {entityC.id()}
        );
        lookupLogic.setEntityIds(entityIds);

        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        metaManager.addEntityClass(C_CLASS);
        metaManager.addEntityClass(D_CLASS);

        masterStorage = new MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB);
        masterStorage.addIEntity(entityC);
        masterStorage.addIEntity(entityD);

    }

    /**
     * 测试创建的时候那些没有改变,但是需要被改变的计算字段.
     */
    @Test
    public void testBuildNotChangeFieldButNeed() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory()).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(B_SUM.id()).get().valueToLong());
    }

    @Test
    public void testBuildCalculation() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory()).build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.focusEntity(entityC, C_CLASS);
        context.addValueChange(
            ValueChange.build(entityC.id(), new EmptyTypedValue(C_LOOKUP), new LongValue(C_LOOKUP, 200L)));

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(C_LOOKUP.id()).get().valueToLong());
    }

    @Test
    public void testReplaceCalculation() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory()).build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.focusEntity(entityC, C_CLASS);
        context.addValueChange(
            ValueChange.build(entityC.id(), new LongValue(C_LOOKUP, 100L), new LongValue(C_LOOKUP, 200L)));

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(C_LOOKUP.id()).get().valueToLong());
    }

    @Test
    public void testReplaceMaintenance() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(masterStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory()).build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.getCalculationLogicFactory().get().register(aggregationLogic);

        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L))
        );

        calculation.maintain(context);

        long[] replaceIds = masterStorage.getReplaceEntities().stream().mapToLong(e -> e.id()).sorted().toArray();

        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityA.id()) < 0,
            String.format("The target instance (%s) was not expected to be found, but it was.", "entityA"));

        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityB.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityB"));
        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityD.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityD"));
        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityC.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityC"));
    }

    /**
     * 测试持久化部份错误情况.
     * 执行批量持久化,发生错误会针对错误数据进行重试.
     * 这里测试是否进行了重试,同时重试的次数是否有上限.
     */
    @Test
    public void testPersistenceErrReplayMax() throws Exception {
        // entityB实例持久化会一直错误.因为这里指定了判断其是否等于entityB,实际更新的是entityA.
        masterStorage.setReplaceTest(e -> !(e.id() == entityB.id()));

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(masterStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory()).build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.getCalculationLogicFactory().get().register(aggregationLogic);

        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L))
        );

        calculation.maintain(context);

        // b 重试了多少次.
        long size = masterStorage.getReplaceEntities().stream()
            .filter(e -> e.id() == entityB.id())
            .count();

        Assertions.assertEquals(100, size);
    }

    static class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        private Predicate<IEntity> replaceTest;

        public void setReplaceTest(Predicate<IEntity> replaceTest) {
            this.replaceTest = replaceTest;
        }

        @Override
        public int[] replace(EntityPackage entityPackage) throws SQLException {
            try {
                if (replaceTest == null) {
                    return IntStream.range(0, entityPackage.size()).map(i -> 1).toArray();
                } else {
                    IEntity[] entities = entityPackage.stream().map(e -> e.getKey()).toArray(IEntity[]::new);
                    return Arrays.stream(entities).mapToInt(e -> replaceTest.test(e) ? 1 : 0).toArray();
                }
            } finally {
                entityPackage.stream().forEach(e -> replaceEntities.add(e.getKey()));
            }
        }

        public void addIEntity(IEntity entity) {
            entities.put(entity.id(), entity);
        }

        public List<IEntity> getReplaceEntities() {
            return replaceEntities;
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId, int size) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.ofNullable(entities.get(id));
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(entities.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
            return Arrays.stream(ids)
                .mapToObj(id -> Optional.of(entities.get(id)))
                .filter(e -> e.isPresent())
                .map(e -> e.get()).collect(Collectors.toList());
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
            return selectMultiple(ids);
        }

        @Override
        public boolean exist(long id) throws SQLException {
            return entities.containsKey(id);
        }
    }

    static class MockLogic implements CalculationLogic {

        // 支持的计算字段类型.
        private CalculationType type;
        // 需要维护的场景.
        private CalculationScenarios[] needMaintenanceScenarios;
        // 计算字段 key为请求计算的IValue实例, value为计算结果.
        private Map<IEntityField, IValue> valueChanage;
        // 指定一个参与者的影响实例id列表.
        private Map<AbstractParticipant, long[]> entityIds;
        // 需要增加的影响范围,当迭代树碰到和key相等的参与者时需要为其增加value影响.
        private Map<AbstractParticipant, AbstractParticipant> scope;

        public MockLogic(CalculationType type) {
            this.type = type;
        }

        public void setNeedMaintenanceScenarios(
            CalculationScenarios[] needMaintenanceScenarios) {
            this.needMaintenanceScenarios = needMaintenanceScenarios;
        }

        public void setValueChanage(
            Map<IEntityField, IValue> valueChanage) {
            this.valueChanage = valueChanage;
        }

        public void setEntityIds(
            Map<AbstractParticipant, long[]> entityIds) {
            this.entityIds = entityIds;
        }

        public void setScope(
            Map<AbstractParticipant, AbstractParticipant> scope) {
            this.scope = scope;
        }

        @Override
        public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
            return Optional.ofNullable(valueChanage.get(context.getFocusField()));
        }

        @Override
        public void scope(CalculationContext context, Infuence infuence) {
            infuence.scan((parentClassOp, participant, infuenceInner) -> {

                AbstractParticipant child = scope.get(participant);

                if (child != null) {
                    infuenceInner.impact(participant, child);
                }

                return InfuenceConsumer.Action.CONTINUE;
            });
        }

        @Override
        public long[] getMaintainTarget(CalculationContext context, AbstractParticipant abstractParticipant,
                                        Collection<IEntity> triggerEntities) throws CalculationException {
            long[] ids = entityIds.get(abstractParticipant);
            if (ids == null) {
                return new long[0];
            } else {
                return ids;
            }
        }

        @Override
        public CalculationScenarios[] needMaintenanceScenarios() {
            return needMaintenanceScenarios;
        }

        @Override
        public CalculationType supportType() {
            return type;
        }
    }
}