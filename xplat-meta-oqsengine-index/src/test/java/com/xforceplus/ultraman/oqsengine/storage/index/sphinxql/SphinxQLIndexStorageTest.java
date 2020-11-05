package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
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
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * SphinxQLIndexStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/26/2020
 * @since <pre>
 * Feb 26, 2020
 *        </pre>
 */
public class SphinxQLIndexStorageTest {

    private static GenericContainer manticore0;
    private static GenericContainer manticore1;
    private static GenericContainer searchManticore;

    private TransactionManager transactionManager = new DefaultTransactionManager(
        new IncreasingOrderLongIdGenerator(0));
    private SphinxQLIndexStorage storage;
    private DataSourcePackage dataSourcePackage;

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

    private static IEntity[] entityes;

    static {
        entityes = new IEntity[7];

        long id = Long.MAX_VALUE;
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ZERO), new EnumValue(enumField, "1"),
            new StringsValue(stringsField, "value1", "value2")));
        entityes[0] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 1;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "v2"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[1] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 2;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "hello world"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[2] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 3;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 76L), new StringValue(stringField, "中文测试chinese test"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[3] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 4;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "\"@带有符号的中文@\"\'"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN")));
        entityes[4] = new Entity(id, entityClass, values);

        // issue #14
        id = Long.MAX_VALUE - 5;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField141, "A"), new StringValue(stringField142, "0")));
        entityes[5] = new Entity(id, entityClass, values);

        // issue #14
        id = Long.MAX_VALUE - 6;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField141, "B"), new StringValue(stringField142, "1")));
        entityes[6] = new Entity(id, entityClass, values);

    }

    @Before
    public void before() throws Exception {

        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector(
            "./src/test/resources/sql_index_storage.conf");
        Selector<DataSource> searchDataSourceSelector = buildSearchDataSourceSelector(
            "./src/test/resources/sql_index_storage.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoJoinTransactionExecutor(transactionManager,
            SphinxQLTransactionResource.class);

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();

        storage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(storage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(storage, "searchDataSourceSelector", searchDataSourceSelector);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        storage.setIndexTableName("oqsindex");
        storage.setMaxQueryTimeMs(1000);
        storage.init();

        truncate();

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

    private void truncate() throws SQLException {

        if (entityes != null) {
            for (IEntity entity : entityes) {
                storage.delete(entity);
            }
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

        Collection<EntityRef> refs = storage.select(conditions, expectedEntity.entityClass(), null,
            Page.newSinglePage(100), Collections.emptyList(), null);

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

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 1L))),
            entityClass, null, Page.newSinglePage(100), Collections.emptyList(), null);

        Assert.assertEquals(0, refs.size());
    }

    @Test
    public void testSelectCase() throws Exception {
        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                refs = storage.select(c.conditions, c.entityClass, c.sort, c.page, Collections.emptyList(), null);
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
            // page 1
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 2L))),
                entityClass, new Page(200, 10), result -> {
                Assert.assertEquals(0, result.refs.size());
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
            }),
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
                entityClass, limitOnePage, result -> {
                Assert.assertEquals(1, result.refs.size());
                long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                Assert.assertEquals(0, result.refs.stream()
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
            }));
    }

    // 初始化数据
    private void initData(SphinxQLIndexStorage storage) throws Exception {
        try {
            Arrays.stream(entityes).forEach(e -> {
                try {
                    storage.buildOrReplace(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

    }

    private Selector<DataSource> buildSearchDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getIndexSearch());

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

    @AfterClass
    public static void cleanEnvironment() throws Exception {
        manticore0.close();
        manticore1.close();
        searchManticore.close();
    }

    @BeforeClass
    public static void prepareEnvironment() throws Exception {
        Network network = Network.newNetwork();
        manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("manticore0")
            .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort());
        manticore0.start();

        manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("manticore1")
            .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort());
        manticore1.start();

        searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("searchManticore")
            .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .dependsOn(manticore0, manticore1)
            .waitingFor(Wait.forListeningPort());
        searchManticore.start();

        String write0Jdbc = String.format(
            "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
            manticore0.getContainerIpAddress(), manticore0.getFirstMappedPort());
        String write1Jdbc = String.format(
            "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
            manticore1.getContainerIpAddress(), manticore1.getFirstMappedPort());

        String searchJdbc = String.format(
            "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
            searchManticore.getContainerIpAddress(), searchManticore.getFirstMappedPort());

        System.setProperty("MANTICORE_WRITE0_JDBC_URL", write0Jdbc);
        System.setProperty("MANTICORE_WRITE1_JDBC_URL", write1Jdbc);
        System.setProperty("MANTICORE_SEARCH_JDBC_URL", searchJdbc);
    }
}
