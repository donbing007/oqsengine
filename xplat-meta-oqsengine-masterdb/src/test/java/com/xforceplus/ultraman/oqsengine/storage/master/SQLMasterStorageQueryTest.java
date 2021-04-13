package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dongbin
 * @version 0.1 2020/11/6 16:16
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL})
public class SQLMasterStorageQueryTest {

    private TransactionManager transactionManager;
    private CommitIdStatusServiceImpl commitIdStatusService;

    private DataSource dataSource;
    private SQLMasterStorage storage;
    private RedisClient redisClient;

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
        .withConfig(FieldConfig.Builder.aFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1002)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0EnumField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.ENUM)
        .withName("l0-enum")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DecimalField = EntityField.Builder.anEntityField()
        .withId(1004)
        .withFieldType(FieldType.DECIMAL)
        .withName("l0-decimal")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DatetimeField = EntityField.Builder.anEntityField()
        .withId(1005)
        .withFieldType(FieldType.DATETIME)
        .withName("l0-datetime")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .withField(l0EnumField)
        .withField(l0DecimalField)
        .withField(l0DatetimeField)
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
        .withConfig(FieldConfig.Builder.aFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
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
    private IEntityClass l2EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(3)
        .withLevel(2)
        .withCode("l2")
        .withField(l2LongField)
        .withField(l2StringField)
        .withFather(l1EntityClass)
        .build();

    private List<IEntity> entityes;


    @Before
    public void before() throws Exception {

        DataSource ds = buildDataSource("./src/test/resources/sql_master_storage_build.conf");


        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        redisClient = RedisClient.create(
            String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        transactionManager = DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();

        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("oqsbigentity"),
            new NoSelector<>(ds), new NoSelector<>("oqsbigentity"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        MetaManager metaManager = mock(MetaManager.class);
        when(metaManager.load(l0EntityClass.id())).thenReturn(Optional.of(l0EntityClass));
        when(metaManager.load(l1EntityClass.id())).thenReturn(Optional.of(l1EntityClass));
        when(metaManager.load(l2EntityClass.id())).thenReturn(Optional.of(l2EntityClass));

        storage = new SQLMasterStorage();
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "conditionsBuilderFactory", sqlJsonConditionsBuilderFactory);
        ReflectionTestUtils.setField(storage, "metaManager", metaManager);
        storage.setTableName("oqsbigentity");
        storage.setQueryTimeout(100000000);
        storage.init();

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        try {
            initData(storage);

            // 表示为只读事务.
            for (IEntity e : entityes) {
                tx.getAccumulator().accumulateBuild(e);
            }
            tx.commit();
        } catch (Exception ex) {

            if (!tx.isCompleted()) {
                tx.rollback();
            }

            throw ex;

        } finally {
            transactionManager.finish();
        }

        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        storage.destroy();

        Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        stat.execute("truncate table oqsbigentity");
        stat.close();
        conn.close();

        ((HikariDataSource) dataSource).close();

        commitIdStatusService.destroy();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
    }

    @Test
    public void testActualEntityClass() throws Exception {
        Optional<IEntity> entityOp = storage.selectOne(entityes.get(0).id(), l0EntityClass);
        Assert.assertTrue(entityOp.isPresent());
        IEntity entity = entityOp.get();
        Assert.assertEquals(l2EntityClass.ref(), entity.entityClassRef());

        Collection<IEntity> entities = storage.selectMultiple(new long[]{entityes.get(1).id()}, l0EntityClass);
        for (IEntity e : entities) {
            Assert.assertEquals(l2EntityClass.ref(), e.entityClassRef());
        }
    }

    /**
     * 测试事务内查询,以测试事务隔离.
     *
     * @throws Exception
     */
    @Test
    public void testUncommittedTransactionSelect() throws Exception {
        IEntity uncommitEntity = Entity.Builder.anEntity()
            .withId(1000000)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 138293),
                    new StringValue(l2EntityClass.field("l0-string").get(), "hAG7O1uv1FS3"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "KRW"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Emerald"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("566017837.77")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2010, 6, 29, 9, 36, 36)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 072571712),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Logan_Uttridge6552@grannar.com"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 1448200874),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Benin")
                )
            )).build();
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        Assert.assertEquals(1, storage.build(uncommitEntity, l2EntityClass));

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(l2EntityClass.field("l0-long").get(), 138293))
            ),
            l0EntityClass,
            SelectConfig.Builder.aSelectConfig().withSort(Sort.buildOutOfSort()).withCommitId(0).build());
        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();

        Assert.assertEquals(2, refs.size());
        long size = refs.stream().mapToLong(e -> e.getId()).filter(id -> (id == 1004) || (id == 1000000)).count();
        Assert.assertEquals(2, size);
    }

    /**
     * 功能性查询测试.
     *
     * @throws Exception
     */
    @Test
    public void testSelect() throws Exception {
        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                refs = storage.select(c.conditions, c.entityClass,
                    SelectConfig.Builder.aSelectConfig()
                        .withDataAccessFitlerCondtitons(c.filterConditions).withCommitId(0).withSort(c.sort).build());
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.accept(refs);
        });
    }

    private Collection<Case> buildSelectCase() {

        return Arrays.asList(
            // sort asc
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-enum").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(
                            l2EntityClass.field("l0-enum").get(), "Blue")
                    )),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, r, true);
                },
                Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)
            )
            ,
            // id eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.EQUALS, new LongValue(EntityField.ID_ENTITY_FIELD, 1004)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            )
            ,
            // id in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(EntityField.ID_ENTITY_FIELD, 1000),
                        new LongValue(EntityField.ID_ENTITY_FIELD, 1002)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1002
                    };
                    assertSelect(expectedIds, result, false);
                }
            )
            ,
            // id in (not exist)
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(EntityField.ID_ENTITY_FIELD, 10000000)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {};
                    assertSelect(expectedIds, result, false);
                }
            )
            ,
            // long eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        l2EntityClass.field("l2-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l2-long").get(), 2032249908))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1003
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long not eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        l2EntityClass.field("l2-long").get(),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(l2EntityClass.field("l2-long").get(), 2032249908))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l1-long").get(),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(l2EntityClass.field("l1-long").get(), 87011006L))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-long").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l1-long").get(), 87011006L))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long <
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1002
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long <=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1002, 1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // long in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293),
                        new LongValue(l2EntityClass.field("l0-long").get(), 634274),
                        new LongValue(l2EntityClass.field("l0-long").get(), 381134)
                    )),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // string eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // string no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // string like
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "org"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1002
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l0-string").get(), "or"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // decimal eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // decimal no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.NOT_EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // decimal >
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };

                    assertSelect(expectedIds, result, false);

                })
            ,
            // decimal >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };

                    assertSelect(expectedIds, result, false);

                })
            ,
            // dateTimeField between
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-datetime").get(),
                        ConditionOperator.GREATER_THAN,
                        new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                            LocalDateTime.of(2001, 4, 29, 8, 13, 4))))

                    .addAnd(new Condition(l2EntityClass.field("l0-datetime").get(),
                        ConditionOperator.LESS_THAN,
                        new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                            LocalDateTime.of(2008, 6, 21, 19, 43, 51)))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000
                    };

                    assertSelect(expectedIds, result, false);
                })
            ,
            // stringsField =
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1003
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // stringsField in
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB"),
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                })
            ,
            // emtpy condition
            new Case(Conditions.buildEmtpyConditions(),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            )
            ,
            // strings in no row.
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    l2EntityClass.field("l0-strings").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "iqoweiqweq"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "nbbbb"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {};
                    assertSelect(expectedIds, result, false);
                })
            ,
            // emtpy condition data access
            new Case(Conditions.buildEmtpyConditions(),
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l2-string").get(), "Mozambique")
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                },
                Sort.buildOutOfSort()
            )
            ,
            // stringsField in with condition data access
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-strings").get(),
                            ConditionOperator.MULTIPLE_EQUALS,
                            new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB"),
                            new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l2-string").get(), "Trinidad")
                    )).addAnd(
                    new Condition(
                        l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l2-string").get(), "Lithuania")
                    )
                ),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001
                    };
                    assertSelect(expectedIds, result, false);
                },
                Sort.buildOutOfSort())
        );
    }

    private void assertSelect(long[] expectedIds, Collection<EntityRef> result, boolean sort) {
        Assert.assertEquals(expectedIds.length, result.size());
        result.stream().forEach(e -> {
            Assert.assertTrue(String.format("Not found %d", e.getId()), Arrays.binarySearch(expectedIds, e.getId()) >= 0);
            if (sort) {
                Assert.assertNotNull(e.getOrderValue());
            }
        });
    }

    private static class Case {
        private Conditions conditions;
        private Conditions filterConditions;
        private IEntityClass entityClass;
        private Sort sort;
        private Consumer<? super Collection<EntityRef>> check;

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check, Sort.buildOutOfSort());
        }

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check, Sort sort) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check, sort);
        }

        public Case(
            Conditions conditions,
            Conditions filterConditions,
            IEntityClass entityClass,
            Consumer<? super Collection<EntityRef>> check,
            Sort sort) {
            this.conditions = conditions;
            this.filterConditions = filterConditions;
            this.entityClass = entityClass;
            this.check = check;
            if (sort == null) {
                this.sort = Sort.buildOutOfSort();
            } else {
                this.sort = sort;
            }
        }
    }

    // 初始化数据
    private void initData(SQLMasterStorage storage) throws Exception {
        buildData();

        try {
            entityes.stream().forEach(e -> {
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
    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        this.dataSource = dataSourcePackage.getMaster().get(0);
        return dataSource;
    }

    private void buildData() {
        entityes = new ArrayList<>();

        long baseId = 1000;
        entityes.add(Entity.Builder.anEntity() // 1000
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 26, 12, 15, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 634274),
                    new StringValue(l2EntityClass.field("l0-string").get(), "TjguZT2nz9KT"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "JPY", "USD"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Blue"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2003, 10, 29, 18, 55, 14)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 87011006),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Emely_Dickson1490@jiman.org"),
                    new LongValue(l2EntityClass.field("l2-long").get(), -2037817147),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Belize")
                )
            )).build());

        entityes.add(Entity.Builder.anEntity() // 1001
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 27, 12, 15, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 381134),
                    new StringValue(l2EntityClass.field("l0-string").get(), "qqSDo69gZcGW"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "JPY", "CHF"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Red"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("741808930.09")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2018, 11, 30, 11, 36, 41)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 443531115),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Manuel_Vincent2662@naiker.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), -251454086),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Montenegro")
                )
            )).build());

        entityes.add(Entity.Builder.anEntity() // 1002
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 129848),
                    new StringValue(l2EntityClass.field("l0-string").get(), "oLS90hto8tSn"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "FRF", "AUD", "BEF"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Aqua"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-445124094.25")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2008, 6, 21, 19, 43, 51)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 208747364),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Maxwell_Richardson4862@twace.org"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 1457704562),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Lithuania")
                )
            )).build());

        entityes.add(Entity.Builder.anEntity() // 1003
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 333326),
                    new StringValue(l2EntityClass.field("l0-string").get(), "Trm7n8Wd2ejj"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY", "FIM"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Azure"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("659709035.95")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2001, 4, 29, 8, 13, 4)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 647147145),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Sebastian_Smith3123@sveldo.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 2032249908),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Trinidad")
                )
            )).build());

        entityes.add(Entity.Builder.anEntity() // 1004
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                    2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withEntityValue(EntityValue.build().addValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 138293),
                    new StringValue(l2EntityClass.field("l0-string").get(), "H5qEkXkTvGWW"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "KRW"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Lavender"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2000, 4, 15, 20, 31, 58)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 441034626),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 622028442),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Mozambique")
                )
            )).build());
    }
}
