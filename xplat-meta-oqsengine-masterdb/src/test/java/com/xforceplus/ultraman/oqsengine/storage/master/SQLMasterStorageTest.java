package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * SQLMasterStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL})
public class SQLMasterStorageTest {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorageTest.class);

    private TransactionManager transactionManager;
    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    private DataSource dataSource;
    private SQLMasterStorage storage;

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
            .withEntityClassId(l0EntityClass.id()).withEntityClassCode(l0EntityClass.code()).build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
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
            .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code()).build();

    //-------------level 2--------------------
    private IEntityField l2LongField = EntityField.Builder.anEntityField()
        .withId(3000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(3001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
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
            .withEntityClassId(l2EntityClass.id()).withEntityClassCode(l2EntityClass.code()).build();

    private List<IEntity> expectedEntitys;

    @Before
    public void before() throws Exception {

        dataSource = buildDataSource("./src/test/resources/sql_master_storage_build.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        redisClient = RedisClient.create(
            String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0),
            new IncreasingOrderLongIdGenerator(0),
            commitIdStatusService,
            false);

        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("oqsbigentity"),
            new NoSelector<>(dataSource), new NoSelector<>("oqsbigentity"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        SQLJsonIEntityValueBuilder entityValueBuilder = new SQLJsonIEntityValueBuilder();
        ReflectionTestUtils.setField(entityValueBuilder, "storageStrategyFactory", storageStrategyFactory);

        storage = new SQLMasterStorage();
        ReflectionTestUtils.setField(storage, "masterDataSource", dataSource);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "entityValueBuilder", entityValueBuilder);
        storage.setTableName("oqsbigentity");
        storage.setQueryTimeout(100000000);
        storage.init();

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        expectedEntitys = initData(storage, 100);
        transactionManager.finish();
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        stat.execute("truncate table oqsbigentity");
        stat.close();
        conn.close();

        ((ShardingDataSource) dataSource).close();

        commitIdStatusService.destroy();

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
    }

    /**
     * 测试写入并查询.
     *
     * @throws Exception
     */
    @Test
    public void testBuildEntity() throws Exception {

        IEntity newEntity = Entity.Builder.anEntity()
            .withEntityClassRef(l1EntityClassRef)
            .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100),
                new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                new LongValue(l1EntityClass.field("l1-long").get(), 200),
                new StringValue(l1EntityClass.field("l1-string").get(), "l1value")
            )))
            .build();
        int size = storage.build(newEntity, l1EntityClass);
        Assert.assertEquals(1, size);

        Optional<IEntity> entityOptional = storage.selectOne(newEntity.id(), l1EntityClass);
        Assert.assertTrue(entityOptional.isPresent());
        IEntity targetEntity = entityOptional.get();
        Assert.assertEquals(newEntity.id(), targetEntity.id());

    }


    @Test
    public void testSelectMultipleIdQuery() throws Exception {

    }

    @Test
    public void testReplace() throws Exception {
        IEntity targetEntity = expectedEntitys.get(0);
        targetEntity.entityValue().addValue(
            new LongValue(l1EntityClass.father().get().field("l0-long").get(), 1000000)
        );
        int size = storage.replace(targetEntity, l2EntityClass);
        Assert.assertEquals(1, size);

        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        Assert.assertEquals(1000000L
            , targetEntity.entityValue().getValue("l0-long").get().valueToLong());
    }

    @Test
    public void testDelete() throws Exception {

        Map<Long, IEntityClass> idMap = expectedEntitys.stream().collect(toMap(IEntity::id, IEntity::entityClass));
        IEntity targetEntity = expectedEntitys.stream().findAny().get();
        storage.delete(targetEntity);
        Collection<IEntity> queryEntitys = storage.selectMultiple(idMap);
        Assert.assertEquals(expectedEntitys.size() - 1, queryEntitys.size());

        Assert.assertEquals(expectedEntitys.size() - 1, queryEntitys.size());
        queryEntitys.stream().forEach(e -> {
            Assert.assertNotEquals(targetEntity.id(), e.id());
        });
    }

    @Test
    public void testDeleteWithoutVersion() throws Exception {
        IEntity targetEntity = expectedEntitys.get(0);
        storage.replace(targetEntity);

        targetEntity = storage.selectOne(targetEntity.id(), targetEntity.entityClass()).get();
        Assert.assertEquals(1, targetEntity.version());

        targetEntity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);
        Assert.assertEquals(1, storage.delete(targetEntity));

        Assert.assertFalse(storage.selectOne(targetEntity.id(), targetEntity.entityClass()).isPresent());
    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        try {
            expectedEntitys.stream().forEach(e -> {
                try {
                    storage.build(e, l2EntityClass);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
                commitIdStatusService.obsoleteAll();
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
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

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
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

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }

}
