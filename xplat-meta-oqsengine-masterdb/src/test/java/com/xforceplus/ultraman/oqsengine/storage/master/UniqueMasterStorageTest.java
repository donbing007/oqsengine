package com.xforceplus.ultraman.oqsengine.storage.master;

import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SimpleFieldKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.crypto.KeyGenerator;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/23 11:53 AM
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.MYSQL})
public class UniqueMasterStorageTest {

    private TransactionManager transactionManager;

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    private MasterUniqueStorage storage;

    private SimpleFieldKeyGenerator keyGenerator;

    private DataSourcePackage dataSourcePackage;



    private LongIdGenerator idGenerator;

    private DataSource dataSource;

    private MetaManager metaManager = mock(MetaManager.class);


    private CommitIdStatusServiceImpl commitIdStatusService;

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
    private IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();
    private EntityClassRef l0EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l0EntityClass.id()).withEntityClassCode(l0EntityClass.code()).build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("l1")
        .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("c1")
        .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("child:u1:1").build()).build();
    private IEntityField l1StringField1 = EntityField.Builder.anEntityField()
        .withId(2002)
        .withFieldType(FieldType.STRING)
        .withName("c2")
        .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("child:u1:2").build()).build();
    private IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("child")
        .withField(l1LongField)
        .withField(l1StringField)
        .withField(l1StringField1)
        .withFather(l0EntityClass)
        .build();
    private EntityClassRef l1EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code()).build();


    private IEntity buildL1Entity(long baseId) {
        return Entity.Builder.anEntity()
            .withId(baseId)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();
    }

    private IEntity buildL1EntityWithDiff(long baseId) {
        return Entity.Builder.anEntity()
            .withId(baseId)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue1(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();
    }

    private IEntityValue buildFixedValue1(Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.name()) {
                case "c1":
                    return new StringValue(f, "c1Value1");
                case "c2":
                    return new StringValue(f,"c2Value1");
                case "l1":
                    return new LongValue(f, 3L);
                default:
                    if(f.type() == FieldType.LONG) {
                        return new LongValue(f, (long) buildRandomLong(10, 100000));
                    }
                    else {
                        return new StringValue(f,buildRandomString(30));
                    }
            }
        }).collect(Collectors.toList());
        IEntityValue value = EntityValue.build();
        value.addValues(values);
        return value;
    }

    private IEntityValue buildFixedValue(Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.name()) {
                case "c1":
                    return new StringValue(f, "c1Value");
                case "c2":
                    return new StringValue(f,"c2Value");
                case "l0-string":
                    return new StringValue(f,"l0Value");
                case "l0-strings" :
                    return new StringValue(f,"l0Values");
                case "l0-long":
                    return new LongValue(f,100L);
                case "l1":
                    return new LongValue(f, 3L);
                default:
                    if(f.type() == FieldType.LONG) {
                        return new LongValue(f, (long) buildRandomLong(10, 100000));
                    }
                    else {
                        return new StringValue(f,buildRandomString(30));
                    }
            }
        }).collect(Collectors.toList());
        IEntityValue value = EntityValue.build();
        value.addValues(values);
        return value;
    }

    @Before
    public void before() throws Exception {
        keyGenerator = new SimpleFieldKeyGenerator();
        when(metaManager.load(1)).thenReturn(Optional.of(l1EntityClass));
        when(metaManager.load(2)).thenReturn(Optional.of(l1EntityClass));

        dataSource = buildDataSource("./src/test/resources/sql_master_storage_build.conf");
        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);
        transactionManager = DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();
        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("oqsunique"),
            new NoSelector<>(dataSource), new NoSelector<>("oqsunique"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());


        storage = new MasterUniqueStorage();
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(keyGenerator,"metaManager",metaManager);
        ReflectionTestUtils.setField(storage,"keyGenerator",keyGenerator);
        storage.setTableName("oqsunique");
        storage.init();

    }

    @After
    public void after() throws Exception {
        transactionManager.finish();
        dataSourcePackage.close();
    }


    // 初始化数据for业务主键
    @Test
    public void testAddDuplicateKeyData() throws Exception {
        thrown.expect(SQLIntegrityConstraintViolationException.class);
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        IEntity entity = buildL1Entity(10001);
        IEntity entity1 = buildL1Entity(10002);
        try {
            storage.build(entity,l1EntityClass);
            storage.build(entity1,l1EntityClass);
        }
        catch (Exception ex){
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx1 = transactionManager.getCurrent().get();
        tx1.commit();
        transactionManager.finish();
    }


    @Test
    public void testAddandDeleteDuplicateKeyData() throws Exception {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        IEntity entity = buildL1Entity(10001);
        IEntity entity1 = buildL1Entity(10002);
        try {
            storage.build(entity,l1EntityClass);
            storage.delete(entity,l1EntityClass);
            storage.build(entity1,l1EntityClass);
            BusinessKey key1 = new BusinessKey();
            key1.setFieldName("c1");
            key1.setValue("c1Value");
            BusinessKey key2 = new BusinessKey();
            key2.setFieldName("c2");
            key2.setValue("c2Value");
            List<BusinessKey> keys = new ArrayList<>();
            keys.add(key1);
            keys.add(key2);
            Optional<StorageUniqueEntity> ret =  storage.select(keys,l1EntityClass);
            Assert.assertEquals(ret.isPresent(),true);
            Assert.assertEquals(ret.get().getId(),10002);
        }
        catch (Exception ex){
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx1 = transactionManager.getCurrent().get();
        tx1.commit();
        transactionManager.finish();
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





//
//
    @Test
    public void testAddandUpdateDuplicateKeyData() throws Exception {
        thrown.expect(SQLIntegrityConstraintViolationException.class);
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        IEntity entity = buildL1Entity(200);
        IEntity entity1 = buildL1EntityWithDiff(300);
        IEntity entity2= buildL1Entity(300);
        try {
            storage.build(entity,l1EntityClass);
            storage.build(entity1,l1EntityClass);
            storage.replace(entity2,l1EntityClass);
        }
        catch (Exception ex){
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx1 = transactionManager.getCurrent().get();
        tx1.commit();
        transactionManager.finish();
    }

    /**
     * 插入子类，父类上有索引，子类上没有索引
     * @throws SQLException
     */
    @Test
    public void testBuildFatherIndexChild() throws SQLException {
        thrown.expect(SQLIntegrityConstraintViolationException.class);
         IEntityField l0LongField = EntityField.Builder.anEntityField()
            .withId(1000)
            .withFieldType(FieldType.LONG)
            .withName("l0-long")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
         IEntityField l0StringField = EntityField.Builder.anEntityField()
            .withId(1001)
            .withFieldType(FieldType.STRING)
            .withName("l0-string")
            .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("father:u1:1").build()).build();
         IEntityField l0StringsField = EntityField.Builder.anEntityField()
            .withId(1003)
            .withFieldType(FieldType.STRINGS)
            .withName("l0-strings")
            .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("father:u1:2").build()).build();

         IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(1)
            .withLevel(0)
            .withCode("father")
            .withField(l0LongField)
            .withField(l0StringField)
            .withField(l0StringsField)
            .build();

         IEntityField l1LongField = EntityField.Builder.anEntityField()
            .withId(2000)
            .withFieldType(FieldType.LONG)
            .withName("l1")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
         IEntityField l1StringField = EntityField.Builder.anEntityField()
            .withId(2001)
            .withFieldType(FieldType.STRING)
            .withName("c1")
             .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
         IEntityField l1StringField1 = EntityField.Builder.anEntityField()
            .withId(2002)
            .withFieldType(FieldType.STRING)
            .withName("c2")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
         IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(3)
            .withLevel(1)
            .withCode("child")
            .withField(l1LongField)
            .withField(l1StringField)
            .withField(l1StringField1)
            .withFather(l0EntityClass)
            .build();
        EntityClassRef l1EntityClassRef =
            EntityClassRef.Builder.anEntityClassRef()
                .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code()).build();


        IEntity entity =  Entity.Builder.anEntity()
            .withId(10000l)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();

        IEntity entity1 =  Entity.Builder.anEntity()
            .withId(10001l)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();
        when(metaManager.load(3)).thenReturn(Optional.of(l1EntityClass));

        storage.build(entity,l1EntityClass);
        storage.build(entity1,l1EntityClass);
    }


    /**
     * 插入子类，父类和子类上有共同的索引，索引属于子类
     * @throws SQLException
     */
    @Test
    public void testBuildFatherChildAllBuild() throws SQLException {
        IEntityField l0LongField = EntityField.Builder.anEntityField()
            .withId(1000)
            .withFieldType(FieldType.LONG)
            .withName("l0-long")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
        IEntityField l0StringField = EntityField.Builder.anEntityField()
            .withId(1001)
            .withFieldType(FieldType.STRING)
            .withName("l0-string")
            .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("child:u1:1").build()).build();
        IEntityField l0StringsField = EntityField.Builder.anEntityField()
            .withId(1003)
            .withFieldType(FieldType.STRINGS)
            .withName("l0-strings")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();

        IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(1)
            .withLevel(0)
            .withCode("father")
            .withField(l0LongField)
            .withField(l0StringField)
            .withField(l0StringsField)
            .build();
        EntityClassRef l0EntityClassRef =
            EntityClassRef.Builder.anEntityClassRef()
                .withEntityClassId(l0EntityClass.id()).withEntityClassCode(l0EntityClass.code()).build();


        IEntityField l1LongField = EntityField.Builder.anEntityField()
            .withId(2000)
            .withFieldType(FieldType.LONG)
            .withName("l1")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
        IEntityField l1StringField = EntityField.Builder.anEntityField()
            .withId(2001)
            .withFieldType(FieldType.STRING)
            .withName("c1")
            .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build()).build();
        IEntityField l1StringField1 = EntityField.Builder.anEntityField()
            .withId(2002)
            .withFieldType(FieldType.STRING)
            .withName("c2")
            .withConfig(FieldConfig.Builder.aFieldConfig().withUniqueName("child:u1:2").build()).build();
        IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(3)
            .withLevel(1)
            .withCode("child")
            .withField(l1LongField)
            .withField(l1StringField)
            .withField(l1StringField1)
            .withFather(l0EntityClass)
            .build();
        EntityClassRef l1EntityClassRef =
            EntityClassRef.Builder.anEntityClassRef()
                .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code()).build();

        IEntity entity0 =  Entity.Builder.anEntity()
            .withId(9999L)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l0EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField))).build();

        IEntity entity00 =  Entity.Builder.anEntity()
            .withId(9998L)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l0EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField))).build();


        IEntity entity =  Entity.Builder.anEntity()
            .withId(10000l)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();

        IEntity entity1 =  Entity.Builder.anEntity()
            .withId(10001l)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(buildFixedValue(
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,l1StringField1)))
            .build();
        when(metaManager.load(3)).thenReturn(Optional.of(l1EntityClass));
        when(metaManager.load(1)).thenReturn(Optional.of(l0EntityClass));
     int ret =    storage.build(entity0,l0EntityClass);
     int ret1 =    storage.build(entity00,l0EntityClass);
     Assert.assertEquals(ret,1);
     Assert.assertEquals(ret1,1);
     int ret2 = storage.build(entity,l1EntityClass);
     Assert.assertEquals(ret2,1);
     storage.build(entity1,l1EntityClass);


    }



