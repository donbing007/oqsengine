package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * EntityManagementServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/12/2020
 * @since <pre>Mar 12, 2020</pre>
 */
public class EntityManagementServiceImplTest {

    private IEntityClass fatherEntityClass;
    private IEntityClass childEntityClass;

    private EntityManagementServiceImpl service;
    private LongIdGenerator idGenerator;
    private MockMasterStorage masterStorage;
    private MockIndexStorage indexStorage;

    @Before
    public void before() throws Exception {

        idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));

        fatherEntityClass = new EntityClass(idGenerator.next(), "father", Arrays.asList(
            new EntityField(idGenerator.next(), "f1", FieldType.LONG, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "f2", FieldType.STRING, FieldConfig.build().searchable(false)),
            new EntityField(idGenerator.next(), "f3", FieldType.DECIMAL, FieldConfig.build().searchable(true))
        ));

        childEntityClass = new EntityClass(
            idGenerator.next(),
            "chlid",
            null,
            null,
            fatherEntityClass,
            Arrays.asList(
                new EntityField(idGenerator.next(), "c1", FieldType.LONG, FieldConfig.build().searchable(true))
            )
        );


        TransactionManager tm = new DefaultTransactionManager(idGenerator);
        TransactionExecutor te = new AutoCreateTransactionExecutor(tm);

        masterStorage = new MockMasterStorage();
        indexStorage = new MockIndexStorage();

        service = new EntityManagementServiceImpl();
        ReflectionTestUtils.setField(service, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(service, "transactionExecutor", te);
        ReflectionTestUtils.setField(service, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(service, "indexStorage", indexStorage);

    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testDeleteForce() throws Exception {
        // 没有继承
        IEntity expectedEntity = buildEntity(fatherEntityClass, false);
        expectedEntity = service.build(expectedEntity);

        Assert.assertEquals(ResultStatus.SUCCESS, service.deleteForce(expectedEntity));
    }

    @Test
    public void testDeleteForceLowVersion() throws Exception {
        // 没有继承
        IEntity expectedEntity = buildEntity(fatherEntityClass, false);
        expectedEntity.resetVersion(Integer.MAX_VALUE - 1);
        expectedEntity = service.build(expectedEntity);

        // 填写一个低version.应该可以更新成功.
        expectedEntity.resetVersion(0);
        Assert.assertEquals(ResultStatus.SUCCESS, service.deleteForce(expectedEntity));
        Assert.assertEquals(VersionHelp.OMNIPOTENCE_VERSION, expectedEntity.version());
    }

    @Test
    public void testNoExtendBuild() throws Exception {
        // 没有继承
        IEntity expectedEntity = buildEntity(fatherEntityClass, false);
        expectedEntity = service.build(expectedEntity);

        Assert.assertNotEquals(0, expectedEntity.id());
        Assert.assertEquals(new EntityFamily(0, 0), expectedEntity.family());

        // 检查是否成功写入主库和索引库.
        IEntity masterEntity = masterStorage.select(expectedEntity.id(), fatherEntityClass).get();
        Assert.assertEquals(expectedEntity.id(), masterEntity.id());
        Assert.assertEquals(expectedEntity.entityValue(), masterEntity.entityValue());
        Assert.assertEquals(expectedEntity.family(), masterEntity.family());
        Assert.assertEquals(0, masterEntity.version());

        IEntity indexEntity = indexStorage.select(expectedEntity.id()).get();
        Assert.assertEquals(expectedEntity.id(), indexEntity.id());

        // 只保留可搜索字段.
        Map<Long, IEntityField> fieldTable =
            fatherEntityClass.fields().stream()
                .filter(f -> f.config().isSearchable()).collect(Collectors.toMap(IEntityField::id, f -> f));
        Assert.assertEquals(fieldTable.size(), indexEntity.entityValue().values().size());
        Assert.assertEquals(fieldTable.size(), indexEntity.entityValue().values().stream()
            .filter(v -> fieldTable.containsKey(v.getField().id())).collect(Collectors.toList()).size());
    }

    @Test
    public void testExtendBuild() throws Exception {
        IEntity expectedEntity = buildEntity(childEntityClass, false);
        expectedEntity = service.build(expectedEntity);


        // 验证父对象
        Map<Long, IEntityField> fatherFieldTable =
            fatherEntityClass.fields().stream().collect(Collectors.toMap(IEntityField::id, f -> f));
        Map<IEntityField, IValue> expectedFatherValues =
            expectedEntity.entityValue().values().stream().filter(v -> fatherFieldTable.containsKey(v.getField().id()))
                .collect(Collectors.toMap(IValue::getField, v -> v));

        IEntity fatherMasterEntity = masterStorage.select(expectedEntity.family().parent(), fatherEntityClass).get();
        Assert.assertEquals(new EntityFamily(0, expectedEntity.id()), fatherMasterEntity.family());
        Assert.assertEquals(0, fatherMasterEntity.version());
        Assert.assertEquals(fatherEntityClass, fatherMasterEntity.entityClass());
        Collection<IValue> fatherValues = fatherMasterEntity.entityValue().values().stream()
            .filter(v -> expectedFatherValues.containsKey(v.getField())).collect(Collectors.toList());
        Assert.assertEquals(expectedFatherValues.size(), fatherValues.size());
        fatherValues.stream().forEach(v -> {
            Assert.assertEquals(v, expectedFatherValues.get(v.getField()));
        });
        //验证父对像索引
        IEntity fatherIndexEntity = indexStorage.select(expectedEntity.family().parent()).get();
        Map<IEntityField, IValue> expectedFatherIndexValues =
            expectedEntity.entityValue().values().stream().filter(
                v -> fatherFieldTable.containsKey(v.getField().id()) && v.getField().config().isSearchable())
                .collect(Collectors.toMap(IValue::getField, v -> v));
        Collection<IValue> fatherIndexValues = fatherIndexEntity.entityValue().values().stream()
            .filter(v -> expectedFatherIndexValues.containsKey(v.getField())).collect(Collectors.toList());
        Assert.assertEquals(expectedFatherIndexValues.size(), fatherIndexValues.size());
        fatherIndexValues.stream().forEach(v -> {
            Assert.assertEquals(v, expectedFatherIndexValues.get(v.getField()));
        });

        // 验证子对象
        Map<Long, IEntityField> childFieldTable =
            childEntityClass.fields().stream().collect(Collectors.toMap(IEntityField::id, f -> f));
        Map<IEntityField, IValue> expectedChildValues =
            expectedEntity.entityValue().values().stream().filter(v -> childFieldTable.containsKey(v.getField().id()))
                .collect(Collectors.toMap(IValue::getField, v -> v));
        IEntity childMasterEntity = masterStorage.select(expectedEntity.id(), childEntityClass).get();
        Assert.assertEquals(new EntityFamily(expectedEntity.family().parent(), 0), childMasterEntity.family());
        Assert.assertEquals(0, childMasterEntity.version());
        Assert.assertEquals(childEntityClass, childMasterEntity.entityClass());
        Collection<IValue> childValues = childMasterEntity.entityValue().values().stream()
            .filter(v -> expectedChildValues.containsKey(v.getField())).collect(Collectors.toList());
        Assert.assertEquals(expectedChildValues.size(), childValues.size());
        childValues.stream().forEach(v -> {
            Assert.assertEquals(v, expectedChildValues.get(v.getField()));
        });

        //验证子对象索引
        IEntity childIndexEntity = indexStorage.select(expectedEntity.id()).get();
        Map<IEntityField, IValue> expectedChildIndexValues =
            expectedEntity.entityValue().values().stream()
                .filter(v -> v.getField().config().isSearchable())
                .collect(Collectors.toMap(IValue::getField, v -> v));
        Collection<IValue> childIndexValues = childIndexEntity.entityValue().values().stream()
            .filter(v -> expectedChildIndexValues.containsKey(v.getField())).collect(Collectors.toList());
        Assert.assertEquals(expectedChildIndexValues.size(), childIndexValues.size());
        childIndexValues.stream().forEach(v -> {
            Assert.assertEquals(v, expectedChildIndexValues.get(v.getField()));
        });
    }

    @Test
    public void testNotExtendReplace() throws Exception {
        IEntity expectedEntity = buildEntity(fatherEntityClass, false);
        expectedEntity = service.build(expectedEntity);

        IEntityField removeField = fatherEntityClass.fields().stream().findFirst().get();
        expectedEntity.entityValue().remove(removeField);

        Assert.assertEquals(ResultStatus.SUCCESS, service.replace(expectedEntity));

        IEntity masterEntity = masterStorage.select(expectedEntity.id(), fatherEntityClass).get();
        Assert.assertEquals(
            fatherEntityClass.fields().stream().filter(f -> f.id() != removeField.id()).count(),
            masterEntity.entityValue().values().stream().filter(v -> v.getField().id() != removeField.id()).count()
        );

        IEntity indexEntity = indexStorage.select(expectedEntity.id()).get();
        Assert.assertEquals(
            fatherEntityClass.fields().stream().filter(f ->
                f.config().isSearchable() && f.id() != removeField.id()).count(),
            indexEntity.entityValue().values().stream().filter(v -> v.getField().id() != removeField.id()).count()
        );
    }

    @Test
    public void testExtendReplace() throws Exception {
        IEntity expectedEntity = buildEntity(childEntityClass, false);
        expectedEntity = service.build(expectedEntity);

        // 更新父类属性
        expectedEntity.entityValue().addValue(
            new DecimalValue(fatherEntityClass.fields().stream().skip(2).findFirst().get(), new BigDecimal("8888.8888"))
        );

        Assert.assertEquals(ResultStatus.SUCCESS, service.replace(expectedEntity));

        // 验证父类
        IEntity masterEntity = masterStorage.select(expectedEntity.family().parent(), fatherEntityClass).get();
        Assert.assertEquals("8888.8888", masterEntity.entityValue().getValue("f3").get().valueToString());
        // 验证父类索引
        masterEntity = indexStorage.select(expectedEntity.family().parent()).get();
        Assert.assertEquals("8888.8888", masterEntity.entityValue().getValue("f3").get().valueToString());

        //验证子类索引
        IEntity indexEntity = indexStorage.select(expectedEntity.id()).get();
        Assert.assertEquals("8888.8888", indexEntity.entityValue().getValue("f3").get().valueToString());
    }

    @Test
    public void testVersionConflict() throws Exception {
        final IEntity expectedEntity = service.build(buildEntity(fatherEntityClass, true));

        int workSize = 30;

        List<Boolean> results = new ArrayList<>(workSize);

        CountDownLatch latch = new CountDownLatch(workSize);
        CountDownLatch doLatch = new CountDownLatch(workSize);
        ExecutorService workPool = Executors.newFixedThreadPool(workSize);
        for (int i = 0; i < workSize; i++) {
            workPool.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                IEntity finalExpectedEntity = null;
                try {
                    finalExpectedEntity = copyEntity(masterStorage.select(expectedEntity.id(), fatherEntityClass).get());
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                LongValue old = (LongValue) finalExpectedEntity.entityValue().remove(
                    fatherEntityClass.field("f1").get());

                finalExpectedEntity.entityValue().addValue(
                    new LongValue(fatherEntityClass.field("f1").get(), old.getValue() + 1));

                ResultStatus status;
                try {
                    status = service.replace(finalExpectedEntity);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                results.add(ResultStatus.SUCCESS == status);

                doLatch.countDown();

            });
        }

        for (int i = 0; i < workSize; i++) {
            latch.countDown();
        }

        doLatch.await();

        workPool.shutdown();

        int successSize = 0;
        for (boolean r : results) {
            if (r) {
                successSize++;
            }
        }

        LongValue oldLongValue = (LongValue) expectedEntity.entityValue().getValue("f1").get();
        IEntity newEntity = masterStorage.select(expectedEntity.id(), fatherEntityClass).get();
        Assert.assertEquals(oldLongValue.valueToLong() + successSize,
            newEntity.entityValue().getValue("f1").get().valueToLong());
    }


    private static IEntity copyEntity(IEntity source) {
        return copyEntity(source, false);
    }

    private static IEntity copyEntity(IEntity source, boolean incrVersion) {
        IEntityValue newValue = new EntityValue(source.id());
        newValue.addValues(source.entityValue().values());

        return new Entity(
            source.id(),
            source.entityClass(),
            newValue,
            new EntityFamily(source.family().parent(), source.family().child()),
            incrVersion ? source.version() + 1 : source.version()
        );
    }

    private IEntity buildEntity(IEntityClass entityClass, boolean buildId) {
        long entityId = 0;
        if (buildId) {
            entityId = idGenerator.next();
        }

        IEntityValue entityValue = new EntityValue(entityId);

        entityValue.addValues(buildValues(entityClass.fields()));

        IEntityClass extendEntityClass = entityClass.extendEntityClass();
        if (extendEntityClass != null) {
            entityValue.addValues(buildValues(extendEntityClass.fields()));
        }

        return new Entity(entityId, entityClass, entityValue, 0);
    }

    private Collection<IValue> buildValues(Collection<IEntityField> fields) {
        return fields.stream().map(f -> {
            switch (f.type()) {
                case LONG:
                    return new LongValue(f, (long) buildRandomLong(0, 1000));
                case STRING:
                    return new StringValue(f, buildRandomString(5));
                case DECIMAL:
                    return new DecimalValue(
                        f,
                        new BigDecimal(
                            Long.toString(buildRandomLong(0, 10))
                                + "."
                                + Long.toString(buildRandomLong(0, 100))
                        )
                    );
                default:
                    throw new IllegalStateException("Error " + f.type().name());
            }
        }).collect(Collectors.toList());
    }

    static class MockMasterStorage implements MasterStorage {

        private ConcurrentMap<Long, IEntity> data = new ConcurrentHashMap<>();

        @Override
        public Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
            return ids.keySet().stream().map(id -> data.get(id)).collect(Collectors.toList());
        }

        @Override
        public synchronized int synchronize(long id, long child) throws SQLException {
            IEntity source = data.get(id);
            IEntity target = data.get(child);

            java.lang.reflect.Field field;
            try {
                field = target.getClass().getField("version");

                field.setAccessible(true);
                field.setInt(target, source.version());

            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }

            return 1;
        }

        @Override
        public IEntityValue toEntityValue(long id, Map<String, IEntityField> fieldTable, String json) throws SQLException {
            return null;
        }

        @Override
        public int build(IEntity entity) throws SQLException {
            data.put(entity.id(), copyEntity(entity));
            return 1;
        }

        @Override
        public synchronized int replace(IEntity entity) throws SQLException {

            IEntity old = data.get(entity.id());
            if (old.version() != entity.version()) {
                return 0;
            } else {
                data.put(entity.id(), copyEntity(entity, true));
                return 1;
            }
        }

        @Override
        public synchronized int delete(IEntity entity) throws SQLException {

            if (VersionHelp.isOmnipotence(entity.version())) {
                data.remove(entity.id());
                return 1;
            } else {

                IEntity old = data.get(entity.id());
                if (old.version() != entity.version()) {
                    return 0;
                } else {
                    data.remove(entity.id());
                    return 1;
                }
            }

        }
    }

    static class MockIndexStorage implements IndexStorage {

        private Map<Long, IEntity> data = new HashMap<>();

        public Optional<IEntity> select(long id) {
            return Optional.ofNullable(copyEntity(data.get(id)));
        }

//        @Override
//        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
//            throws SQLException {
//            throw new UnsupportedOperationException();
//        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page, List<Long> filterIds, Long commitId) throws SQLException {
            return null;
        }

        @Override
        public void replaceAttribute(IEntityValue attribute) throws SQLException {
            IEntity target = data.get(attribute.id());
            attribute.values().stream().forEach(a -> {
                target.entityValue().addValue(a);
            });
        }

        @Override
        public int delete(long id) throws SQLException {
            return 0;
        }

        @Override
        public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
            return 0;
        }

        @Override
        public int build(IEntity entity) throws SQLException {
            data.put(entity.id(), copyEntity(entity));
            return 1;
        }

        @Override
        public int replace(IEntity entity) throws SQLException {
            data.put(entity.id(), copyEntity(entity));
            return 1;
        }

        @Override
        public int delete(IEntity entity) throws SQLException {
            data.remove(entity.id());
            return 1;
        }
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }
} 
