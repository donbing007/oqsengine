package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.DevOpsRebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLManticoreIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.lettuce.core.RedisClient;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.Ignore;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 抽像的devops启动容器.
 *
 * @author xujia 2020/11/22
 * @since 1.8
 */
@Ignore
public abstract class AbstractDevOpsContainer {

    protected StorageStrategyFactory masterStorageStrategyFactory;
    protected CommitIdStatusServiceImpl commitIdStatusService;
    protected IndexStorage indexStorage;
    protected TransactionManager transactionManager;
    protected DataSourcePackage dataSourcePackage;
    protected SQLMasterStorage masterStorage;
    protected DataSource dataSource;
    protected TransactionExecutor masterTransactionExecutor;
    protected RedisClient redisClient;

    protected LongIdGenerator idGenerator;
    protected ExecutorService executorService;
    protected DevOpsRebuildIndexExecutor taskExecutor;
    protected static String tableName = "oqsbigentity";

    protected static String rebuildTableName = "devopstasks";

    protected void start() throws Exception {
        dataSourcePackage = DataSourceFactory.build();

        if (transactionManager == null) {
            redisClient = RedisClient.create(
                String.format("redis://%s:%s", System.getProperty("REDIS_HOST"), System.getProperty("REDIS_PORT")));
            commitIdStatusService = new CommitIdStatusServiceImpl();
            ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
            commitIdStatusService.init();


            transactionManager = DefaultTransactionManager.Builder.anDefaultTransactionManager()
                .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
                .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(new DoNothingCacheEventHandler())
                .withWaitCommitSync(false)
                .build();
        }

        idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));
        initMaster();
        initIndex();
        initDevOps();
    }


    protected void close() {
        commitIdStatusService.destroy();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();

        dataSourcePackage.close();

        if (null != executorService) {
            ExecutorHelper.shutdownAndAwaitTermination(executorService, 3600);
        }
    }

    protected DataSource buildDataSourceSelectorMaster() {
        return dataSourcePackage.getMaster().get(0);
    }

    private void initDevOps() throws Exception {
        initTaskStorage(buildDevOpsDataSource());
    }

    private void initMaster() throws Exception {

        dataSource = buildDataSourceSelectorMaster();

        masterTransactionExecutor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory(tableName),
            NoSelector.build(dataSource), NoSelector.build(tableName));


        masterStorageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        masterStorageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        masterStorageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(masterStorageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        masterStorage = new SQLMasterStorage();
        ReflectionTestUtils.setField(masterStorage, "transactionExecutor", masterTransactionExecutor);
        ReflectionTestUtils.setField(masterStorage, "storageStrategyFactory", masterStorageStrategyFactory);
        ReflectionTestUtils.setField(masterStorage, "conditionsBuilderFactory", sqlJsonConditionsBuilderFactory);
        masterStorage.setTableName(tableName);
        masterStorage.setQueryTimeout(30000);
        masterStorage.init();
    }

    private void initIndex() throws SQLException, InterruptedException {
        final Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector();
        final DataSource searchDataSource = buildSearchDataSource();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        final TransactionExecutor writeExecutor = new AutoJoinTransactionExecutor(transactionManager,
            new SphinxQLTransactionResourceFactory(), writeDataSourceSelector, NoSelector.build("oqsindex"));
        final TransactionExecutor searchExecutor = new AutoJoinTransactionExecutor(transactionManager,
            new SphinxQLTransactionResourceFactory(), NoSelector.build(searchDataSource), NoSelector.build("oqsindex"));

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();

        Selector<String> indexWriteIndexNameSelector = new SuffixNumberHashSelector("oqsindex", 2);

        final ExecutorService executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        indexStorage = new SphinxQLManticoreIndexStorage();
        ReflectionTestUtils.setField(indexStorage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "indexWriteIndexNameSelector", indexWriteIndexNameSelector);
        ReflectionTestUtils.setField(indexStorage, "searchTransactionExecutor", searchExecutor);
        ReflectionTestUtils.setField(indexStorage, "writeTransactionExecutor", writeExecutor);
        ReflectionTestUtils
            .setField(indexStorage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(indexStorage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(indexStorage, "threadPool", executorService);

        ((SphinxQLManticoreIndexStorage) indexStorage).setMaxSearchTimeoutMs(1000);
        indexStorage.init();
    }


    private void initTaskStorage(DataSource devOpsDataSource) {

        SQLTaskStorage sqlTaskStorage = new SQLTaskStorage();
        ReflectionTestUtils.setField(sqlTaskStorage, "devOpsDataSource", devOpsDataSource);
        sqlTaskStorage.setTable(rebuildTableName);


        taskExecutor = new DevOpsRebuildIndexExecutor(10, 3000, 30000, 300);

        ReflectionTestUtils.setField(taskExecutor, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(taskExecutor, "sqlTaskStorage", sqlTaskStorage);
        ReflectionTestUtils.setField(taskExecutor, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(taskExecutor, "idGenerator", idGenerator);
    }

    private DataSource buildDevOpsDataSource() {
        return dataSourcePackage.getDevOps();
    }

    private Selector<DataSource> buildWriteDataSourceSelector() {
        return new HashSelector<>(dataSourcePackage.getIndexWriter());
    }


    private DataSource buildSearchDataSource() {
        return dataSourcePackage.getIndexSearch().get(0);
    }

    public void clear() throws SQLException {
        for (DataSource ds : dataSourcePackage.getMaster()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("truncate table oqsbigentity");
                }
            }
        }

        for (DataSource ds : dataSourcePackage.getIndexWriter()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("truncate table oqsindex0");
                    st.executeUpdate("truncate table oqsindex1");
                }
            }
        }

        DataSource ds = dataSourcePackage.getDevOps();
        try (Connection conn = ds.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("truncate table " + rebuildTableName);
            }
        }
    }
}