//
//    /**
//     * 测试插入父类对象，没有设置业务主键的情况
//     * @throws SQLException
//     */
//    @Test
//    public void testBuildNoIndexFather() throws SQLException {
//        FieldConfig config = FieldConfig.Builder.aFieldConfig().withUniqueName("").build();
//        IEntityField field1 = new EntityField(100, "c1", FieldType.STRING,config);
//        IEntityField field2 = new EntityField(101, "c2", FieldType.STRING,config);
//        Collection<IEntityField> fields = buildRandomFields(102, 3);
//        fields.add(field1);
//        fields.add(field2);
//        IEntity entity =  new Entity(
//                1000,
//                new EntityClass(1, "father", fields),
//                buildFixedValue(1000, fields)
//        );
//        int ret = storage.build(entity);
//        int ret1 = storage.replace(entity);
//        Assert.assertEquals(ret,0);
//        Assert.assertEquals(ret1,0);
//    }
//
//    /**
//     * 测试插入父类对象，没有设置业务主键的情况
//     * @throws SQLException
//     */
//    @Test
//    public void testBuildFather() throws SQLException {
//        FieldConfig config = FieldConfig.build();
//        config.uniqueName("father:U1:2");
//        FieldConfig config1 = FieldConfig.build();
//        config1.uniqueName("father:U1:1");
//        IEntityField field1 = new EntityField(100, "c1", FieldType.STRING,config);
//        IEntityField field2 = new EntityField(101, "c2", FieldType.STRING,config1);
//        Collection<IEntityField> fields = buildRandomFields(102, 3);
//        fields.add(field1);
//        fields.add(field2);
//        IEntity entity =  new Entity(
//                1000,
//                new EntityClass(1, "father", fields),
//                buildFixedValue(1000, fields)
//        );
//        int ret = storage.build(entity);
//        int ret1 = storage.replace(entity);
//        int ret2 = storage.deleteDirectly(entity);
//        Assert.assertEquals(ret,1);
//        Assert.assertEquals(ret1,1);
//        Assert.assertEquals(ret2,1);
//    }
//
//    /**
//     * 插入父类对象，主键构建在父类和子类上。主键属于子类。
//     * @throws SQLException
//     */
//    @Test
//    public void testBuildFatherChildIndex() throws SQLException {
//        FieldConfig config = FieldConfig.build();
//        config.uniqueName("child:U1:2");
//        FieldConfig config1 = FieldConfig.build();
//        config1.uniqueName("child:U1:1");
//        IEntityField field1 = new EntityField(100, "c1", FieldType.STRING,config);
//        IEntityField field2 = new EntityField(101, "c2", FieldType.STRING,config1);
//        Collection<IEntityField> fields = buildRandomFields(102, 3);
//        fields.add(field1);
//        fields.add(field2);
//        IEntity entity =  new Entity(
//                1000,
//                new EntityClass(1, "father", fields),
//                buildFixedValue(1000, fields)
//        );
//        int ret = storage.build(entity);
//        int ret2 = storage.replace(entity);
//        int ret1 = storage.delete(entity);
//        Assert.assertEquals(ret,0);
//        Assert.assertEquals(ret1,0);
//        Assert.assertEquals(ret2,0);
//    }
//
//
//    /**
//     * 插入子类，子类父类上都有各自索引，插入子类索引。
//     * @throws SQLException
//     */
//    @Test
//    public void testBuildIndexChild() throws SQLException {
//        FieldConfig config = FieldConfig.build();
//        config.uniqueName("father:u1:1");
//        FieldConfig config1 = FieldConfig.build();
//        config1.uniqueName("father:u1:2");
//        IEntityField field1 = new EntityField(100, "c1", FieldType.STRING,config);
//        IEntityField field2 = new EntityField(101, "c2", FieldType.STRING,config1);
//        Collection<IEntityField> fields = buildRandomFields(102, 3);
//        fields.add(field1);
//        fields.add(field2);
//
//        IEntityValue entityValue = new EntityValue(idGenerator.next());
//        entityValue.addValues(buildFixedValue(1000,fields).values());
//
//        fatherEntityClass = new EntityClass(idGenerator.next(), "father",fields);
//        IEntity father =  new Entity(
//                1000,
//                new EntityClass(1, "father", fields),
//                entityValue
//        );
//
//        Collection<IEntityField> childFields =
//                Arrays.asList(new EntityField(idGenerator.next(), "c3", FieldType.LONG, FieldConfig.build().searchable(true).uniqueName("child:u2:1")),
//        new EntityField(idGenerator.next(), "c4", FieldType.LONG, FieldConfig.build().searchable(true).uniqueName("child:u2:2")));
//        childEntityClass = new EntityClass(
//                idGenerator.next(),
//                "child",
//                null,
//                null,
//                fatherEntityClass,
//                childFields
//        );
//        entityValue.addValues(buildFixedValue(1001,childFields).values());
//        IEntity child = new Entity(1001,childEntityClass,entityValue);
//        int ret = storage.build(child);
//        int ret1 = storage.replace(child);
//        int ret2 = storage.deleteDirectly(child);
//        Assert.assertEquals(ret,1);
//        Assert.assertEquals(ret1,1);
//        Assert.assertEquals(ret2,1);
//    }
//
//



