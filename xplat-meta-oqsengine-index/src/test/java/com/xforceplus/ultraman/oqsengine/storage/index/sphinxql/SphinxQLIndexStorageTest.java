package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
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
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SphinxQLIndexStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/26/2020
 * @since <pre>
 * Feb 26, 2020
 *        </pre>
 */
public class SphinxQLIndexStorageTest extends AbstractContainerTest {

    private TransactionManager transactionManager;
    private SphinxQLIndexStorage storage;
    private DataSourcePackage dataSourcePackage;
    private RedisClient redisClient;
    private Selector<String> indexWriteIndexNameSelector;
    private Selector<DataSource> writeDataSourceSelector;

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING);
    private static IEntityField boolField = new EntityField(Long.MAX_VALUE - 2, "bool", FieldType.BOOLEAN);
    private static IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 3, "datetime", FieldType.DATETIME);
    private static IEntityField decimalField = new EntityField(Long.MAX_VALUE - 4, "decimal", FieldType.DECIMAL);
    private static IEntityField enumField = new EntityField(Long.MAX_VALUE - 5, "enum", FieldType.ENUM);
    private static IEntityField stringsField = new EntityField(Long.MAX_VALUE - 6, "strings", FieldType.STRINGS);

    // issue #14
    private static IEntityField stringField141 = new EntityField(Long.MAX_VALUE - 7, "string0", FieldType.STRING);
    private static IEntityField stringField142 = new EntityField(Long.MAX_VALUE - 8, "string2", FieldType.STRING);

    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test",
        Arrays.asList(longField, stringField, boolField, dateTimeField, decimalField, enumField, stringsField));

    private static IEntityClass batchDeleteClass = new EntityClass(1024 + 1, "batchDeleteClass",
        Arrays.asList(longField, stringField));

    private static IEntity[] entityes;

    private static IEntity[] batchDeleteEntities;
    private static long testStandTime;
    private static long rangeTime;
    private static long taskId;
    private static List<Long> expectedBatchDeleteIds;

    static {
        entityes = new IEntity[7];

        long id = Long.MAX_VALUE;
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ZERO), new EnumValue(enumField, "1"),
            new StringsValue(stringsField, "value1", "value2")));
        entityes[0] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 1;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "v2"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[1] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 2;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "hello world"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[2] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 3;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 76L), new StringValue(stringField, "中文测试chinese test"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[3] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        id = Long.MAX_VALUE - 4;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "\"@带有符号的中文@\"\'"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN")));
        entityes[4] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        // issue #14
        id = Long.MAX_VALUE - 5;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField141, "A"), new StringValue(stringField142, "0")));
        entityes[5] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        // issue #14
        id = Long.MAX_VALUE - 6;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField141, "B"), new StringValue(stringField142, "1")));
        entityes[6] = new Entity(id, entityClass, values, OqsVersion.MAJOR);

        initReIndexData();
    }

    private static void initReIndexData() {
        //	reIndex test delete
        expectedBatchDeleteIds = new ArrayList<>();
        batchDeleteEntities = new IEntity[6];

        taskId = Long.MAX_VALUE;

        testStandTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

        rangeTime = 10;

        //	1. 标准时间-1ms ，不被删
        long time = testStandTime - 5;
        long id = 1;
        IEntityValue batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1025")));

        batchDeleteEntities[0] = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        batchDeleteEntities[0].markTime(time);


        //	2. 满足 标准时～rangeTime, taskId相等, 不被删
        time = testStandTime + rangeTime - 1;
        id = 2;
        batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1026")));

        IEntity entity1 = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        entity1.restMaintainId(taskId);
        batchDeleteEntities[1] = entity1;
        batchDeleteEntities[1].markTime(time);


        //	3. 满足 标准时～rangeTime, 被删
        time = testStandTime + rangeTime - 1;
        id = 3;
        batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1027")));

        batchDeleteEntities[2] = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        batchDeleteEntities[2].markTime(time);
        expectedBatchDeleteIds.add(id);

        // 4.满足taskId不相等, 时间在删除区间内  被删除
        id = 4;
        time = testStandTime + rangeTime - 1;
        batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1028")));

        batchDeleteEntities[3] = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        batchDeleteEntities[3].markTime(time);
        expectedBatchDeleteIds.add(id);

        //	5.满足taskId不相等, 时间卡在边界线下限 被删除
        id = 5;
        time = testStandTime;
        batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1029")));

        batchDeleteEntities[4] = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        batchDeleteEntities[4].markTime(time);
        expectedBatchDeleteIds.add(id);

        //	6.满足taskId不相等, 时间卡在边界线上限 被删除
        id = 6;
        time = testStandTime + rangeTime;
        batchDeletes = new EntityValue(id);
        batchDeletes.addValues(Arrays.asList(new LongValue(longField, id), new StringValue(stringField, "V1030")));

        batchDeleteEntities[5] = new Entity(id, batchDeleteClass, batchDeletes, null, 0, OqsVersion.MAJOR);
        batchDeleteEntities[5].markTime(time);
        expectedBatchDeleteIds.add(id);
    }

    @Before
    public void before() throws Exception {

        redisClient = RedisClient.create(
            String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
        CommitIdStatusServiceImpl commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        writeDataSourceSelector = buildWriteDataSourceSelector(
            "./src/test/resources/sql_index_storage.conf");
        DataSource searchDataSource = buildSearchDataSourceSelector(
            "./src/test/resources/sql_index_storage.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0),
            new IncreasingOrderLongIdGenerator(0),
            commitIdStatusService,
            false);

        indexWriteIndexNameSelector = new SuffixNumberHashSelector("oqsindex", 2);

        TransactionExecutor searchExecutor =
            new AutoJoinTransactionExecutor(transactionManager, new SphinxQLTransactionResourceFactory(),
                new NoSelector<>(searchDataSource), new NoSelector<>("oqsindex"));
        TransactionExecutor writeExecutor =
            new AutoJoinTransactionExecutor(
                transactionManager, new SphinxQLTransactionResourceFactory(),
                writeDataSourceSelector, indexWriteIndexNameSelector);

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();


        storage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(storage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(storage, "searchTransactionExecutor", searchExecutor);
        ReflectionTestUtils.setField(storage, "writeTransactionExecutor", writeExecutor);
        ReflectionTestUtils.setField(storage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "indexWriteIndexNameSelector", indexWriteIndexNameSelector);
        storage.setSearchIndexName("oqsindex");
        storage.setMaxSearchTimeoutMs(1000);
        storage.init();

        truncate();

        initData(storage);

    }

    private void truncate() throws SQLException {
        List<DataSource> dataSources = dataSourcePackage.getIndexWriter();
        for (DataSource ds : dataSources) {
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table oqsindex0");
            st.executeUpdate("truncate table oqsindex1");

            st.close();
            conn.close();
        }
    }

    @After
    public void after() throws Exception {

        Optional<Transaction> t = transactionManager.getCurrent();
        if (t.isPresent()) {
            Transaction tx = t.get();
            if (!tx.isCompleted()) {
                tx.rollback();
            }
        }

        transactionManager.finish();

        truncate();

        dataSourcePackage.close();

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
    }

    @Test
    public void testShard() throws Exception {
        String shardKey;
        for (IEntity entity : entityes) {
            shardKey = Long.toString(entity.id());

            DataSource ds = writeDataSourceSelector.select(shardKey);
            String indexName = indexWriteIndexNameSelector.select(shardKey);

            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    try (ResultSet rs =
                             stat.executeQuery(
                                 String.format("select count(*) c from %s where id = %d", indexName, entity.id()))) {

                        rs.next();
                        Assert.assertEquals(1, rs.getInt("c"));
                    }
                }
            }
        }
    }

    @Test
    public void testReplaceAttribute() throws Exception {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        IEntity expectedEntity = (IEntity) entityes[0].clone();
        expectedEntity.entityValue().clear()
            .addValue(new LongValue(longField, 760))
            .addValue(new StringsValue(stringsField, "\\\'新的字段,会有特殊字符.\'\\", "value3"));

        storage.replaceAttribute(expectedEntity.entityValue());
        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Conditions conditions = Conditions.buildEmtpyConditions();
        conditions.addAnd(new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 760L)));
        conditions.addAnd(new Condition(stringsField, ConditionOperator.EQUALS,
            new StringsValue(stringsField, "\\\'新的字段,会有特殊字符.\'\\")));

        // todo fixed
        Collection<EntityRef> refs = storage.select(conditions, expectedEntity.entityClass(), null,
            Page.newSinglePage(100), null, 1000L);

        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(expectedEntity.id(), refs.stream().findFirst().get().getId());
        Assert.assertEquals(expectedEntity.family().parent(), refs.stream().findFirst().get().getPref());
        Assert.assertEquals(expectedEntity.family().child(), refs.stream().findFirst().get().getCref());

    }

    @Test
    public void testDeleteSuccess() throws Exception {

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        IEntity expectedEntity = (IEntity) entityes[0].clone();
        storage.delete(expectedEntity);

        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        //  todo fixed
        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 1L))),
            entityClass, null, Page.newSinglePage(100), null, 0L);

        Assert.assertEquals(0, refs.size());
    }

    @Test
    public void testBatchDelete() throws Exception {

        //	test delete last one due to taskId = 0 and time in range
        boolean deleteResult =
            storage.clean(batchDeleteClass.id(), taskId, testStandTime, testStandTime + 101);

        Assert.assertTrue(deleteResult);

        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        Collection<EntityRef> entityRefs =
            storage.select(
                Conditions.buildEmtpyConditions(),
                batchDeleteClass,
                null,
                Page.newSinglePage(1000),
                null,
                1000L);

        Assert.assertNotNull(entityRefs);
        Assert.assertEquals(batchDeleteEntities.length - expectedBatchDeleteIds.size(), entityRefs.size());
        List<Long> ids = entityRefs.stream().map(EntityRef::getId).collect(Collectors.toList());
        expectedBatchDeleteIds.forEach(
            expectedDelete -> {
                //	删除的ID不在列表中
                Assert.assertFalse(ids.contains(expectedDelete));
            }
        );
    }

    @Test
    public void testSelectCase() throws Exception {
        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                //  todo fixed
                refs = storage.select(c.conditions, c.entityClass, c.sort, c.page, null, 1000L);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.test(new Result(refs, c.page));
        });

    }

    @Test
    public void testSelectCaseWithTxAndFilterId() throws Exception {
        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        List<Long> ids = Arrays.asList(1L, 2L);

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                //  todo fixed
                refs = storage.select(c.conditions, c.entityClass, c.sort, c.page, ids, 1000L);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.test(new Result(refs, c.page));
        });
    }

    private Collection<Case> buildSelectCase() {

        Page limitOnePage = new Page();
        limitOnePage.setVisibleTotalCount(1);

        return Arrays.asList(
            // sort with id asc
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100),
                result -> {

                    Assert.assertEquals(2, result.refs.size());
                    Assert.assertTrue(result.refs.stream().findFirst().get().getId() < result.refs.stream().skip(1).findFirst().get().getId());
                    // 比较主键id和排序值是否一致.
                    Assert.assertEquals(Long.toString(result.refs.stream().findFirst().get().getId()),
                        result.refs.stream().findFirst().get().getOrderValue());
                    Assert.assertEquals(Long.toString(result.refs.stream().skip(1).findFirst().get().getId()),
                        result.refs.stream().skip(1).findFirst().get().getOrderValue());

                    return true;
                },
                Sort.buildAscSort(new EntityField(0, "id", FieldType.LONG, FieldConfig.build().identifie(true)))
            )
            ,
            // sort with id dsc
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100),
                result -> {

                    Assert.assertEquals(2, result.refs.size());
                    Assert.assertTrue(result.refs.stream().findFirst().get().getId() > result.refs.stream().skip(1).findFirst().get().getId());
                    // 比较主键id和排序值是否一致.
                    Assert.assertEquals(Long.toString(result.refs.stream().findFirst().get().getId()),
                        result.refs.stream().findFirst().get().getOrderValue());
                    Assert.assertEquals(Long.toString(result.refs.stream().skip(1).findFirst().get().getId()),
                        result.refs.stream().skip(1).findFirst().get().getOrderValue());

                    return true;
                },
                Sort.buildDescSort(new EntityField(0, "id", FieldType.LONG, FieldConfig.build().identifie(true)))
            )
            ,
            // page 1
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, new Page(200, 10), result -> {
                Assert.assertEquals(2, result.refs.size());
                Assert.assertEquals(2, result.page.getTotalCount());
                return true;
            }),
            // page max
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, new Page().setVisibleTotalCount(1), result -> {
                Assert.assertEquals(1, result.refs.size());
                Assert.assertEquals(2, result.page.getTotalCount());
                return true;
            }),
            // long eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(2, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long not eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.NOT_EQUALS, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(3, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3, Long.MAX_VALUE};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.GREATER_THAN, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(2, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long <
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.LESS_THAN, new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(1, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long <=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(longField, 2L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(3, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // long in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(longField, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(longField, 2L), new LongValue(longField, 76L))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(3, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2,
                    Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // string eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField, ConditionOperator.EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'"))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(1, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // string no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField, ConditionOperator.NOT_EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'"))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2,
                    Long.MAX_VALUE - 1, Long.MAX_VALUE};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // string like
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(stringField, ConditionOperator.LIKE, new StringValue(stringField, "中文"))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(2, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            })
            ,
            // decimal eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(3, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2,
                    Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // decimal no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.NOT_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 6, Long.MAX_VALUE - 5,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // decimal >
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.GREATER_THAN,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(1, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // decimal >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(decimalField, ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                return true;
            }),
            // dateTimeField between
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(dateTimeField, ConditionOperator.GREATER_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1))))
                    .addAnd(new Condition(dateTimeField, ConditionOperator.LESS_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)))),
                entityClass, Page.newSinglePage(100), result -> {

                Assert.assertEquals(2, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                return true;
            }),
            // stringsField =
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringsField, ConditionOperator.EQUALS,
                        new StringsValue(stringsField, "UNKNOWN"))),
                entityClass, Page.newSinglePage(100), result -> {
                Assert.assertEquals(1, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                return true;
            }),
            // stringsField in
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(stringsField, ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(stringsField, "UNKNOWN"), new StringsValue(stringsField, "value3"))),
                entityClass, Page.newSinglePage(100), result -> {
                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                Assert.assertEquals(0, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                return true;
            }),
            // stringsField in
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(stringsField, ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(stringsField, "UNKNOWN"), new StringsValue(stringsField, "value3"))),
                entityClass, Page.newSinglePage(100), result -> {
                Assert.assertEquals(4, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                Assert.assertEquals(3, result.refs.stream()
                    .filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                return true;
            }),
            // empty page
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(stringsField, ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(stringsField, "UNKNOWN"), new StringsValue(stringsField, "value3"))),
                entityClass, Page.emptyPage(), result -> {
                Assert.assertEquals(0, result.refs.size());
                return true;
            }),
            new Case(Conditions.buildEmtpyConditions(), entityClass, new Page(), result -> {
                Assert.assertEquals(7, result.refs.size());
                return true;
            })
            // 空数据匹配.
            ,
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(stringsField, ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(stringsField, "iqoweiqweq"), new StringsValue(stringsField, "nbbbb"))),
                entityClass, new Page(), result -> {
                Assert.assertEquals(0, result.refs.size());
                return true;
            }),
            // issue #14
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField141, ConditionOperator.EQUALS,
                        new StringValue(stringField141, "B")))
                    .addAnd(new Condition(stringField142, ConditionOperator.EQUALS,
                        new StringValue(stringField142, "0"))),
                entityClass, Page.newSinglePage(100), result -> {
                Assert.assertEquals(0, result.refs.size());
                return true;
            }),
            // order by
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(stringField141, ConditionOperator.EQUALS,
                        new StringValue(stringField141, "B")))
                    .addAnd(new Condition(stringField142, ConditionOperator.EQUALS,
                        new StringValue(stringField142, "0"))),
                entityClass, Page.newSinglePage(100), result -> {
                Assert.assertEquals(0, result.refs.size());
                return true;
            }, Sort.buildAscSort(stringField141))
        );
    }

    private StorageEntity create(Long id, Long entityId, Long commitId, Long tx) {
        StorageEntity entity = new StorageEntity();
        entity.setId(id);
        entity.setEntity(entityId);
        entity.setCommitId(commitId);
        entity.setTx(tx);
        return entity;
    }

    // 初始化数据
    private void initData(SphinxQLIndexStorage storage) throws Exception {

        Long commitId = 100L;
        Long tx = 102L;

        try {
            Arrays.stream(entityes).forEach(e -> {
                try {
                    StorageEntity storageEntity = create(e.id(), e.entityClass().id(), commitId, tx);
                    storage.entityValueToStorage(storageEntity, e.entityValue());
                    storage.batchSave(Collections.singletonList(storageEntity), false, false);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
            Arrays.stream(batchDeleteEntities).forEach(e -> {
                try {
                    StorageEntity storageEntity =
                        create(e.id(), e.entityClass().id(), commitId, tx);
                    storageEntity.setTime(e.time());
                    if (e.maintainId() == taskId) {
                        storageEntity.setMaintainId(taskId);
                    }
                    storage.entityValueToStorage(storageEntity, e.entityValue());
                    storage.batchSave(Collections.singletonList(storageEntity), false, false);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            throw ex;
        }
    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build(true);
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

    }

    private DataSource buildSearchDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build(true);
        }

        return dataSourcePackage.getIndexSearch().get(0);

    }

    private static class Case {
        private Conditions conditions;
        private IEntityClass entityClass;
        private Page page;
        private Sort sort;
        private Predicate<? super Result> check;

        public Case(Conditions conditions, IEntityClass entityClass, Page page, Predicate<? super Result> check) {
            this(conditions, entityClass, page, check, null);
        }

        public Case(Conditions conditions, IEntityClass entityClass, Page page, Predicate<? super Result> check,
                    Sort sort) {
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.page = page;
            this.check = check;
            this.sort = sort;
        }
    }

    private static class Result {
        private Collection<EntityRef> refs;
        private Page page;

        public Result(Collection<EntityRef> refs, Page page) {
            this.refs = refs;
            this.page = page;
        }
    }
}
