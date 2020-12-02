package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.HashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.SuffixNumberHashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.lettuce.core.RedisClient;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author dongbin
 * @version 0.1 2020/11/6 16:16
 * @since 1.8
 */
public class SQLMasterStorageQueryTest extends AbstractContainerTest {

    private TransactionManager transactionManager;

    private DataSource dataSource;
    private SQLMasterStorage storage;
    private RedisClient redisClient;

    private static IEntityField idField = new EntityField(Long.MAX_VALUE - 100, "id", FieldType.LONG, FieldConfig.build().identifie(true));
    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING);
    private static IEntityField boolField = new EntityField(Long.MAX_VALUE - 2, "bool", FieldType.BOOLEAN);
    private static IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 3, "datetime", FieldType.DATETIME);
    private static IEntityField decimalField = new EntityField(Long.MAX_VALUE - 4, "decimal", FieldType.DECIMAL);
    private static IEntityField enumField = new EntityField(Long.MAX_VALUE - 5, "enum", FieldType.ENUM);
    private static IEntityField stringsField = new EntityField(Long.MAX_VALUE - 6, "strings", FieldType.STRINGS);

    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test",
        Arrays.asList(longField, stringField, boolField, dateTimeField, decimalField, enumField, stringsField));

    private static IEntity[] entityes;

    static {
        entityes = new IEntity[5];

        long id = Long.MAX_VALUE;
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(
            new LongValue(longField, 1L),
            new StringValue(stringField, "v1"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ZERO), new EnumValue(enumField, "1"),
            new StringsValue(stringsField, "value1", "value2"),
            new EnumValue(enumField, "1")
            )
        );
        entityes[0] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 1;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "v2"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3"),
            new EnumValue(enumField, "2")
        ));
        entityes[1] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 2;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "hello world"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3"),
            new EnumValue(enumField, "3")
        ));
        entityes[2] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 3;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 76L), new StringValue(stringField, "中文测试chinese test"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3"),
            new EnumValue(enumField, "4")
        ));
        entityes[3] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 4;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "\"@带有符号的中文@\"\'"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN"),
            new EnumValue(enumField, "5")
        ));
        entityes[4] = new Entity(id, entityClass, values, OqsVersion.MAJOR);
    }

    @Before
    public void before() throws Exception {

        DataSource ds = buildDataSource("./src/test/resources/sql_master_storage_build.conf");


        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        redisClient = RedisClient.create(
            String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
        CommitIdStatusServiceImpl commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0), new IncreasingOrderLongIdGenerator(0), commitIdStatusService);

        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("oqsbigentity"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        SQLJsonIEntityValueBuilder entityValueBuilder = new SQLJsonIEntityValueBuilder();
        ReflectionTestUtils.setField(entityValueBuilder, "storageStrategyFactory", storageStrategyFactory);

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        storage = new SQLMasterStorage();
        ReflectionTestUtils.setField(storage, "masterDataSource", ds);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "entityValueBuilder", entityValueBuilder);
        ReflectionTestUtils.setField(storage, "conditionsBuilderFactory", sqlJsonConditionsBuilderFactory);
        storage.setTableName("oqsbigentity");
        storage.setQueryTimeout(100000000);
        storage.init();

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        try {
            initData(storage);
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

        Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        stat.execute("truncate table oqsbigentity");
        stat.close();
        conn.close();

        ((ShardingDataSource) dataSource).close();

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
    }

    /**
     * 测试事务内查询,以测试事务隔离.
     *
     * @throws Exception
     */
    @Test
    public void testUncommittedTransactionSelect() throws Exception {
        IEntityValue uncommitEntityValue = new EntityValue(100L);
        uncommitEntityValue.addValues(Arrays.asList(
            new LongValue(longField, 2L),
            new StringValue(stringField, "\"@带有符号的中文@\"\'"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN")));
        IEntity uncommitEntity = new Entity(100L, entityClass, uncommitEntityValue, OqsVersion.MAJOR);
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        storage.build(uncommitEntity);

        Collection<EntityRef> refs = storage.select(
            0L, // 每一个测试准备的数据中提交号都为0.
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))
            ),
            entityClass,
            Sort.buildOutOfSort());
        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();

        Assert.assertEquals(3, refs.size());
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
                refs = storage.select(0, c.conditions, c.entityClass, c.sort);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.accept(refs);
        });
    }

    private Collection<Case> buildSelectCase() {

        return Arrays.asList(
            // enum no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        enumField, ConditionOperator.NOT_EQUALS, new EnumValue(enumField, "1")
                    )),
                entityClass,
                r -> {
                    Assert.assertEquals(4, r.size());
                }
            )
            ,
            // id eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        idField, ConditionOperator.EQUALS, new LongValue(idField, Long.MAX_VALUE)
                    )),
                entityClass,
                result -> {
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(Long.MAX_VALUE, result.stream().findFirst().get().getId());
                }
            )
            ,
            // long eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass,
                result -> {
                    Assert.assertEquals(2, result.size());
                    Assert.assertEquals(0, result.stream().filter(r ->
                        !(r.getId() == Long.MAX_VALUE - 1)).filter(r -> !(r.getId() == Long.MAX_VALUE - 2)).count());
                })
            ,
            // long not eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.NOT_EQUALS, new LongValue(longField, 2L))),
                entityClass,
                result -> {
                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());
                })
            ,
            // long >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.GREATER_THAN, new LongValue(longField, 2L))),
                entityClass,
                result -> {

                    Assert.assertEquals(2, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());
                })
            ,
            // long >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(longField, 2L))),
                entityClass,
                result -> {

                    Assert.assertEquals(4, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());
                })
            ,
            // long <
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.LESS_THAN, new LongValue(longField, 2L))),
                entityClass,
                result -> {

                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .count());
                })
            ,
            // long <=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(longField, 2L))),
                entityClass,
                result -> {

                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .count());
                })
            ,
            // long in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(longField, 2L), new LongValue(longField, 76L))),
                entityClass,
                result -> {

                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .count());

                })
            ,
            // string eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField, ConditionOperator.EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'"))),
                entityClass,
                result -> {

                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());

                })
            ,
            // string no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField, ConditionOperator.NOT_EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'"))),
                entityClass,
                result -> {

                    Assert.assertEquals(4, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .count());

                })
            ,
            // string like
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(stringField, ConditionOperator.LIKE, new StringValue(stringField, "中文"))),
                entityClass,
                result -> {

                    Assert.assertEquals(2, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .count());

                })
            ,
            // decimal eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass,
                result -> {

                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .count());

                })
            ,
            // decimal no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.NOT_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass,
                result -> {

                    Assert.assertEquals(2, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());

                })
            ,
            // decimal >
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.GREATER_THAN,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass,
                result -> {

                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());

                })
            ,
            // decimal >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass,
                result -> {

                    Assert.assertEquals(4, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());

                })
            ,
            // dateTimeField between
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(dateTimeField, ConditionOperator.GREATER_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1))))
                    .addAnd(new Condition(dateTimeField, ConditionOperator.LESS_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)))),
                entityClass,
                result -> {

                    Assert.assertEquals(2, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .count());
                })
            ,
            // stringsField =
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringsField, ConditionOperator.EQUALS,
                        new StringsValue(stringsField, "UNKNOWN"))),
                entityClass,
                result -> {
                    Assert.assertEquals(1, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());
                })
            ,
            // stringsField in
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        stringsField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(stringsField, "UNKNOWN"),
                        new StringsValue(stringsField, "value3"))),
                entityClass,
                result -> {
                    Assert.assertEquals(4, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 1))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 2))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());
                })
            ,
            // emtpy condition
            new Case(Conditions.buildEmtpyConditions(), entityClass, result -> {
                Assert.assertEquals(5, result.size());
            })
            ,
            // strings in no row.
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(stringsField, ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(stringsField, "iqoweiqweq"), new StringsValue(stringsField, "nbbbb"))),
                entityClass,
                result -> {
                    Assert.assertEquals(0, result.size());
                })
            ,
            // sort
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.NOT_EQUALS, new LongValue(longField, 2L))),
                entityClass,
                result -> {
                    Assert.assertEquals(3, result.size());
                    Assert.assertEquals(0, result.stream()
                        .filter(r -> !(r.getId() == Long.MAX_VALUE))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 3))
                        .filter(r -> !(r.getId() == Long.MAX_VALUE - 4))
                        .count());

                    Assert.assertEquals(3, result.stream().filter(e -> e.getOrderValue() != null).count());
                },
                Sort.buildDescSort(dateTimeField)
            )
        );
    }

    private static class Case {
        private Conditions conditions;
        private IEntityClass entityClass;
        private Sort sort;
        private Consumer<? super Collection<EntityRef>> check;

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check) {
            this(conditions, entityClass, check, null);
        }

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check,
                    Sort sort) {
            this.conditions = conditions;
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
        try {
            Arrays.stream(entityes).forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);

        AtomicInteger index = new AtomicInteger(0);
        Map<String, DataSource> dsMap = dataSourcePackage.getMaster().stream().collect(Collectors.toMap(
            d -> "ds" + index.getAndIncrement(), d -> d));

        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(
            "oqsbigentity", "ds${0..1}.oqsbigentity${0..2}");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("id", new HashPreciseShardingAlgorithm()));
        tableRuleConfiguration.setTableShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("id", new SuffixNumberHashPreciseShardingAlgorithm()));


        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfiguration);

        Properties prop = new Properties();
//        prop.put("sql.show", "true");
//        prop.put("sql.simple", "false");
        dataSource = ShardingDataSourceFactory.createDataSource(dsMap, shardingRuleConfig, prop);
        return dataSource;
    }
}
