package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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


    private KeyValueStorage kv;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        kv = new MemoryKeyValueStorage();

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
                        .withRightFamilyEntityClassLoader(id -> Arrays.asList(strongLookupEntityClass))
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
                        .withRightFamilyEntityClassLoader(id -> Arrays.asList(weakLookupEntityClass))
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
                        .withRightFamilyEntityClassLoader(id -> Arrays.asList(targetEntityClass))
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
                        .withRightFamilyEntityClassLoader(id -> Arrays.asList(targetEntityClass))
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
        kv = null;
    }

    @Test
    public void testScope() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext().build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new LongValue(targetLongField, 100)
                )
            ).build();
        Infuence infuence = new Infuence(
            targetEntity,
            Participant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetLongField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(targetLongField, 50L),
                new LongValue(targetLongField, 100L))
        );

        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetLongField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        logic.scope(context, infuence);

        List<Participant> participants = new ArrayList<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            participants.add(participant);

            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(2, participants.size());
        Assertions.assertEquals(targetClassId, participants.get(0).getEntityClass().id());
        Assertions.assertEquals(weakLookupClassId, participants.get(1).getEntityClass().id());
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
            .withTaskCoordinator(coordinator)
            .build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new LongValue(targetLongField, 100)
                )
            ).build();
        Infuence infuence = new Infuence(
            targetEntity,
            Participant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetLongField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(targetLongField, 50L),
                new LongValue(targetLongField, 100L))
        );

        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetLongField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        logic.scope(context, infuence);

        AtomicReference<Participant> p = new AtomicReference<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {
            if (parentParticipant.isPresent()) {
                if (parentParticipant.get().getEntityClass().id() == targetClassId) {
                    p.set(participant);
                    return InfuenceConsumer.Action.OVER;
                }
            }
            return InfuenceConsumer.Action.CONTINUE;
        });


        Participant participant = p.get();
        long[] ids = logic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(0, ids.length);

        tx.commit();

        List<Task> tasks = coordinator.getTasks();
        Assertions.assertEquals(1, tasks.size());

        LookupMaintainingTask lookTask = (LookupMaintainingTask) tasks.get(0);

        LookupHelper.LookupLinkIterKey lookupLinkIterKey = LookupHelper.buildIteratorPrefixLinkKey(
            context.getFocusField(), participant.getEntityClass(), participant.getField(),
            context.getFocusEntity());
        Assertions.assertEquals(lookupLinkIterKey.toString(), lookTask.getIterKey());
        Assertions.assertNull(lookTask.getPointKey().orElse(null));
    }

    @Test
    public void testStrongRelationship() throws Exception {
        MockTaskCoordinator coordinator = new MockTaskCoordinator();
        MockTransaction tx = new MockTransaction();
        KeyValueStorage kv = new MemoryKeyValueStorage();


        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new StringValue(targetStringField, "v1")
                )
            ).build();

        // 比当前事务可处理上限多1.
        for (int i = 0; i < 1001; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withId(Integer.MAX_VALUE - i)
                .withEntityClassRef(strongLookupEntityClass.ref())
                .build();
            String key =
                LookupHelper.buildLookupLinkKey(targetEntity, targetStringField, lookupEntity, strongStringLookupField)
                    .toString();
            kv.save(key, null);
        }

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .withKeyValueStorage(kv)
            .build();
        context.focusEntity(targetEntity, targetEntityClass);
        context.focusField(targetStringField);

        LookupCalculationLogic logic = new LookupCalculationLogic();

        Infuence infuence = new Infuence(
            targetEntity,
            Participant.Builder.anParticipant()
                .withEntityClass(targetEntityClass)
                .withField(targetStringField)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new StringValue(targetStringField, "v0"),
                new StringValue(targetStringField, "v1")
            )
        );

        logic.scope(context, infuence);

        AtomicReference<Participant> p = new AtomicReference<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {
            if (participant.getEntityClass().id() == strongLookupClassId) {
                p.set(participant);
                return InfuenceConsumer.Action.OVER;
            }
            return InfuenceConsumer.Action.CONTINUE;
        });


        Participant participant = p.get();
        long[] ids = logic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(1000, ids.length);

        tx.commit();

        List<Task> tasks = coordinator.getTasks();
        Assertions.assertEquals(1, tasks.size());

        LookupMaintainingTask lookTask = (LookupMaintainingTask) tasks.get(0);

        String startKey =
            "lookup-tf9223372036854775806-lc9223372036854775806-lp-lf9223372036854775803-te9223372036854775807-le0000000002147483646";
        Assertions.assertEquals(startKey, lookTask.getPointKey().get());
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