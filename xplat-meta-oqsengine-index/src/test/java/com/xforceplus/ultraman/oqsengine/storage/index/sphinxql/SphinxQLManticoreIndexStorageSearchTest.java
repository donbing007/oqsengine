package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 搜索测试.
 *
 * @author dongbin
 * @version 0.1 2021/05/18 15:51
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MANTICORE})
public class SphinxQLManticoreIndexStorageSearchTest {

    private IEntityField baseStringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE)
        .withFieldType(FieldType.STRING)
        .withName("base-string")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build();
    private IEntityClass baseEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("base")
        .withField(baseStringField)
        .build();

    // 第一个子类.
    private IEntityField firstNameField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 1)
        .withFieldType(FieldType.STRING)
        .withName("name")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build();
    private IEntityClass firstEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("first")
        .withField(firstNameField)
        .withFather(baseEntityClass)
        .build();

    // 第二个子类.
    private IEntityField secondNameField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 2)
        .withFieldType(FieldType.STRING)
        .withName("name")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.WILDCARD)).build();
    private IEntityClass secondEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withLevel(1)
        .withCode("second")
        .withField(secondNameField)
        .withFather(baseEntityClass)
        .build();

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
    private StorageStrategyFactory storageStrategyFactory;

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

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        tokenizerFactory = new DefaultTokenizerFactory();

        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new SphinxQLStringsStorageStrategy());

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

    @Test
    public void testSearch() throws Exception {

        for (Case c : buildCase()) {
            Collection<EntityRef> refs = storage.search(
                c.config,
                c.entityClasses
            );

            long[] expectedIds = c.expectedSupplie.get();
            Arrays.sort(expectedIds);
            Assert.assertEquals(
                String.format("%s check length failed.", c.description), expectedIds.length, refs.size());
            for (EntityRef ref : refs) {
                Assert
                    .assertTrue(String.format("%s validation failed to find expected %d.", c.description, ref.getId()),
                        Arrays.binarySearch(expectedIds, ref.getId()) >= 0);
            }
        }
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                "精确搜索,第一个子类第二个子类.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.NOT)
                    .withCode(firstNameField.name())
                    .withValue("第一个类的数据").build(),
                new IEntityClass[] {
                    firstEntityClass, secondEntityClass
                },
                () -> {
                    long[] ids = new long[10];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - i;
                    }
                    return ids;
                }
            ),
            new Case(
                "分词搜索,只能搜索出一半.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withCode(firstNameField.name())
                    .withValue("一个类").build(),
                new IEntityClass[] {
                    firstEntityClass, secondEntityClass
                },
                () -> {
                    long[] ids = new long[5];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - (i + 5);
                    }
                    return ids;
                }
            ),
            new Case(
                "所有数据,不指定元信息.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.NOT)
                    .withCode(firstNameField.name())
                    .withValue("第一个类的数据").build(),
                new IEntityClass[0],
                () -> {
                    long[] ids = new long[10];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - i;
                    }
                    return ids;
                }
            )
        );
    }

    static class Case {
        private String description;
        private SearchConfig config;
        private IEntityClass[] entityClasses;
        private Supplier<long[]> expectedSupplie;

        public Case(String description, SearchConfig config, IEntityClass[] entityClasses,
                    Supplier<long[]> expectedSupplie) {
            this.description = description;
            this.config = config;
            this.entityClasses = entityClasses;
            this.expectedSupplie = expectedSupplie;
        }
    }

    private void initData() throws Exception {
        final int size = 10;
        expectedDatas = new ArrayList<>();
        OriginalEntity oe;
        int index = 0;
        int max = index + size / 2;
        for (; index < max; index++) {
            oe = OriginalEntity.Builder.anOriginalEntity()
                .withId(Long.MAX_VALUE - index)
                .withCommitid(0)
                .withDeleted(false)
                .withAttribute(buildAttributeKey(firstNameField.id()), "第一个类的数据")
                .withVersion(0)
                .withCreateTime(System.currentTimeMillis())
                .withUpdateTime(System.currentTimeMillis())
                .withOp(OperationType.CREATE.getValue())
                .withEntityClass(firstEntityClass)
                .build();
            expectedDatas.add(oe);
        }

        max = index + size / 2;
        for (; index < max; index++) {
            oe = OriginalEntity.Builder.anOriginalEntity()
                .withId(Long.MAX_VALUE - index)
                .withCommitid(0)
                .withDeleted(false)
                .withAttribute(buildAttributeKey(secondEntityClass.id()), "第一个类的数据")
                .withVersion(0)
                .withCreateTime(System.currentTimeMillis())
                .withUpdateTime(System.currentTimeMillis())
                .withOp(OperationType.CREATE.getValue())
                .withEntityClass(secondEntityClass)
                .build();
            expectedDatas.add(oe);
        }

        storage.saveOrDeleteOriginalEntities(expectedDatas);
    }

    private String buildAttributeKey(long id) {
        return String.format("%dS", id);
    }

    private DataSource buildSearchDataSourceSelector() {
        if (dataSourcePackage == null) {
            dataSourcePackage = DataSourceFactory.build(true);
        }

        return dataSourcePackage.getIndexSearch().get(0);

    }

    private Selector<DataSource> buildWriteDataSourceSelector() {
        if (dataSourcePackage == null) {
            dataSourcePackage = DataSourceFactory.build(true);
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

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
