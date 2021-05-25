package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.lettuce.core.RedisClient;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * MantiocreIndexStorage Tester.
 *
 * @author dongbin
 * @version 1.0 03/08/2021
 * @since <pre>Mar 8, 2021</pre>
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MANTICORE})
public class SphinxQLManticoreIndexStorageTest {

    private Faker faker = new Faker(Locale.CHINA);

    private TransactionManager transactionManager;
    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private DataSourcePackage dataSourcePackage;
    private Selector<String> indexWriteIndexNameSelector;
    private Selector<DataSource> writeDataSourceSelector;
    private ExecutorService threadPool;
    private SphinxQLManticoreIndexStorage storage;
    private TokenizerFactory tokenizerFactory;
    private StorageStrategyFactory storageStrategyFactory;

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

        tokenizerFactory = new DefaultTokenizerFactory();

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        sphinxQLConditionsBuilderFactory.init();

        threadPool = Executors.newFixedThreadPool(3);

        storage = new SphinxQLManticoreIndexStorage();

        DataSource searchDataSource = buildSearchDataSourceSelector();
        TransactionExecutor searchExecutor =
            new AutoJoinTransactionExecutor(transactionManager, new SphinxQLTransactionResourceFactory(),
                new NoSelector<>(searchDataSource), new NoSelector<>("oqsindex"));
        TransactionExecutor writeExecutor =
            new AutoJoinTransactionExecutor(
                transactionManager, new SphinxQLTransactionResourceFactory(),
                writeDataSourceSelector, indexWriteIndexNameSelector);

        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        // 浮点数转换处理.
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        // 多值字符串
        storageStrategyFactory.register(FieldType.STRINGS, new SphinxQLStringsStorageStrategy());

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
     * 测试主库物理储存类型转换到索引储存时的转换是否正确.
     * 比如浮点数,在主库是以字符串非多值存在,但是索引需要以数字型的多值形式存在.
     */
    @Test
    public void testPhysicalStorageCorrect() throws Exception {

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l2DecField.id() + "S", "123.789")
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("123.789"))
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );

        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(Long.MAX_VALUE - 300, refs.stream().findFirst().get().getId());

        refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("123.789"))
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(Long.MAX_VALUE - 300, refs.stream().findFirst().get().getId());

        target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 600)
            .withAttribute(l0StringsField.id() + "S", "[RMB][JPY][USD]")
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );

        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(Long.MAX_VALUE - 600, refs.stream().findFirst().get().getId());
    }

    /**
     * 测试值中有特殊符号,单引号,双引号等.
     */
    @Test
    public void testSymbolValue() throws Exception {
        // 单引号
        StorageStrategy l0StorageStrategy = storageStrategyFactory.getStrategy(
            l2EntityClass.field("l0-string").get().type());
        StorageValue l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "1d'f"));

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l0-string").get(), "1d'f")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(target.getId(), refs.stream().findFirst().get().getId());

        target.setOp(OperationType.DELETE.getValue());
        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        // 双引号
        l0StorageStrategy = storageStrategyFactory.getStrategy(
            l2EntityClass.field("l0-string").get().type());
        l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "1d\"f"));

        target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 400)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        refs = storage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l0-string").get(), "1d\"f")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(target.getId(), refs.stream().findFirst().get().getId());

    }

    @Test
    public void testFuzzyValue() throws Exception {
        StorageStrategy l0StorageStrategy = storageStrategyFactory.getStrategy(
            l2EntityClass.field("l0-string").get().type());
        StorageValue l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "这是一个测试"));

        StorageStrategy l1StorageStrategy = storageStrategyFactory.getStrategy(
            l2EntityClass.field("l1-string").get().type());
        StorageValue l1StorageValue = l1StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l1-string").get(),
                "abcdefg"
            ));

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withAttribute(l1StorageValue.storageName(), l1StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        storage.saveOrDeleteOriginalEntities(Arrays.asList(target));

        List<String> attrs;
        try (Connection conn = buildSearchDataSourceSelector().getConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(
                    String.format(
                        "select %s from %s where %s = %d",
                        FieldDefine.ATTRIBUTEF, "oqsindex", FieldDefine.ID, target.getId()))) {

                    rs.next();

                    String attrf = rs.getString(FieldDefine.ATTRIBUTEF);
                    attrs = Arrays.asList(attrf.split(" "));
                }
            }
        }

        ShortStorageName shortStorageName = l0StorageValue.shortStorageName();
        Tokenizer tokenizer = tokenizerFactory.getTokenizer(l2EntityClass.field("l0-string").get());
        Iterator<String> words = tokenizer.tokenize(l0StorageValue.value().toString());
        while (words.hasNext()) {
            Assert.assertTrue(
                attrs.contains(
                    String.format("%s%s%s", shortStorageName.getPrefix(), words.next(), shortStorageName.getSuffix())));
        }
        Assert.assertTrue(
            attrs.contains(
                String.format("%s%s%s",
                    shortStorageName.getPrefix(), l0StorageValue.value().toString(), shortStorageName.getSuffix())));

        shortStorageName = l1StorageValue.shortStorageName();
        tokenizer = tokenizerFactory.getTokenizer(l2EntityClass.field("l1-string").get());
        words = tokenizer.tokenize(l1StorageValue.value().toString());
        while (words.hasNext()) {
            Assert.assertTrue(
                attrs.contains(
                    String.format("%s%s%s", shortStorageName.getPrefix(), words.next(), shortStorageName.getSuffix())));
        }
        Assert.assertTrue(
            attrs.contains(
                String.format("%s%s%s",
                    shortStorageName.getPrefix(), l1StorageValue.value().toString(), shortStorageName.getSuffix())));
    }

    @Test
    public void testSaveOriginalEntities() throws Exception {
        List<OriginalEntity> initDatas = new LinkedList<>();
        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));

        storage.saveOrDeleteOriginalEntities(initDatas);

        // 查询id降序
        Page page = Page.newSinglePage(1000);
        Collection<EntityRef> refs = storage.select(Conditions.buildEmtpyConditions(), l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(page).withCommitId(0).withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build());
        Assert.assertEquals(initDatas.size(), refs.size());
        Assert.assertEquals(initDatas.size(), page.getTotalCount());

        // 新创建2条
        List<OriginalEntity> processDatas = new LinkedList<>();
        processDatas.addAll(buildSyncData(OperationType.CREATE, 2, Long.MAX_VALUE - 11));

        // 删除2条
        OriginalEntity target = initDatas.get(0);
        OriginalEntity d1 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d1);
        target = initDatas.get(1);
        OriginalEntity d2 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d2);
        target = initDatas.get(2);
        OriginalEntity d3 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d3);

        // 更新两条
        target = initDatas.get(3);
        OriginalEntity u1 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withAttributes(Arrays.asList(target.getAttributes()))
            .withCommitid(target.getCommitid())
            .withVersion(target.getVersion())
            .withEntityClass(target.getEntityClass())
            .withCreateTime(target.getCreateTime())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.UPDATE.getValue())
            .withTx(target.getTx())
            .withVersion(target.getVersion() + 1)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        processDatas.add(u1);

        target = initDatas.get(4);
        OriginalEntity u2 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withAttributes(Arrays.asList(target.getAttributes()))
            .withCommitid(target.getCommitid())
            .withVersion(target.getVersion())
            .withEntityClass(target.getEntityClass())
            .withCreateTime(target.getCreateTime())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.UPDATE.getValue())
            .withTx(target.getTx())
            .withVersion(target.getVersion() + 1)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        processDatas.add(u2);

        storage.saveOrDeleteOriginalEntities(processDatas);

        page = Page.newSinglePage(1000);
        refs = storage.select(Conditions.buildEmtpyConditions(), l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(page).withCommitId(0).withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build());
        Assert.assertEquals(10 + 2 - 3, refs.size());
        Assert.assertEquals(10 + 2 - 3, page.getTotalCount());
    }

    @Test
    public void testClean() throws Exception {
        List<OriginalEntity> initDatas = new LinkedList<>();
        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));

        storage.saveOrDeleteOriginalEntities(initDatas);
        storage.clean(l2EntityClass, 10, 0, Long.MAX_VALUE);

        Page page = Page.newSinglePage(1000);
        Collection<EntityRef> refs = storage.select(Conditions.buildEmtpyConditions(), l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(page).withCommitId(0).build());
        Assert.assertEquals(0, refs.size());
        Assert.assertEquals(0, page.getTotalCount());

        initDatas.clear();

        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));
        storage.saveOrDeleteOriginalEntities(initDatas);
        storage.clean(l2EntityClass, 0, 0, Long.MAX_VALUE);

        page = Page.newSinglePage(1000);
        refs = storage.select(Conditions.buildEmtpyConditions(), l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(page).withCommitId(0).build());
        Assert.assertEquals(initDatas.size(), refs.size());
        Assert.assertEquals(initDatas.size(), page.getTotalCount());
    }

    // 构造同步数据
    private Collection<OriginalEntity> buildSyncData(OperationType op, int size, long lastId) {
        return IntStream.range(0, size).mapToObj(i -> buildSyncData(op, i, l2EntityClass, lastId))
            .collect(Collectors.toList());
    }

    private OriginalEntity buildSyncData(OperationType op, int index, IEntityClass entityClass, long lastId) {
        OriginalEntity.Builder builder = OriginalEntity.Builder.anOriginalEntity()
            .withId(lastId - index)
            .withOp(op.getValue())
            .withCreateTime(System.currentTimeMillis())
            .withUpdateTime(System.currentTimeMillis())
            .withCommitid(index)
            .withTx(Integer.MAX_VALUE - index)
            .withDeleted(OperationType.DELETE == op ? true : false)
            .withEntityClass(l2EntityClass)
            .withOqsMajor(OqsVersion.MAJOR);

        List<Object> attrs = new LinkedList<>();
        entityClass.fields().stream().forEach(f -> {
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(f.type());
            StorageValue storageValue = null;
            switch (f.name()) {
                case "l0-long": {
                    storageValue = storageStrategy.toStorageValue(new LongValue(f,
                        faker.number().numberBetween(1, Long.MAX_VALUE)));
                    break;
                }
                case "l0-string": {
                    storageValue = storageStrategy.toStorageValue(new StringValue(f, faker.name().fullName()));
                    break;
                }
                case "l0-strings": {
                    StringBuilder buff = new StringBuilder();
                    buff.append("[").append(faker.color().name()).append("]");
                    buff.append("[").append(faker.color().name()).append("]");
                    buff.append("[").append(faker.color().name()).append("]");
                    storageValue = new StringStorageValue(
                        Long.toString(f.id()),
                        buff.toString(),
                        true
                    );
                    break;
                }
                case "l1-long": {
                    storageValue = storageStrategy.toStorageValue(
                        new LongValue(f, faker.number().numberBetween(100, 200)));
                    break;
                }
                case "l1-string": {
                    storageValue = storageStrategy.toStorageValue(
                        new StringValue(f, faker.phoneNumber().cellPhone()));
                    break;
                }
                case "l2-string": {
                    storageValue = storageStrategy.toStorageValue(
                        new StringValue(f, faker.idNumber().invalid()));
                    break;
                }
                case "l2-time": {
                    storageValue = storageStrategy.toStorageValue(
                        new DateTimeValue(f,
                            LocalDateTime.ofInstant(faker.date().birthday().toInstant(), ZoneId.systemDefault())));
                    break;
                }
                case "l2-enum": {
                    storageValue = storageStrategy.toStorageValue(new EnumValue(f, faker.color().name()));
                    break;
                }
                case "l2-dec": {
                    storageValue = new StringStorageValue(
                        Long.toString(f.id()),
                        Double.toString(faker.number().randomDouble(10, 100, 100000)),
                        true
                    );

                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Cannot process the field.[%s]", f.name()));
                }

            }

            while (storageValue != null) {
                attrs.add(storageValue.storageName());
                attrs.add(storageValue.value());

                storageValue = storageValue.next();
            }

        });

        builder.withAttributes(attrs);

        return builder.build();

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
