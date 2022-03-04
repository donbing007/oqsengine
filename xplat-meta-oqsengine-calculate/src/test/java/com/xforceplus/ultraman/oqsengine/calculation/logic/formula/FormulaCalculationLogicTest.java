package com.xforceplus.ultraman.oqsengine.calculation.logic.formula;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionExclusiveAction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试类.
 *                  A
 * /                                 \
 * B(sum) ——> B(Formula c(sum) * 2)  C(COUNT)
 * /            \
 * D(SUM)    D(SUM B(Formula c(sum) * 2))
 */
public class FormulaCalculationLogicTest {

    private FormulaCalculationLogic formulaCalculationLogic;

    private static IEntityField A_LONG;

    private static IEntityField B_ID;

    private static IEntityField B_REF;

    private static IEntityField B_SUM;

    private static IEntityField B_FML;

    private static IEntityField C_COUNT;

    private static IEntityField D_SUM;

    private static IEntityField D_SUM_B_FML;

    private static IEntityClass A_CLASS;

    private static IEntityClass B_CLASS;

    private static IEntityClass C_CLASS;

    private static IEntityClass D_CLASS;

    private IEntity entityA;

    private IEntity entityB;

    private IEntity entityD;

    private IEntity entityC;

    private FormulaCalculationLogicTest.MockLogic aggregationLogic;
    private FormulaCalculationLogicTest.MockLogic lookupLogic;

    private MockMetaManager metaManager;
    private FormulaCalculationLogicTest.MockMasterStorage masterStorage;
    private DefaultCalculationImpl calculation;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() {

        A_LONG = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.LONG)
            .withName("a-long").build();

