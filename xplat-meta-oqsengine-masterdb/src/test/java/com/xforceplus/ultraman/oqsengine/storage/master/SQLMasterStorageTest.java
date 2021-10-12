package com.xforceplus.ultraman.oqsengine.storage.master;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SQLMasterStorage Tester.
 *
 * @author dongbin
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
public class SQLMasterStorageTest extends AbstractContainerExtends {

    private TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
    private SQLMasterStorage storage = MasterDBInitialization.getInstance().getMasterStorage();

    //-------------level 0--------------------
    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(1000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(1001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();
    private EntityClassRef l0EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l0EntityClass.id()).withEntityClassCode(l0EntityClass.code())
            .build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("l1-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("l1-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
        .build();
    private EntityClassRef l1EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code())
            .build();

    //-------------level 2--------------------
    private IEntityField l2LongField = EntityField.Builder.anEntityField()
        .withId(3000)
        .withFieldType(FieldType.LONG)
        .withName("l2-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(3001)
        .withFieldType(FieldType.STRING)
        .withName("l2-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(3)
        .withLevel(2)
        .withCode("l2")
        .withField(l2LongField)
        .withField(l2StringField)
        .withFather(l1EntityClass)
        .build();
    private EntityClassRef l2EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l2EntityClass.id()).withEntityClassCode(l2EntityClass.code())
            .build();

    private List<IEntity> expectedEntitys;

    public SQLMasterStorageTest() throws Exception {
    }

    /**
     * 每个测试初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(l2EntityClass));

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        expectedEntitys = initData(storage, 100);
        transactionManager.finish();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
    }

    /**
     * 测试写入并查询.
     */
    @Test
    public void testBuildEntity() throws Exception {

        LocalDateTime updateTime = LocalDateTime.now();
        IEntity newEntity = Entity.Builder.anEntity()
            .withId(100000)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100),
                new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                new LongValue(l1EntityClass.field("l1-long").get(), 200),
                new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                new DateTimeValue(EntityField.UPDATE_TIME_FILED, updateTime)
            )))
            .build();
        int size = storage.build(newEntity, l1EntityClass);
        Assertions.assertEquals(1, size);

        Optional<IEntity> entityOptional = storage.selectOne(newEntity.id(), l1EntityClass);
        Assertions.assertTrue(entityOptional.isPresent());
        IEntity targetEntity = entityOptional.get();
        Assertions.assertEquals(100, targetEntity.entityValue().getValue("l0-long").get().valueToLong());
        Assertions.assertEquals("l0value", targetEntity.entityValue().getValue("l0-string").get().valueToString());
        Assertions.assertEquals(200, targetEntity.entityValue().getValue("l1-long").get().valueToLong());
        Assertions.assertEquals("l1value", targetEntity.entityValue().getValue("l1-string").get().valueToString());
        Assertions.assertEquals(0, targetEntity.version());
        Assertions.assertEquals(updateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
            entityOptional.get().time());
    }

    @Test
    public void testBuildEntities() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        int expectedSize = 10000;
        for (int i = 0; i < expectedSize; i++) {
            entityPackage.put(
                Entity.Builder.anEntity()
                    .withId(100000 + i)
                    .withEntityClassRef(l1EntityClassRef)
                    .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                        new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100 + i),
                        new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                        new LongValue(l1EntityClass.field("l1-long").get(), 200 + i),
                        new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                        new DateTimeValue(EntityField.UPDATE_TIME_FILED, LocalDateTime.now())
                    )))
                    .build(), l1EntityClass);
        }

        int[] results = storage.build(entityPackage);
        Assertions.assertEquals(expectedSize, Arrays.stream(results).sum());

        long[] ids = IntStream.range(0, expectedSize).mapToLong(i -> 100000 + i).toArray();
        List<IEntity> entities = new ArrayList(storage.selectMultiple(ids));
        Collections.sort(entities, (o1, o2) -> {
            if (o1.id() < o2.id()) {
                return -1;
            } else if (o1.id() > o2.id()) {
                return 1;
            } else {
                return 0;
            }
        });

        for (int i = 0; i < expectedSize; i++) {
            IEntity entity = entities.get(i);
            Assertions.assertEquals(100 + i, entity.entityValue().getValue("l0-long").get().valueToLong());
            Assertions.assertEquals("l0value", entity.entityValue().getValue("l0-string").get().valueToString());
            Assertions.assertEquals(200 + i, entity.entityValue().getValue("l1-long").get().valueToLong());
            Assertions.assertEquals("l1value", entity.entityValue().getValue("l1-string").get().valueToString());
            Assertions.assertEquals(0, entity.version());
        }

    }

    @Test
    public void testSelectOne() throws Exception {
        List<IEntity> entities = new ArrayList<>(expectedEntitys.size());
        expectedEntitys.stream().mapToLong(e -> e.id()).forEach(id -> {
            Optional<IEntity> entityOp;
            try {
                entityOp = storage.selectOne(id, l1EntityClass);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            entities.add(entityOp.get());
        });

        Assertions.assertEquals(expectedEntitys.size(), entities.size());

    }

    @Test
    public void testSelectMultiple() throws Exception {
        long[] ids = expectedEntitys.stream().mapToLong(e -> e.id()).toArray();
        Collection<IEntity> entities = storage.selectMultiple(ids, l1EntityClass);

        Map<Long, IEntity> expectedEntityMap =
            expectedEntitys.stream().collect(Collectors.toMap(e -> e.id(), e -> e, (e0, e1) -> e0));

        Assertions.assertEquals(expectedEntityMap.size(), entities.size());

        IEntity expectedEntity;
        for (IEntity e : entities) {
            expectedEntity = expectedEntityMap.get(e.id());
            Assertions.assertNotNull(
                expectedEntity, String.format("An instance of the %d object should be found, but not found.", e.id()));

            Assertions.assertEquals(expectedEntity, e);
            // 实际类型是l2EntityClass.
            Assertions.assertEquals(l2EntityClassRef, e.entityClassRef());
        }
    }

    @Test
    public void testReplace() throws Exception {
        LocalDateTime updateTime = LocalDateTime.now();
        IEntity targetEntity = expectedEntitys.get(0);
        targetEntity.entityValue().addValue(
            new LongValue(l1EntityClass.father().get().field("l0-long").get(), 1000000)
        ).addValue(
            new DateTimeValue(EntityField.UPDATE_TIME_FILED, updateTime)
        );

        int oldVersion = targetEntity.version();

        int size = storage.replace(targetEntity, l2EntityClass);
        Assertions.assertEquals(1, size);


        Optional<IEntity> targetEntityOp = storage.selectOne(targetEntity.id(), l2EntityClass);
        Assertions.assertTrue(targetEntityOp.isPresent());
        Assertions.assertEquals(1000000L,
            targetEntityOp.get().entityValue().getValue("l0-long").get().valueToLong());
        Assertions.assertEquals(oldVersion + 1, targetEntityOp.get().version());
        Assertions.assertEquals(updateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
            targetEntityOp.get().time());
    }

    @Test
    public void testReplaceEntities() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        int expectedSize = 1000;
        for (int i = 0; i < expectedSize; i++) {
            entityPackage.put(
                Entity.Builder.anEntity()
                    .withId(100000 + i)
                    .withEntityClassRef(l1EntityClassRef)
                    .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                        new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100 + i),
                        new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                        new LongValue(l1EntityClass.field("l1-long").get(), 200 + i),
                        new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                        new DateTimeValue(EntityField.UPDATE_TIME_FILED, LocalDateTime.now())
                    )))
                    .build(), l1EntityClass);
        }

        int[] results = storage.build(entityPackage);
        Assertions.assertEquals(expectedSize, Arrays.stream(results).sum());

        EntityPackage updatePackage = new EntityPackage();
        entityPackage.stream().map(e -> {
            e.getKey().entityValue().addValue(
                new LongValue(l1EntityClass.father().get().field("l0-long").get(), -100));
            return e;
        }).forEach(e -> {
            updatePackage.put(e.getKey(), e.getValue());
        });

        results = storage.replace(updatePackage);
        Assertions.assertEquals(expectedSize, Arrays.stream(results).sum());

        long[] ids = updatePackage.stream().mapToLong(e -> e.getKey().id()).toArray();
        Collection<IEntity> entities = storage.selectMultiple(ids);
        Assertions.assertEquals(expectedSize,
            entities.stream().filter(e -> e.entityValue().getValue("l0-long").get().valueToLong() == -100).count());
    }

    @Test
    public void testJson() throws Exception {
        IEntity targetEntity = expectedEntitys.get(1);
        targetEntity.entityValue().addValue(
            new StringValue(l2EntityClass.field("l2-string").get(),
                "[{\n   \"c1\":\"c1-value\", \"c2\": 123},]"
            ));
        int size = storage.replace(targetEntity, l2EntityClass);
        Assertions.assertEquals(1, size);

        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        Assertions.assertEquals("[{\n   \"c1\":\"c1-value\", \"c2\": 123},]",
            targetEntity.entityValue().getValue("l2-string").get().valueToString());
    }

    @Test
    public void testDelete() throws Exception {
        IEntity targetEntity = expectedEntitys.get(1);
        storage.replace(targetEntity, l2EntityClass);
        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();

        Assertions.assertEquals(1, storage.delete(targetEntity, l2EntityClass));

        Assertions.assertFalse(storage.selectOne(targetEntity.id(), l2EntityClass).isPresent());

        Assertions.assertFalse(storage.exist(targetEntity.id()));
    }

    @Test
    public void testDeletes() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        expectedEntitys.stream().forEach(e -> {
            entityPackage.put(e, l2EntityClass);
        });

        int[] results = storage.delete(entityPackage);
        Assertions.assertEquals(expectedEntitys.size(), Arrays.stream(results).sum());
    }

    @Test
    public void testDeleteWithoutVersion() throws Exception {
        IEntity targetEntity = expectedEntitys.get(2);
        storage.replace(targetEntity, l2EntityClass);
        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        targetEntity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);

        Assertions.assertEquals(1, storage.delete(targetEntity, l2EntityClass));
        Assertions.assertFalse(storage.selectOne(targetEntity.id(), l2EntityClass).isPresent());
        Assertions.assertFalse(storage.exist(targetEntity.id()));
    }

    @Test
    public void testExist() throws Exception {
        IEntity targetEntity = expectedEntitys.get(2);
        Assertions.assertTrue(storage.exist(targetEntity.id()));

        Assertions.assertFalse(storage.exist(-1));
    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        EntityPackage entityPackage = new EntityPackage();
        for (int i = 1; i <= size; i++) {
            IEntity entity = buildEntity(i * size);
            expectedEntitys.add(entity);
            entityPackage.put(entity, l2EntityClass);
        }

        try {
            storage.build(entityPackage);
            StorageInitialization.getInstance().getCommitIdStatusService().obsoleteAll();
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();

        // 表示为非可读事务.
        for (IEntity e : expectedEntitys) {
            tx.getAccumulator().accumulateBuild(e);
        }

        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        return Entity.Builder.anEntity()
            .withId(baseId)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l2EntityClassRef)
            .withEntityValue(buildEntityValue(baseId,
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,
                    l2LongField, l2StringField)))
            .build();
    }

    private IEntityValue buildEntityValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(10));
                case STRINGS:
                    return new StringsValue(f, buildRandomString(5), buildRandomString(3), buildRandomString(7));
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        return EntityValue.build().addValues(values);
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
}
