package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSONArray;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author dongbin
 * @version 0.1 2021/3/10 14:01
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MANTICORE})
public class SphinxQLManticoreIndexStorageSearchTest {
    private static StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();

    static {
        // 浮点数转换处理.
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    private TransactionManager transactionManager;
    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private DataSourcePackage dataSourcePackage;
    private Selector<String> indexWriteIndexNameSelector;
    private Selector<DataSource> writeDataSourceSelector;
    private ExecutorService threadPool;
    private SphinxQLManticoreIndexStorage storage;
    private Collection<OriginalEntity> expectedDatas;
    private TokenizerFactory tokenizerFactory;

    //-------------level 0--------------------
    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 1)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 2)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 3)
        .withFieldType(FieldType.LONG)
        .withName("l1-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 4)
        .withFieldType(FieldType.STRING)
        .withName("l1-string")
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
        .build();

    //-------------level 2--------------------
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 5)
        .withFieldType(FieldType.STRING)
        .withName("l2-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2TimeField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 6)
        .withFieldType(FieldType.DATETIME)
        .withName("l2-time")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2EnumField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 7)
        .withFieldType(FieldType.ENUM)
        .withName("l2-enum")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2DecField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 8)
        .withFieldType(FieldType.DECIMAL)
        .withName("l2-dec")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l2EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withLevel(2)
        .withCode("l2")
        .withField(l2StringField)
        .withField(l2TimeField)
        .withField(l2EnumField)
        .withField(l2DecField)
        .withFather(l1EntityClass)
        .build();

    @Before
    public void before() throws Exception {

        redisClient = RedisClient.create(
            String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        writeDataSourceSelector = buildWriteDataSourceSelector();
        DataSource searchDataSource = buildSearchDataSourceSelector();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        transactionManager = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();

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

        tokenizerFactory = new DefaultTokenizerFactory();

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        sphinxQLConditionsBuilderFactory.init();

        threadPool = Executors.newFixedThreadPool(3);

        storage = new SphinxQLManticoreIndexStorage();
        ReflectionTestUtils.setField(storage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(storage, "searchTransactionExecutor", searchExecutor);
        ReflectionTestUtils.setField(storage, "writeTransactionExecutor", writeExecutor);
        ReflectionTestUtils.setField(storage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "indexWriteIndexNameSelector", indexWriteIndexNameSelector);
        ReflectionTestUtils.setField(storage, "tokenizerFactory", tokenizerFactory);
        ReflectionTestUtils.setField(storage, "threadPool", threadPool);
        storage.setSearchIndexName("oqsindex");
        storage.setMaxSearchTimeoutMs(1000);
        storage.init();

        initData();
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

        commitIdStatusService.destroy();

        redisClient.connect().sync().flushall();
        redisClient.shutdown();

        ExecutorHelper.shutdownAndAwaitTermination(threadPool);
    }

    /**
     * 测试如果使用Page.emptyPage()实例做为分页,那么page的数据总量还是会被填充的.
     *
     * @throws Exception
     */
    @Test
    public void testEmptyPageCount() throws Exception {
        Page page = Page.emptyPage();
        storage.select(
            Conditions.buildEmtpyConditions(),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(page)
                .withCommitId(0)
                .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                .build()
        );
        Assert.assertEquals(page.getTotalCount(), expectedDatas.size());

    }

    @Test
    public void testSelect() throws Exception {
        Collection<EntityRef> refs;
        for (Case c : buildSelectCases()) {
            refs = storage.select(c.conditions, c.entityClass, c.selectConfig);

            long[] expectedIds = c.expectedIds;
            if (expectedIds == null) {
                expectedIds = expectedDatas.stream().mapToLong(oe -> oe.getId()).toArray();
            }
            Arrays.sort(expectedIds);
            Assert.assertEquals(
                String.format("%s check length failed.", c.description), expectedIds.length, refs.size());
            for (EntityRef ref : refs) {
                Assert.assertTrue(String.format("%s validation failed to find expected %d.", c.description, ref.getId()),
                    Arrays.binarySearch(expectedIds, ref.getId()) >= 0);
            }

            if (c.otherCheck != null) {
                String r = (String) c.otherCheck.apply(new SelectResult(refs, c.selectConfig.getPage()));
                if (r != null && r.length() > 0) {
                    Assert.fail(String.format("%s validation failed due to %s.", c.description, r));
                }
            }
        }
    }

    private Collection<Case> buildSelectCases() {
        return Arrays.asList(
            new Case(
                "id in (not exist)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.ID_ENTITY_FIELD,
                            ConditionOperator.MULTIPLE_EQUALS,
                            new LongValue(EntityField.ID_ENTITY_FIELD, 10000000L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                    .build(),
                new long[0],
                r -> {

                    if (!r.refs.isEmpty()) {
                        return String.format("No data is expected, but there is %d data.", r.refs.size());
                    }

                    return null;
                }
            )
            ,
            new Case(
                "sort with value",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                    .build(),
                null,
                r -> {
                    for (EntityRef ref : r.refs) {
                        if (ref.getOrderValue() == null) {
                            return "The sort is expected to return the value of the sort, but it does not.";
                        }
                    }
                    return null;
                }
            )
            ,
            new Case(
                "empty",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.emptyPage())
                    .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[0],
                r -> {
                    if (!r.refs.isEmpty()) {
                        return String.format("No data is expected, but there is %d data.", r.refs.size());
                    }

                    if (r.page.getTotalCount() != expectedDatas.size()) {
                        return String.format("The expected data total is %d, but it is actually up %d.",
                            expectedDatas.size(), r.page.getTotalCount());
                    }

                    return null;
                }
            )
            ,
            new Case(
                "order by id asc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null,
                r -> isSorted(r.refs, false) ? null : "Not expecting descending order"
            )
            ,
            new Case(
                "order by id desc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null,
                r -> isSorted(r.refs, true) ? null : "Not expecting descending order"
            )
            ,
            new Case(
                "first page",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(new Page(1, 5))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4
                }
            )
            ,
            new Case(
                "last page",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(new Page(2, 5))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null
            )
            ,
            new Case(
                "long eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 5932795534250140672L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 4
                }
            )
            ,
            new Case(
                "long not equals",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 5932795534250140672L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "long >",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "long <",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 3,
                }
            )
            ,
            new Case(
                "long >=",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "long <=",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 7,
                }
            )
            ,
            new Case(
                "long in",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L),
                        new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 7,
                }
            )
            ,
            new Case(
                "String eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "15841884807")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 1
                }
            )
            ,
            new Case(
                "String no eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "15841884807")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 3
                }
            )
            ,
            new Case(
                "String like 熊鹤轩 (熊鹤轩)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l0-string").get(), "熊鹤轩")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "String like 18159301250 (301)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "301")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "String like 18159301250 (3012)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "3012")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "String like 18159301250 (30125)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "30125")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "String like 18159301250 (301250)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "301250")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "String like 18159301250 (9301250)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "9301250")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "decimal eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "decimal not eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.NOT_EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7,
                    Long.MAX_VALUE - 8
                }
            )
            ,
            new Case(
                "decimal >",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8
                }
            )
            ,
            new Case(
                "decimal between",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    ))
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.LESS_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("61894.0"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 7
                }
            )
            ,
            new Case(
                "strings eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "fuchsia")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 3
                }
            )
            ,
            new Case(
                "strings in",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "teal"),
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "fuchsia")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 3
                }
            )
            ,
            new Case(
                "update time desc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.UPDATE_TIME_FILED))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7,
                    Long.MAX_VALUE - 8, Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "l0-long or l0-long",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                        )
                    ).addOr(
                    new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                    )
                ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE, Long.MAX_VALUE - 7,
                }
            )
            ,
            new Case(
                "l0-long eq or l0-long no eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                        )
                    ).addOr(
                    new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                    )
                ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            )
            ,
            new Case(
                "l0-long > or l0-long =",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    ).addOr(
                    new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 2305210501598537472L)
                    )
                ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 2, Long.MAX_VALUE
                }
            )
            ,
            new Case(
                "l0-long > or l0-long = (filter l0-long !=)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    ).addOr(
                    new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 2305210501598537472L)
                    )
                ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .withDataAccessFitlerCondtitons(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    l2EntityClass.field("l0-long").get(),
                                    ConditionOperator.NOT_EQUALS,
                                    new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                                )
                            )
                    )
                    .build(),
                new long[]{
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 2
                }
            )
            ,
            new Case(
                "l0-long = and l0-long = (filter l0-long !=)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    ).addAnd(
                    new Condition(
                        l2EntityClass.field("l1-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 162L)
                    )
                ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .withDataAccessFitlerCondtitons(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    l2EntityClass.field("l0-long").get(),
                                    ConditionOperator.NOT_EQUALS,
                                    new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                                )
                            )
                    )
                    .build(),
                new long[]{
                }
            )
        );
    }

    // 判断是否有序
    private boolean isSorted(Collection<EntityRef> refs, boolean desc) {
        EntityRef lastRef = null;
        for (EntityRef ref : refs) {
            if (lastRef == null) {
                lastRef = ref;
            }

            int result = ref.compareTo(lastRef);
            // 降序判断
            if (desc) {

                if (result > 0) {
                    return false;
                }

            } else {
                // 升序判断

                if (result < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private static class SelectResult {
        private Collection<EntityRef> refs;
        private Page page;

        public SelectResult(Collection<EntityRef> refs, Page page) {
            this.refs = refs;
            this.page = page;
        }
    }

    private static class Case {
        private String description;
        private Conditions conditions;
        private IEntityClass entityClass;
        private SelectConfig selectConfig;
        private long[] expectedIds;
        private Function<? super SelectResult, ? super String> otherCheck;

        public Case(String description, Conditions conditions, IEntityClass entityClass, SelectConfig selectConfig, long[] expectedIds) {
            this.description = description;
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.selectConfig = selectConfig;
            this.expectedIds = expectedIds;
        }

        public Case(
            String description,
            Conditions conditions,
            IEntityClass entityClass,
            SelectConfig selectConfig,
            long[] expectedIds,
            Function<? super SelectResult, ? super String> otherCheck) {
            this.description = description;
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.selectConfig = selectConfig;
            this.expectedIds = expectedIds;
            this.otherCheck = otherCheck;
        }
    }

    // 初始化数据,以JSON格式读取当前类路径下的OriginalEntityTestData.json
    private void initData() throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("OriginalEntityTestData.json").toURI());
        String value = new String(Files.readAllBytes(path), "utf8");
        Collection<OriginalEntity> datas = JSONArray.parseArray(value, OriginalEntity.class);
        datas.stream().forEach(o -> {
            o.setEntityClass(l2EntityClass);
        });

        storage.saveOrDeleteOriginalEntities(datas);

        this.expectedDatas = datas;
    }

    private Selector<DataSource> buildWriteDataSourceSelector() {
        if (dataSourcePackage == null) {
            dataSourcePackage = DataSourceFactory.build(true);
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

    }

    private DataSource buildSearchDataSourceSelector() {
        if (dataSourcePackage == null) {
            dataSourcePackage = DataSourceFactory.build(true);
        }

        return dataSourcePackage.getIndexSearch().get(0);

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
}