        B_ID = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1000)
            .withFieldType(FieldType.LONG)
            .withName("id").build();

        B_REF = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 10)
            .withFieldType(FieldType.LONG)
            .withName("relb").build();

        B_SUM = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1)
            .withFieldType(FieldType.LONG)
            .withName("b-sum-a")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(Long.MAX_VALUE)
                        .withFieldId(Long.MAX_VALUE)
                        .withRelationId(Long.MAX_VALUE - 10)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .build();

        B_FML = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 11)
            .withFieldType(FieldType.LONG)
            .withName("b-fml-b-sum")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Formula.Builder.anFormula()
                        .withLevel(1)
                        .withExpression("${b-sum-a} * 2")
                        .withFailedDefaultValue(0)
                        .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                        .withArgs(Collections.singletonList("b-sum-a"))
                        .build()
                    ).build()
            )
            .build();

        C_COUNT = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 2)
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(Long.MAX_VALUE)
                        .withFieldId(Long.MAX_VALUE)
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withAggregationType(AggregationType.COUNT)
                        .build()
                    ).build()
            )
            .withName("c-count-a").build();

        D_SUM = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 3)
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(Long.MAX_VALUE - 1)
                        .withFieldId(Long.MAX_VALUE - 1)
                        .withRelationId(Long.MAX_VALUE - 30)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .withName("d-sum-a").build();

        D_SUM_B_FML = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 31)
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(Long.MAX_VALUE - 1)
                        .withFieldId(Long.MAX_VALUE - 11)
                        .withRelationId(Long.MAX_VALUE - 30)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .withName("d-sum-a").build();

        A_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withCode("a-class")
            .withFields(Arrays.asList(A_LONG, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 100)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("a-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withEntityField(B_REF)
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 200)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("a-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 2)
                    .withRightEntityClassLoader((id, a) -> Optional.of(C_CLASS))
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 10)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 20)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("c-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 2)
                    .withRightEntityClassLoader((id, a) -> Optional.of(C_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        B_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withCode("b-class")
            .withFields(Arrays.asList(B_SUM, B_FML, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 300)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 3)
                    .withRightEntityClassLoader((id, a) -> Optional.of(D_CLASS))
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 30)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("d-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 3)
                    .withRightEntityClassLoader((id, a) -> Optional.of(D_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 10)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE)
                    .withRightEntityClassLoader((id, a) -> Optional.of(A_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        C_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withCode("c-class")
            .withFields(Arrays.asList(C_COUNT, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 20)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 2)
                    .withLeftEntityClassCode("c-class")
                    .withRightEntityClassId(Long.MAX_VALUE)
                    .withRightEntityClassLoader((id, a) -> Optional.of(A_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        D_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 3)
            .withCode("d-class")
            .withFields(Arrays.asList(D_SUM, D_SUM_B_FML, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 30)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE - 3)
                    .withLeftEntityClassCode("d-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        entityA = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(new LongValue(A_LONG, 100L))
            .build();

        entityB = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_SUM, 100L))
            .build();

        entityD = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 2)
            .withEntityClassRef(D_CLASS.ref())
            .withValue(new LongValue(D_SUM, 100L))
            .build();

        entityC = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 3)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(new LongValue(C_COUNT, 100L))
            .build();

        calculation = new DefaultCalculationImpl();

        aggregationLogic = new FormulaCalculationLogicTest.MockLogic(CalculationType.AGGREGATION);

        Map<IEntityField, IValue> valueChange = new HashMap<>();
        valueChange.put(B_SUM, new LongValue(B_SUM, 200L));
        valueChange.put(D_SUM, new LongValue(D_SUM, 200L));
        valueChange.put(C_COUNT, new LongValue(C_COUNT, 200L));
        aggregationLogic.setValueChanage(valueChange);
        aggregationLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
            CalculationScenarios.BUILD,
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        });
        Map<Participant, Participant> scope = new HashMap<>();
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build()
        );
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_COUNT).build()
        );
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build()
        );
        aggregationLogic.setScope(scope);

        Map<Participant, AffectedInfo[]> entityIds = new HashMap<>();
        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
            new AffectedInfo[] {
                new AffectedInfo(entityB, entityB.id())
            }
        );

        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_COUNT).build(),
            new AffectedInfo[] {
                new AffectedInfo(entityC, entityC.id())
            }
        );

        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build(),
            new AffectedInfo[] {
                new AffectedInfo(entityD, entityD.id())
            }
        );
        aggregationLogic.setEntityIds(entityIds);

        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        metaManager.addEntityClass(C_CLASS);
        metaManager.addEntityClass(D_CLASS);

        masterStorage = new FormulaCalculationLogicTest.MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB);
        masterStorage.addIEntity(entityC);
        masterStorage.addIEntity(entityD);

        formulaCalculationLogic = new FormulaCalculationLogic();
    }

    @Test
    public void testBuildCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .withScenarios(CalculationScenarios.BUILD).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_FML);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = formulaCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testReplaceCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .withScenarios(CalculationScenarios.REPLACE).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_FML);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new LongValue(A_LONG, 100L), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = formulaCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testRemoveCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .withScenarios(CalculationScenarios.DELETE).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_FML);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new LongValue(A_LONG, 10L), new EmptyTypedValue(A_LONG)));

        Optional<IValue> targetValue = formulaCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void scope() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext().build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(
                new LongValue(B_SUM, 100)
            ).build();
        Infuence infuence = new Infuence(
            targetEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(B_SUM, 50L),
                new LongValue(B_SUM, 100L))
        );

        context.focusEntity(targetEntity, B_CLASS);
        context.focusField(B_SUM);

        formulaCalculationLogic.scope(context, infuence);
        List<Participant> participants = new ArrayList<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            participants.add(participant);

            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(2, participants.size());
        Assertions.assertEquals(B_CLASS.id(), participants.get(0).getEntityClass().id());
        Assertions.assertEquals(B_CLASS.id(), participants.get(1).getEntityClass().id());
    }

    @Test
    public void getMaintainTarget() throws SQLException {
        FormulaCalculationLogicTest.MockTaskCoordinator coordinator =
            new FormulaCalculationLogicTest.MockTaskCoordinator();
        FormulaCalculationLogicTest.MockTransaction tx = new FormulaCalculationLogicTest.MockTransaction();

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .build();


        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_SUM, 100))
            .build();

        Infuence infuence = new Infuence(
            targetEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(B_SUM, 50L),
                new LongValue(B_SUM, 100L))
        );

        context.focusEntity(targetEntity, B_CLASS);
        context.focusField(B_SUM);

        formulaCalculationLogic.scope(context, infuence);
        AtomicReference<Participant> p = new AtomicReference<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {
            if (parentParticipant.isPresent()) {
                if (parentParticipant.get().getEntityClass().id() == B_CLASS.id()) {
                    p.set(participant);
                    return InfuenceConsumer.Action.OVER;
                }
            }
            return InfuenceConsumer.Action.CONTINUE;
        });

        Participant participant = p.get();
        Collection<AffectedInfo> affectedInfos =
            formulaCalculationLogic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(1, affectedInfos.size());
    }

    static class MockMasterStorage implements MasterStorage {

        private Map<Long, com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        @Override
        public void replace(EntityPackage entityPackage) throws SQLException {
            entityPackage.stream().forEach(e -> {
                replaceEntities.add(e.getKey());
                e.getKey().neat();
            });
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
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId) throws SQLException {
            return null;
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId, int size) throws SQLException {
            return null;
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
        private Map<Participant, AffectedInfo[]> entityIds;
        // 需要增加的影响范围,当迭代树碰到和key相等的参与者时需要为其增加value影响.
        private Map<Participant, Participant> scope;

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
            Map<Participant, AffectedInfo[]> entityIds) {
            this.entityIds = entityIds;
        }

        public void setScope(
            Map<Participant, Participant> scope) {
            this.scope = scope;
        }

        @Override
        public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
            return Optional.ofNullable(valueChanage.get(context.getFocusField()));
        }

        @Override
        public void scope(CalculationContext context, Infuence infuence) {
            infuence.scan((parentClassOp, participant, infuenceInner) -> {

                Participant child = scope.get(participant);

                if (child != null) {
                    infuenceInner.impact(participant, child);
                }

                return InfuenceConsumer.Action.CONTINUE;
            });
        }

        @Override
        public Collection<AffectedInfo> getMaintainTarget(CalculationContext context, Participant participant,
                                                          Collection<IEntity> triggerEntities)
            throws CalculationException {
            AffectedInfo[] infos = entityIds.get(participant);
            if (infos == null) {
                return Collections.emptyList();
            } else {
                return Arrays.stream(infos).collect(Collectors.toList());
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

    private static class MockTaskCoordinator implements TaskCoordinator {

        private List<Task> tasks = new ArrayList<>();

        public List<Task> getTasks() {
            return tasks;
        }

        @Override
        public boolean registerRunner(TaskRunner runner) {
            return false;
        }

        @Override
        public Optional<TaskRunner> getRunner(Class clazz) {
            return Optional.empty();
        }

        @Override
        public boolean addTask(Task task) {
            tasks.add(task);
            return true;
        }
    }

    private static class MockTransaction implements Transaction {

        private List<Consumer<Transaction>> commitHooks = new ArrayList<>();

        @Override
        public long id() {
            return Long.MAX_VALUE;
        }

        @Override
        public Optional<String> message() {
            return Optional.empty();
        }

        @Override
        public void commit() throws SQLException {
            commitHooks.forEach(h -> {
                h.accept(this);
            });
        }

        @Override
        public void rollback() throws SQLException {

        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public boolean isRollback() {
            return false;
        }

        @Override
        public boolean isCompleted() {
            return false;
        }

        @Override
        public void join(TransactionResource transactionResource) throws SQLException {

        }

        @Override
        public Optional<TransactionResource> queryTransactionResource(String key) {
            return Optional.empty();
        }

        @Override
        public Collection<TransactionResource> listTransactionResource(TransactionResourceType type) {
            return null;
        }

        @Override
        public long attachment() {
            return 0;
        }

        @Override
        public void attach(long id) {

        }

        @Override
        public boolean isReadyOnly() {
            return false;
        }

        @Override
        public void focusNotReadOnly() {

        }

        @Override
        public TransactionAccumulator getAccumulator() {
            return null;
        }

        @Override
        public void exclusiveAction(TransactionExclusiveAction action) throws SQLException {

        }

        @Override
        public void registerCommitHook(Consumer<Transaction> hook) {
            this.commitHooks.add(hook);
        }

        @Override
        public void registerRollbackHook(Consumer<Transaction> hook) {

        }
    }

}
