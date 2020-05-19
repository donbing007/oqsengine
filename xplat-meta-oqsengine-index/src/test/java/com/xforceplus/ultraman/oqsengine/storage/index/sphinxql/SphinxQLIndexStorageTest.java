package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
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
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * SphinxQLIndexStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/26/2020
 * @since <pre>Feb 26, 2020</pre>
 */
public class SphinxQLIndexStorageTest {


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

    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test", Arrays.asList(
        longField,
        stringField,
        boolField,
        dateTimeField,
        decimalField,
        enumField,
        stringsField
    ));

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
            new DecimalValue(decimalField, BigDecimal.ZERO),
            new EnumValue(enumField, "1"),
            new StringsValue(stringsField, "value1", "value2")
        ));
        entityes[0] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 1;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(
            new LongValue(longField, 2L),
            new StringValue(stringField, "v2"),
            new BooleanValue(boolField, true),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE),
            new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")
        ));
        entityes[1] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 2;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(
            new LongValue(longField, 2L),
            new StringValue(stringField, "hello world"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE),
            new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")
        ));
        entityes[2] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 3;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(
            new LongValue(longField, 76L),
            new StringValue(stringField, "中文测试chinese test"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, BigDecimal.ONE),
            new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3")
        ));
        entityes[3] = new Entity(id, entityClass, values);

        id = Long.MAX_VALUE - 4;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(
            new LongValue(longField, 86L),
            new StringValue(stringField, "\"@带有符号的中文@\"\'"),
            new BooleanValue(boolField, false),
            new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
            new DecimalValue(decimalField, new BigDecimal("123.7582193213")),
            new EnumValue(enumField, "CODE"),
            new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN")
        ));
        entityes[4] = new Entity(id, entityClass, values);

    }


    @Before
    public void before() throws Exception {


        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector("./src/test/resources/sql_index_storage.conf");
        Selector<DataSource> searchDataSourceSelector = buildSearchDataSourceSelector("./src/test/resources/sql_index_storage.conf");


        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(
            transactionManager, SphinxQLTransactionResource.class);

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
        storage.setIndexTableName("oqsindextest");
        storage.init();

        truncate();

        transactionManager.create();

        Transaction tx = transactionManager.getCurrent().get();
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
        transactionManager.create();

        IEntity expectedEntity = (IEntity) entityes[0].clone();
        expectedEntity.entityValue().addValue(
            new LongValue(longField, 760L)
        );
        expectedEntity.entityValue().addValue(
            new StringsValue(stringsField, "\\\'新的字段,会有特殊字符.\'\\", "value3")
        );

        storage.replaceAttribute(expectedEntity.entityValue());
        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Conditions conditions = Conditions.buildEmtpyConditions();
        conditions.addAnd(new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 760L)));
        conditions.addAnd(
            new Condition(
                stringsField,
                ConditionOperator.EQUALS,
                new StringsValue(stringsField, "\\\'新的字段,会有特殊字符.\'\\")));

        Collection<EntityRef> refs = storage.select(
            conditions, expectedEntity.entityClass(), null, Page.newSinglePage(100));

        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(expectedEntity.id(), refs.stream().findFirst().get().getId());
        Assert.assertEquals(expectedEntity.family().parent(), refs.stream().findFirst().get().getPref());
        Assert.assertEquals(expectedEntity.family().child(), refs.stream().findFirst().get().getCref());


    }

    @Test
    public void testDeleteSuccess() throws Exception {

        transactionManager.create();

        IEntity expectedEntity = (IEntity) entityes[0].clone();
        storage.delete(expectedEntity);

        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(longField, ConditionOperator.EQUALS, new LongValue(longField, 1L))),
            entityClass,
            null,
            Page.newSinglePage(100)
        );

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
                refs = storage.select(c.conditions, c.entityClass, c.sort, c.page);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.test(refs);
        });

    }

    private Collection<Case> buildSelectCase() {
        return Arrays.asList(
            // long eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.EQUALS,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long not eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(3, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3, Long.MAX_VALUE};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.GREATER_THAN,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long >=
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(4, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long <
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.LESS_THAN,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(1, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long <=
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(longField, 2L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(3, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // long in
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        longField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(longField, 2L),
                        new LongValue(longField, 76L)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(3, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // string eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringField,
                        ConditionOperator.EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'")
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(1, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // string no eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringField,
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(stringField, "\"@带有符号的中文@\"\'")
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(4, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // string like
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringField,
                        ConditionOperator.LIKE,
                        new StringValue(stringField, "中文")
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // decimal eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        decimalField,
                        ConditionOperator.EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(3, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // decimal no eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        decimalField,
                        ConditionOperator.NOT_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // decimal >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        decimalField,
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(decimalField, BigDecimal.ONE)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(1, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // decimal >=
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        decimalField,
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(decimalField, BigDecimal.ONE)
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(4, refs.size());
                    long[] expectedIds =
                        new long[]{Long.MAX_VALUE - 4, Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());

                    return true;
                }
            )
            ,
            // dateTimeField between
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        dateTimeField,
                        ConditionOperator.GREATER_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1))
                    )
                ).addAnd(
                    new Condition(
                        dateTimeField,
                        ConditionOperator.LESS_THAN,
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1))
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                    return true;
                }
            )
            ,
            // stringsField =
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringsField,
                        ConditionOperator.EQUALS,
                        new StringsValue(stringsField, "UNKNOWN")
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(1, refs.size());
                    long[] expectedIds = new long[]{Long.MAX_VALUE - 4};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                    return true;
                }
            )
            ,
            // stringsField in
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringsField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(stringsField, "UNKNOWN"),
                        new StringsValue(stringsField, "value3")
                    )
                ),
                entityClass,
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(4, refs.size());
                    long[] expectedIds = new long[]{
                        Long.MAX_VALUE - 4, Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1};
                    Assert.assertEquals(0,
                        refs.stream().filter(r -> Arrays.binarySearch(expectedIds, r.getId()) < 0).count());
                    return true;
                }
            )
            ,
            // empty
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        stringsField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(stringsField, "UNKNOWN"),
                        new StringsValue(stringsField, "value3")
                    )
                ),
                entityClass,
                Page.emptyPage(),
                refs -> {
                    Assert.assertEquals(0, refs.size());
                    return true;
                }
            )
        );
    }

    // 初始化数据
    private void initData(SphinxQLIndexStorage storage) throws Exception {
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

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new TakeTurnsSelector<>(dataSourcePackage.getIndexWriter());

    }

    private Selector<DataSource> buildSearchDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new TakeTurnsSelector<>(dataSourcePackage.getIndexSearch());

    }

    private static class Case {
        private Conditions conditions;
        private IEntityClass entityClass;
        private Page page;
        private Sort sort;
        private Predicate<? super Collection<EntityRef>> check;

        public Case(Conditions conditions, IEntityClass entityClass, Page page,
                    Predicate<? super Collection<EntityRef>> check) {
            this(conditions, entityClass, page, check, null);
        }

        public Case(Conditions conditions, IEntityClass entityClass, Page page,
                    Predicate<? super Collection<EntityRef>> check, Sort sort) {
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.page = page;
            this.check = check;
            this.sort = sort;
        }
    }
} 
