package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraphConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionExclusiveAction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.hint.TransactionHint;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lookup 字段计算逻辑.
 *
 * @author dongbin
 * @version 0.1 2021/08/02 15:49
 * @since 1.8
 */
public class LookupCalculationLogicTest {

    final Logger logger = LoggerFactory.getLogger(LookupCalculationLogicTest.class);

    private static ExecutorService TASK_POOL;

    private long targetClassId = Long.MAX_VALUE;
    private long strongLookupClassId = Long.MAX_VALUE - 1;
    private long weakLookupClassId = Long.MAX_VALUE - 2;
    private long fieldId = Long.MAX_VALUE;

    private IEntityClass targetEntityClass;
    private IEntityClass strongLookupEntityClass;
    private IEntityClass weakLookupEntityClass;

    // 目标对象.
    private IEntityField targetLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("target-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField targetStringField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("target-string")
        .withConfig(FieldConfig.build().searchable(true)).build();


    // 强关系对象.
    private IEntityField strongLookLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("strong-long")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                StaticCalculation.Builder.anStaticCalculation().build()
            ).build()
        ).build();
    private IEntityField strongLookStringField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("strong-string")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                StaticCalculation.Builder.anStaticCalculation().build()
            ).build()
        ).build();
    private IEntityField strongStringLookupField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("strong-string-lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withClassId(targetClassId)
                        .withFieldId(targetStringField.id()).build()
                )
                .build()
        ).build();

    // 弱关系对象.
    private IEntityField weakLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("weak-look-long")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(StaticCalculation.Builder.anStaticCalculation().build()).build()
        ).build();
    private IEntityField weakLongLookupField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("weak-long-lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                Lookup.Builder.anLookup()
                    .withFieldId(targetLongField.id())
                    .withClassId(targetClassId).build()

            ).build()
        ).build();

    /**
     * 全局初始化.
     */
    @BeforeAll
    public static void beforeAll() throws Exception {
        TASK_POOL = new ThreadPoolExecutor(3, 3,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1000),
            ExecutorHelper.buildNameThreadFactory("task", false),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @AfterAll
    public static void afterAll() throws Exception {
        ExecutorHelper.shutdownAndAwaitTermination(TASK_POOL);
    }

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        targetEntityClass = EntityClass.Builder.anEntityClass()
            .withId(targetClassId)
            .withCode("targetClass")
            .withField(targetLongField)
            .withField(targetStringField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(targetClassId)
                        .withRightEntityClassId(strongLookupClassId)
                        .withRightEntityClassLoader((id, profile) -> Optional.of(strongLookupEntityClass))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(strongLookupEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(true)
                        .withStrong(true)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(1)
                        .withLeftEntityClassId(targetClassId)
                        .withRightEntityClassId(weakLookupClassId)
                        .withRightEntityClassLoader((id, profile) -> Optional.of(weakLookupEntityClass))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(weakLookupEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(true)
                        .withStrong(false)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            )
            .build();

        strongLookupEntityClass = EntityClass.Builder.anEntityClass()
            .withId(strongLookupClassId)
            .withLevel(0)
            .withCode("strongLookupClass")
            .withField(strongLookLongField)
            .withField(strongLookStringField)
            .withField(strongStringLookupField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(strongLookupClassId)
                        .withRightEntityClassId(targetClassId)
                        .withRightEntityClassLoader((id, profile) -> Optional.of(targetEntityClass))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(targetEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            ).build();

        weakLookupEntityClass = EntityClass.Builder.anEntityClass()
            .withId(weakLookupClassId)
            .withLevel(0)
            .withCode("weakLookupClass")
            .withField(weakLongField)
            .withField(weakLongLookupField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(weakLookupClassId)
                        .withRightEntityClassId(targetClassId)
                        .withRightEntityClassLoader((id, profile) -> Optional.of(targetEntityClass))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(targetEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(false)
                        .withStrong(false)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            ).build();
    }

    @AfterEach
    public void after() throws Exception {
    }

    @Test
    public void testScope() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext().build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withValue(
                new LongValue(targetLongField, 100)
            ).build();
        InfuenceGraph infuence = new InfuenceGraph(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(EntityField.ILLUSORY_FIELD)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        infuence.impact(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetLongField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetLongField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        logic.scope(context, infuence);

        List<Participant> abstractParticipants = new ArrayList<>();
        infuence.scanNoSource((parentParticipant, participant, infuenceInner) -> {

            abstractParticipants.add(participant);

            return InfuenceGraphConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(2, abstractParticipants.size());
        Assertions.assertEquals(targetClassId, abstractParticipants.get(0).getEntityClass().id());
        Assertions.assertEquals(weakLookupClassId, abstractParticipants.get(1).getEntityClass().id());
    }

    /**
     * lookup目标字段值不null(不存在), 仍然会返回一个记录关系的IValue实例.<br>
     * 在目标被更新时,应该可能被触发.
     */
    @Test
    public void testNullLookup() throws Exception {
        MockTaskCoordinator coordinator = new MockTaskCoordinator();
        MockTransaction tx = new MockTransaction();
        MockConditionsSelectStorage conditionsSelectStorage = new MockConditionsSelectStorage();
        MockMetaManager metaManager = new MockMetaManager();
        metaManager.addEntityClass(targetEntityClass);
        metaManager.addEntityClass(weakLookupEntityClass);
        metaManager.addEntityClass(strongLookupEntityClass);

        /*
        lookup 目标为 targetStringField,但是没有给值.
        应该返回一个有附件的 EmptyTypeValue 实例.
         */
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withValue(
                new LongValue(targetLongField, 100L)
            ).build();

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.selectOne(targetEntity.id(), targetEntityClass)).thenReturn(Optional.of(targetEntity));

        IEntity lookupEntity = Entity.Builder.anEntity()
            .withEntityClassRef(strongLookupEntityClass.ref())
            .withValue(
                new LongValue(strongLookLongField, 1000L)
            )
            .withValue(
                new LookupValue(strongStringLookupField, targetEntity.id())
            ).build();

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .withTaskExecutorService(TASK_POOL)
            .withConditionsSelectStorage(conditionsSelectStorage)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .build();
        context.focusSourceEntity(lookupEntity);
        context.focusEntity(lookupEntity, strongLookupEntityClass);
        context.focusField(strongStringLookupField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> newValueOp = logic.calculate(context);
        Assertions.assertTrue(newValueOp.isPresent());
        Assertions.assertEquals(EmptyTypedValue.class, newValueOp.get().getClass());
        Assertions.assertEquals(targetEntity.id(), Long.parseLong((String) newValueOp.get().getAttachment().get()));
    }

    /**
     * lookup 一个存在值的字段.
     */
    @Test
    public void testNoNullLookup() throws Exception {
        MockTaskCoordinator coordinator = new MockTaskCoordinator();
        MockTransaction tx = new MockTransaction();
        MockConditionsSelectStorage conditionsSelectStorage = new MockConditionsSelectStorage();
        MockMetaManager metaManager = new MockMetaManager();
        metaManager.addEntityClass(targetEntityClass);
        metaManager.addEntityClass(weakLookupEntityClass);
        metaManager.addEntityClass(strongLookupEntityClass);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withValue(
                new LongValue(targetLongField, 100L)
            )
            .withValue(
                new StringsValue(targetStringField, "v1")
            )
            .build();

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.selectOne(targetEntity.id(), targetEntityClass)).thenReturn(Optional.of(targetEntity));

        IEntity lookupEntity = Entity.Builder.anEntity()
            .withEntityClassRef(strongLookupEntityClass.ref())
            .withValue(
                new LongValue(strongLookLongField, 1000L)
            )
            .withValue(
                new LookupValue(strongStringLookupField, targetEntity.id())
            ).build();

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .withTaskExecutorService(TASK_POOL)
            .withConditionsSelectStorage(conditionsSelectStorage)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .build();
        context.focusSourceEntity(lookupEntity);
        context.focusEntity(lookupEntity, strongLookupEntityClass);
        context.focusField(strongStringLookupField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> newValueOp = logic.calculate(context);
        Assertions.assertTrue(newValueOp.isPresent());
        IValue newValue = newValueOp.get();
        Assertions.assertEquals(StringsValue.class, newValue.getClass());
        Assertions.assertEquals(targetEntity.id(), Long.parseLong((String) newValue.getAttachment().get()));
        Assertions.assertEquals(
            targetEntity.entityValue().getValue("target-string").get().getValue(),
            newValue.getValue()
        );
    }

    /**
     * 弱关系的执行,预期直接在异步任务中增加任务.
     * 不在当前事务维护任何实例.
     */
    @Test
    public void testWeakRelationship() throws Exception {
        MockTaskCoordinator coordinator = new MockTaskCoordinator();
        MockTransaction tx = new MockTransaction();

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskExecutorService(TASK_POOL)
            .withTaskCoordinator(coordinator)
            .withCalculationLogicFactory(new CalculationLogicFactory()).build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withValue(
                new LongValue(targetLongField, 100)
            ).build();


        context.focusSourceEntity(targetEntity);
        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetLongField);

        InfuenceGraph graph = new InfuenceGraph(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(EntityField.ILLUSORY_FIELD)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        graph.impact(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetLongField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        LookupCalculationLogic logic = new LookupCalculationLogic();
        logic.scope(context, graph);

        AtomicReference<Participant> p = new AtomicReference<>();
        graph.scanNoSource((parentParticipants, participant, infuenceInner) -> {
            if (participant.getField().calculationType() == CalculationType.LOOKUP) {
                if (parentParticipants.stream().findFirst().get().getEntityClass().id() == targetClassId) {
                    p.set(participant);
                    return InfuenceGraphConsumer.Action.OVER;
                }
            }
            return InfuenceGraphConsumer.Action.CONTINUE;
        });


        Participant participant = p.get();
        Collection<AffectedInfo> affectedInfos =
            logic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(0, affectedInfos.size());

        tx.commit();

        List<Task> tasks = null;
        for (int i = 0; i < 10000; i++) {
            tasks = coordinator.getTasks();
            if (tasks.isEmpty()) {
                logger.info("No asynchronous task found, wait 1 second and try again.[{}/{}]", i, 10000);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            } else {
                break;
            }
        }
        Assertions.assertNotNull(tasks);
        Assertions.assertEquals(1, tasks.size());

        LookupMaintainingTask lookTask = (LookupMaintainingTask) tasks.get(0);
        Assertions.assertEquals(0, lookTask.getLastStartLookupEntityId());
        Assertions.assertEquals(weakLookupEntityClass.ref(), lookTask.getLookupClassRef());
        Assertions.assertEquals(weakLongLookupField.id(), lookTask.getLookupFieldId());
    }

    @Test
    public void testStrongRelationship() throws Exception {
        MockTaskCoordinator coordinator = new MockTaskCoordinator();
        MockTransaction tx = new MockTransaction();
        MockConditionsSelectStorage conditionsSelectStorage = new MockConditionsSelectStorage();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withValue(
                new StringValue(targetStringField, "v1")
            ).build();

        // 准备超出事务内可处理的极限数量的实例.
        List<EntityRef> refs = IntStream.range(0, 10000)
            .mapToObj(i -> EntityRef.Builder.anEntityRef().withId(Long.MAX_VALUE - i).build())
            .collect(Collectors.toList());
        conditionsSelectStorage.put(strongLookupEntityClass, refs);

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .withTaskExecutorService(TASK_POOL)
            .withConditionsSelectStorage(conditionsSelectStorage)
            .build();
        context.focusSourceEntity(targetEntity);
        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetStringField);

        LookupCalculationLogic logic = new LookupCalculationLogic();

        InfuenceGraph infuence = new InfuenceGraph(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(EntityField.ILLUSORY_FIELD)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        infuence.impact(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetStringField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build()
        );

        logic.scope(context, infuence);

        AtomicReference<Participant> p = new AtomicReference<>();
        infuence.scanNoSource((parentParticipant, participant, infuenceInner) -> {
            if (participant.getEntityClass().id() == strongLookupClassId) {
                p.set(participant);
                return InfuenceGraphConsumer.Action.OVER;
            }
            return InfuenceGraphConsumer.Action.CONTINUE;
        });


        Participant participant = p.get();
        Collection<AffectedInfo> affectedInfos = logic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(1000, affectedInfos.size());

        tx.commit();

        List<Task> tasks = null;
        for (int i = 1; i <= 10000; i++) {
            tasks = coordinator.getTasks();
            if (tasks.isEmpty()) {
                logger.info("No asynchronous task found, wait 1 second and try again.[{}/{}]", i, 10000);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            } else {
                break;
            }
        }
        Assertions.assertNotNull(tasks);
        Assertions.assertEquals(1, tasks.size());

        LookupMaintainingTask lookTask = (LookupMaintainingTask) tasks.get(0);

        // 生成的时候id是降序的,但是mock的排序是从升序的,所以任务的开始是从尾部减1000开始的.
        Assertions.assertEquals(refs.get(refs.size() - 1000).getId(), lookTask.getLastStartLookupEntityId());
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

    private static class MockConditionsSelectStorage implements ConditionsSelectStorage {

        private Map<IEntityClass, List<EntityRef>> dataPool = new HashMap();

        public void put(IEntityClass entityClass, List<EntityRef> refs) {
            List<EntityRef> sortRefs = new ArrayList<>(refs);
            Collections.sort(sortRefs);

            dataPool.put(entityClass, sortRefs);
        }


        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            long startId = conditions.collectCondition().stream()
                .filter(c -> c.getField().config().isIdentifie()).findFirst().get().getFirstValue().valueToLong();

            long size = config.getPage().getPageSize();
            List<EntityRef> refs = dataPool.get(entityClass);
            int index = Collections.binarySearch(refs, EntityRef.Builder.anEntityRef().withId(startId).build());
            if (index < 0) {
                // 没有找到,从头开始迭代.
                if (size > refs.size()) {
                    return refs.subList(0, refs.size());
                } else {
                    return refs.subList(0, ((int) size));
                }
            } else {
                if (index + size > refs.size()) {
                    return refs.subList(index, refs.size());
                } else {
                    return refs.subList(index, (int) size);
                }
            }

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
        public TransactionHint getHint() {
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