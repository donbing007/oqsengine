package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.lang.reflect.Field;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class DefaultCalculationInitInstanceImplTest {
    private DefaultCalculationInitInstanceImpl defaultCalculationInitInstance;

    private MockMasterStorage masterStorage;

    private MockMetaManager metaManager;

    private MockCalculationInit calculationInit;


    private static enum FieldIndex {
        A_LONG,
        B_SUM_CONDITION,
        B_SUM,
        B_LONG,
        B_FORMULA1,
        B_FORMULA2
    }

    private static enum ClassIndex {
        A_CLASS,
        B_CLASS,
    }

    private static enum RelationIndex {
        B_A,
        A_B,
    }

    private static IEntityClass A_CLASS;

    private static IEntityClass B_CLASS;

    private static long getFieldId(FieldIndex fieldIndex) {
        // 避免和默认ID系统字段冲突,因为默认的EntityField.ID_ENTITY_FIELD的id是 Long.MAX_VALUE.
        return Long.MAX_VALUE - 1000 - fieldIndex.ordinal();
    }

    private static long getClassId(ClassIndex classIndex) {
        return Long.MAX_VALUE - classIndex.ordinal();
    }

    private static long getRelationIndex(RelationIndex relationIndex) {
        return Long.MAX_VALUE - relationIndex.ordinal();
    }



    // 被聚合
    private static IEntityField A_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.A_LONG))
            .withFieldType(FieldType.LONG)
            .withName("along").build();

    private static IEntityField B_SUM_CONDITION = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_SUM_CONDITION))
            .withFieldType(FieldType.LONG)
            .withName("bsumacondition")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Aggregation.Builder.anAggregation()
                                            .withAggregationType(AggregationType.SUM)
                                            .withRelationId(getRelationIndex(RelationIndex.B_A))
                                            .withClassId(getClassId(ClassIndex.A_CLASS))
                                            .withFieldId(getFieldId(FieldIndex.A_LONG))
                                            .withConditions(
                                                    Conditions.buildEmtpyConditions()
                                                            .addAnd(
                                                                    new Condition(
                                                                            A_LONG,
                                                                            ConditionOperator.NOT_EQUALS,
                                                                            new LongValue(A_LONG, 0L)
                                                                    )
                                                            )
                                            )
                                            .build()
                            ).build()
            )
            .build();

    private static IEntityField B_SUM = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_SUM))
            .withFieldType(FieldType.LONG)
            .withName("bsuma")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Aggregation.Builder.anAggregation()
                                            .withAggregationType(AggregationType.SUM)
                                            .withRelationId(getRelationIndex(RelationIndex.B_A))
                                            .withClassId(getClassId(ClassIndex.A_CLASS))
                                            .withFieldId(getFieldId(FieldIndex.A_LONG))
                                            .build()
                            ).build()
            )
            .build();

    private static IEntityField B_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_LONG))
            .withFieldType(FieldType.LONG)
            .withName("b-long").build();

    private static IEntityField B_FORMULA1 = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_FORMULA1))
            .withFieldType(FieldType.LONG)
            .withName("bformula1")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Formula.Builder.anFormula()
                                            .withLevel(0)
                                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                                            .withExpression("return ${b-long} + ${bsuma};")
                                            .withArgs(Arrays.asList("b-long", "bsuma"))
                                            .build()
                            ).build()
            ).build();


    private static IEntityField B_FORMULA2 = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_FORMULA2))
            .withFieldType(FieldType.LONG)
            .withName("bformula2")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Formula.Builder.anFormula()
                                            .withLevel(1)
                                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                                            .withExpression("return ${b-long} + ${bformula1};")
                                            .withArgs(Arrays.asList("b-long", "bformula1"))
                                            .build()
                            ).build()
            ).build();



    static {
        A_CLASS = EntityClass.Builder.anEntityClass()
                .withId(getClassId(ClassIndex.A_CLASS))
                .withCode("a-class")
                .withRelations(
                        Arrays.asList(
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.B_A))
                                        .withCode("b_a")
                                        .withBelongToOwner(true)
                                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withLeftEntityClassCode("a-class")
                                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(B_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(B_CLASS))
                                        .build(),
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.A_B))
                                        .withCode("a_b")
                                        .withBelongToOwner(true)
                                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withLeftEntityClassCode("a-class")
                                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(B_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(B_CLASS))
                                        .build()
                        )
                )
                .withField(A_LONG).build();


        B_CLASS = EntityClass.Builder.anEntityClass()
                .withId(getClassId(ClassIndex.B_CLASS))
                .withCode("b-class")
                .withField(B_SUM)
                .withField(B_LONG)
                .withField(B_SUM_CONDITION)
                .withField(B_FORMULA1)
                .withField(B_FORMULA2)
                .withRelations(
                        Arrays.asList(
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.B_A))
                                        .withCode("b_a")
                                        .withBelongToOwner(false)
                                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withLeftEntityClassCode("b-class")
                                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(A_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(A_CLASS))
                                        .build(),
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.A_B))
                                        .withCode("a_b")
                                        .withBelongToOwner(false)
                                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withLeftEntityClassCode("b-class")
                                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(A_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(A_CLASS))
                                        .build()
                        )
                ).build();


    }

    private IEntity entityA = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(new LongValue(A_LONG, 100L))
            .build();


    private IEntity entityB1 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_LONG, 100L))
            .build();


    private IEntity entityB2 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 2)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_LONG, 100L))
            .withValue(new LongValue(B_SUM, 100L))
            .build();


    private IEntity entityB3 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 3)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_SUM, 100L))
            .build();

    @BeforeEach
    public void before() throws Exception {
        defaultCalculationInitInstance = new DefaultCalculationInitInstanceImpl();
        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        masterStorage = new MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB1);
        masterStorage.addIEntity(entityB2);
        masterStorage.addIEntity(entityB3);
        calculationInit = new MockCalculationInit();
        calculationInit.addIEntity(entityB3);
        calculationInit.addIEntity(entityB2);
        calculationInit.addIEntity(entityB1);

        Field storage = DefaultCalculationInitInstanceImpl.class.getDeclaredField("masterStorage");
        storage.setAccessible(true);
        storage.set(defaultCalculationInitInstance, masterStorage);

        Field manager = DefaultCalculationInitInstanceImpl.class.getDeclaredField("metaManager");
        manager.setAccessible(true);
        manager.set(defaultCalculationInitInstance, metaManager);

        Field calculation = DefaultCalculationInitInstanceImpl.class.getDeclaredField("calculationInit");
        calculation.setAccessible(true);
        calculation.set(defaultCalculationInitInstance, calculationInit);

    }



    @Test
    void initInstance() {
        Optional<IEntity> entity = defaultCalculationInitInstance.initInstance(entityB2.id(), B_CLASS, false);
        Assertions.assertTrue(entity.isPresent());
        Assertions.assertEquals(99L, ((LongValue) entityB2.entityValue().getValue(B_SUM).get()).getValue());
    }

    @Test
    void initInstances() {
        List<IEntity> entities = defaultCalculationInitInstance.initInstances(Arrays.asList(entityB2.id(), entityB3.id()), B_CLASS, false, 200L);
        Assertions.assertEquals(2, entities.size());
    }

    @Test
    void initFields() {
        List<IEntity> entities = defaultCalculationInitInstance.initFields(Arrays.asList(entityB2.id(), entityB1.id()), B_CLASS, (List<IEntityField>) B_CLASS.fields(), false, 200L);
        Assertions.assertEquals(2, entities.size());
    }

    @Test
    void initField() {
        Optional<IEntity> entity = defaultCalculationInitInstance.initField(entityA.id(), A_CLASS, (List<IEntityField>) A_CLASS.fields(), false);
        Assertions.assertFalse(entity.isPresent());

        Optional<IEntity> entity1 = defaultCalculationInitInstance.initField(entityB1.id(), B_CLASS, (List<IEntityField>) B_CLASS.fields(), false);
        Assertions.assertTrue(entity1.isPresent());

    }

    @Test
    void initCheckFields() {

        List<ErrorCalculateInstance> errorCalculateInstance = defaultCalculationInitInstance.initCheckFields(Arrays.asList(entityB2.id(), entityB3.id()), B_CLASS, (List<IEntityField>) B_CLASS.fields(), 200L);

        Assertions.assertEquals(2, errorCalculateInstance.size());
    }

    @Test
    void initCheckField() {
        Optional<ErrorCalculateInstance> errorCalculateInstance = defaultCalculationInitInstance.initCheckField(entityA.id(), A_CLASS, (List<IEntityField>) A_CLASS.fields());
        Assertions.assertFalse(errorCalculateInstance.isPresent());

        Optional<ErrorCalculateInstance> errorCalculateInstance1 = defaultCalculationInitInstance.initCheckField(entityB1.id(), B_CLASS, (List<IEntityField>) B_CLASS.fields());
        Assertions.assertFalse(errorCalculateInstance1.isPresent());

        Optional<ErrorCalculateInstance> errorCalculateInstance2 = defaultCalculationInitInstance.initCheckField(entityB2.id(), B_CLASS, (List<IEntityField>) B_CLASS.fields());
        Assertions.assertTrue(errorCalculateInstance2.isPresent());

        Assertions.assertEquals(1, errorCalculateInstance2.get().getErrorFieldUnits().size());

        entityB2.entityValue().addValue(new LongValue(B_SUM_CONDITION, 100L));

        Optional<ErrorCalculateInstance> errorCalculateInstance3 = defaultCalculationInitInstance.initCheckField(entityB2.id(), B_CLASS, (List<IEntityField>) B_CLASS.fields());
        Assertions.assertTrue(errorCalculateInstance3.isPresent());

        Assertions.assertEquals(2, errorCalculateInstance3.get().getErrorFieldUnits().size());
    }

    @Test
    void initCheckInstance() {

        Optional<ErrorCalculateInstance> errorCalculateInstance1 = defaultCalculationInitInstance.initCheckInstance(entityB1.id(), B_CLASS);
        Assertions.assertFalse(errorCalculateInstance1.isPresent());

        Optional<ErrorCalculateInstance> errorCalculateInstance2 = defaultCalculationInitInstance.initCheckInstance(entityB2.id(), B_CLASS);
        Assertions.assertTrue(errorCalculateInstance2.isPresent());

        Assertions.assertEquals(1, errorCalculateInstance2.get().getErrorFieldUnits().size());

        entityB2.entityValue().addValue(new LongValue(B_SUM_CONDITION, 100L));

        Optional<ErrorCalculateInstance> errorCalculateInstance3 = defaultCalculationInitInstance.initCheckInstance(entityB2.id(), B_CLASS);
        Assertions.assertTrue(errorCalculateInstance3.isPresent());

        Assertions.assertEquals(2, errorCalculateInstance3.get().getErrorFieldUnits().size());
    }

    @Test
    void initCheckInstances() {
        List<ErrorCalculateInstance> errorCalculateInstance =
            defaultCalculationInitInstance.initCheckInstances(Arrays.asList(entityB2.id(), entityB3.id()), B_CLASS, 200L);

        Assertions.assertEquals(2, errorCalculateInstance.size());
    }


    static class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        private Predicate<IEntity> replaceTest;

        public void setReplaceTest(Predicate<IEntity> replaceTest) {
            this.replaceTest = replaceTest;
        }

        @Override
        public void replace(EntityPackage entityPackage) throws SQLException {
            if (replaceTest == null) {
                entityPackage.stream().forEach(e -> {
                    replaceEntities.add(e.getKey());
                    e.getKey().neat();
                });
            } else {
                entityPackage.stream().forEach(e -> {
                    replaceEntities.add(e.getKey());
                    if (replaceTest.test(e.getKey())) {
                        e.getKey().neat();
                    }
                });
            }
        }

        public void addIEntity(IEntity entity) {
            entities.put(entity.id(), entity);
        }

        public List<IEntity> getReplaceEntities() {
            return replaceEntities;
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
        public int exist(long id) throws SQLException {
            IEntity entity = entities.get(id);
            if (entity == null) {
                return -1;
            } else {
                return entity.version();
            }
        }

        @Override
        public DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                      long lastId, boolean useSelfClass) throws SQLException {
            return null;
        }

        @Override
        public DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                      long lastId, int size, boolean useSelfClass) throws SQLException {
            return null;
        }
    }

    static class MockCalculationInit implements CalculationInit {
        private Map<Long, IEntity> entities = new HashMap<>();

        public void addIEntity(IEntity entity) {
            entities.put(entity.id(), entity);
        }



        @Override
        public IEntity init(InitInstance initInstance) {
            if (entities.containsKey(initInstance.getEntity().id())) {
                for (IValue value : initInstance.getEntity().entityValue().values()) {
                    if (value instanceof LongValue) {
                        LongValue decrement = (LongValue) ((LongValue) value).decrement();
                        initInstance.getEntity().entityValue().addValue(decrement);
                    }
                }
            }
            return initInstance.getEntity();
        }

        @Override
        public List<IEntity> init(List<InitInstance> initInstances) {
            return initInstances.stream().map(this::init).collect(Collectors.toList());
        }
    }


}