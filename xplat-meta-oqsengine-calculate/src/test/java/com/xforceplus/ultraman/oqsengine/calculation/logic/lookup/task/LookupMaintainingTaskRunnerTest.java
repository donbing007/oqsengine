package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.task.DefaultTaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.queue.MemoryTaskKeyQueue;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lookup同步运行者测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/17 10:39
 * @since 1.8
 */
public class LookupMaintainingTaskRunnerTest {

    final Logger logger = LoggerFactory.getLogger(LookupMaintainingTaskRunnerTest.class);

    private long targetClassId = 100;
    private long lookupClassId = 200;

    private long targetField0Id = 1000;
    private long targetField1Id = 1001;

    private long lookupField0Id = 2000;
    private long lookupField1Id = 2001;

    private IEntityField lookupField0 = EntityField.Builder.anEntityField()
        .withId(lookupField0Id)
        .withFieldType(FieldType.STRING)
        .withName("lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withFieldId(targetField0Id)
                        .withClassId(targetClassId).build()
                ).build()
        ).build();

    private IEntityField lookupField1 = EntityField.Builder.anEntityField()
        .withId(lookupField1Id)
        .withFieldType(FieldType.LONG)
        .withName("lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withFieldId(targetField1Id)
                        .withClassId(targetClassId).build()
                ).build()
        ).build();

    private IEntityClass lookupEntityClass = EntityClass.Builder.anEntityClass()
        .withId(lookupClassId)
        .withCode("lookup")
        .withField(lookupField0).withField(lookupField1)
        .withLevel(0)
        .withRelations(
            Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE)
                    .withCode(Long.toString(Long.MAX_VALUE))
                    .withLeftEntityClassId(targetClassId)
                    .withRightEntityClassId(lookupClassId)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .withBelongToOwner(false)
                    .withStrong(true)
                    .withIdentity(true)
                    .build()
            )
        ).build();

    private IEntityField targetField0 = EntityField.Builder.anEntityField()
        .withId(targetField0Id)
        .withFieldType(FieldType.STRING)
        .withName("target-field-0")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withLen(100)
                .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                .build()
        ).build();

    private IEntityField targetField1 = EntityField.Builder.anEntityField()
        .withId(targetField1Id)
        .withFieldType(FieldType.LONG)
        .withName("target-field-1")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withLen(300)
                .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                .build()
        ).build();

    private IEntityClass targetEntityClass = EntityClass.Builder.anEntityClass()
        .withId(targetClassId)
        .withCode("target")
        .withLevel(0)
        .withFields(
            Arrays.asList(
                targetField0, targetField1
            )
        )
        .withRelations(
            Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 1)
                    .withCode(Long.toString(Long.MAX_VALUE))
                    .withLeftEntityClassId(targetClassId)
                    .withRightEntityClassId(lookupClassId)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .withStrong(true)
                    .withBelongToOwner(false)
                    .withIdentity(true)
                    .build()
            )
        )
        .build();

    private MockMasterStorage masterStorage;
    private MockConditionsSelectStorage conditionsSelectStorage;
    private MockMetaManager metaManager;
    private DefaultTaskCoordinator coordinator;
    private LookupMaintainingTaskRunner runner;
    private ExecutorService worker;

    private long targetEntityId;
    private long[] lookupEntityIds;


    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        conditionsSelectStorage = new MockConditionsSelectStorage();
        masterStorage = new MockMasterStorage();
        metaManager = new MockMetaManager();

        runner = new LookupMaintainingTaskRunner(false);
        Field field = LookupMaintainingTaskRunner.class.getDeclaredField("conditionsSelectStorage");
        field.setAccessible(true);
        field.set(runner, conditionsSelectStorage);

        field = LookupMaintainingTaskRunner.class.getDeclaredField("masterStorage");
        field.setAccessible(true);
        field.set(runner, masterStorage);

        field = LookupMaintainingTaskRunner.class.getDeclaredField("metaManager");
        field.setAccessible(true);
        field.set(runner, metaManager);

        metaManager.addEntityClass(targetEntityClass);
        metaManager.addEntityClass(lookupEntityClass);

        worker = new ThreadPoolExecutor(
            10, 10, 100, TimeUnit.SECONDS, new ArrayBlockingQueue(1000)
        );
        coordinator = new DefaultTaskCoordinator();
        coordinator.setWorker(worker);

        field = DefaultTaskCoordinator.class.getDeclaredField("taskQueue");
        field.setAccessible(true);
        field.set(coordinator, new MemoryTaskKeyQueue());
        coordinator.init();
        coordinator.registerRunner(runner);
    }

    @AfterEach
    public void after() throws Exception {
        coordinator.destroy();
        ExecutorHelper.shutdownAndAwaitTermination(worker, 1L);
    }


    /**
     * 只会执行一次.
     */
    @Test
    public void testRunOnece() throws Exception {
        // 设定的默认执行上限是1000,所以这里设置10将不会往任务队列中增加任务.
        doTest(10);
    }

    @Test
    public void testRunRepeatedly() throws Exception {
        // 会增加第二个任务.
        doTest(1500);
    }

    // 实际测试任务,只有一个target,会有1-N个lookup实例.
    private void doTest(int lookupSize) throws Exception {
        buildDatas(lookupSize);
        IEntity targetEntity = masterStorage.selectOne(targetEntityId).get();
        targetEntity.entityValue().addValue(
            new StringValue(targetField0, "2")
        );
        masterStorage.replace(targetEntity, targetEntityClass);

        LookupMaintainingTask task =
            LookupMaintainingTask.Builder.anLookupMaintainingTask()
                .withTargetEntityId(targetEntity.id())
                .withTargetClassRef(targetEntityClass.ref())
                .withTargetFieldId(targetField0.id())
                .withLookupClassRef(lookupEntityClass.ref())
                .withLookupFieldId(lookupField0.id())
                .withLastStartLookupEntityId(0)
                .withMaxSize(1000)
                .build();

        runner.run(coordinator, task);

        // 等待所有lookup字段被更新.
        Collection<IEntity> newLookupEntities = null;
        boolean notUpdate = true;
        int okSize = 0;
        while (notUpdate) {
            newLookupEntities = masterStorage.selectMultiple(lookupEntityIds);
            okSize = 0;
            for (IEntity lookupEntity : newLookupEntities) {
                if (lookupEntity.version() == 0) {
                    notUpdate = true;

                    logger.info("Wait until all tasks are complete.[{}/{}]", okSize, newLookupEntities.size());

                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000L));

                    break;
                } else {
                    okSize++;
                    notUpdate = false;
                }
            }
        }
        logger.info("All entity update.[{}/{}]", okSize, newLookupEntities.size());

        for (IEntity le : newLookupEntities) {
            Assertions.assertEquals(
                targetEntity.entityValue().getValue(targetField0Id).get().getValue(),
                le.entityValue().getValue(lookupField0Id).get().getValue()
            );
        }
    }

    private void buildDatas(int lookupSize) throws Exception {

        IValue targetValue0 = new StringValue(targetField0, "1");
        IValue targetValue1 = new LongValue(targetField1, 100L);
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(10000)
            .withEntityClassRef(targetEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withVersion(0)
            .withValue(targetValue0)
            .withValue(targetValue1)
            .build();

        targetEntityId = targetEntity.id();
        masterStorage.build(targetEntity, targetEntityClass);

        long baseId = 20000;
        lookupEntityIds = new long[lookupSize];
        for (int i = 0; i < lookupSize; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withId(baseId++)
                .withEntityClassRef(lookupEntityClass.ref())
                .withTime(System.currentTimeMillis())
                .withVersion(0)
                .withValue(targetValue0.copy(lookupField0, Long.toString(targetEntity.id())))
                .withValue(targetValue1.copy(lookupField1, Long.toString(targetEntity.id())))
                .build();

            lookupEntityIds[i] = lookupEntity.id();
            masterStorage.build(lookupEntity, lookupEntityClass);
            conditionsSelectStorage.add(
                EntityRef.Builder.anEntityRef()
                    .withId(lookupEntity.id())
                    .build()
            );
        }
    }

    class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> data;

        public MockMasterStorage() {
            data = new ConcurrentHashMap<>();
        }

        public Map<Long, IEntity> getData() {
            return data;
        }

        @Override
        public boolean build(IEntity entity, IEntityClass entityClass) throws SQLException {
            data.put(entity.id(), entity);
            return true;
        }

        @Override
        public boolean replace(IEntity entity, IEntityClass entityClass) throws SQLException {
            if (data.containsKey(entity.id())) {
                entity.resetVersion(entity.version() + 1);
                data.put(entity.id(), entity);
                entity.neat();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void replace(EntityPackage entityPackage) throws SQLException {
            entityPackage.stream().forEach(entry -> {
                IEntity e = entry.getKey();
                if (data.containsKey(e.id())) {
                    e.resetVersion(e.version() + 1);
                    e.neat();
                }
            });
        }

        @Override
        public boolean delete(IEntity entity, IEntityClass entityClass) throws SQLException {
            if (data.remove(entity.id()) != null) {
                entity.delete();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
            return Arrays.stream(ids).mapToObj(id -> data.get(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int exist(long id) throws SQLException {
            IEntity entity = data.get(id);
            if (entity == null) {
                return -1;
            } else {
                return entity.version();
            }
        }
    }

    class MockConditionsSelectStorage implements ConditionsSelectStorage {

        private List<EntityRef> refs;

        public void add(EntityRef ref) {
            if (refs == null) {
                refs = new ArrayList<>();
            }

            refs.add(ref);
            Collections.sort(refs);
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            if (lookupEntityClass.id() == entityClass.id()) {

                long startId =
                    conditions.collectCondition().stream().filter(c -> c.getField().config().isIdentifie())
                        .findFirst().get().getFirstValue().valueToLong();

                int index = Collections.binarySearch(refs, EntityRef.Builder.anEntityRef().withId(startId).build());
                if (index < 0) {
                    long toSize = config.getPage().getPageSize();
                    toSize = Math.min(toSize, refs.size());
                    return refs.subList(0, (int) toSize);
                } else {
                    long toSize = index + config.getPage().getPageSize() + 1;
                    toSize = Math.min(toSize, refs.size());
                    return refs.subList(index + 1, (int) toSize);
                }
            }

            return Collections.emptyList();
        }
    }
}