//
    private Collection<IEntityField> getFixedFields() {
        FieldConfig config1 = FieldConfig.Builder.aFieldConfig().withUniqueName("test:IDX_U1:1").build();
        FieldConfig config2 = FieldConfig.Builder.aFieldConfig().withUniqueName("test:IDX_U1:2").build();
        IEntityField field1 = EntityField.Builder.anEntityField().withId(101).withName("c1")
            .withFieldType(FieldType.STRING).withConfig(config1).build();
        IEntityField field2 = EntityField.Builder.anEntityField().withId(102).withName("c2")
            .withFieldType(FieldType.STRING).withConfig(config2).build();
        Collection<IEntityField> fields = buildRandomFields(102, 3);
        fields.add(field1);
        fields.add(field2);
        return fields;
    }
//
//    private IEntityClass buildEntityClass(long entityClassId) {
//        Collection<IEntityField> fields = getFixedFields();
//       return EntityClass.Builder.anEntityClass().withId(entityClassId).withLevel(0).withCode("test").withFields(fields).build();
//    }
//
    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new EntityField(fieldId, "c" + fieldId,
                    ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }
//

//
//
//    private Selector<String> buildTableNameSelector(String base, int size) {
//        return new SuffixNumberHashSelector(base, size);
//    }
//
//    private UniqueKeyGenerator buildKeyGenerator() {
//        return new SimpleFieldKeyGenerator();
//    }
//
//    private Selector<DataSource> buildDataSourceSelector(String file) {
//        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
//
//        dataSourcePackage = DataSourceFactory.build();
//
//        return new HashSelector<>(dataSourcePackage.getMaster());
//
//    }

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

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }


}
