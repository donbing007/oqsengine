package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.SQLCdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxSyncExecutor;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
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
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.lettuce.core.RedisClient;
import org.junit.Ignore;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : AbstractContainer
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
@Ignore
public abstract class CDCAbstractContainer {

    protected StorageStrategyFactory masterStorageStrategyFactory;

    protected CommitIdStatusServiceImpl commitIdStatusService;
    protected SphinxSyncExecutor sphinxSyncExecutor;
    protected IndexStorage indexStorage;

    protected MetaManager metaManager;

    protected TransactionManager transactionManager;
    protected DataSourcePackage dataSourcePackage;
    protected SQLCdcErrorStorage cdcErrorStorage;
    protected SQLMasterStorage masterStorage;
    protected DataSource dataSource;
    protected TransactionExecutor masterTransactionExecutor;
    protected RedisClient redisClient;
    protected String tableName = "oqsbigentity";
    protected String cdcErrors = "cdcerrors";

    protected SingleCDCConnector singleCDCConnector;
    protected ExecutorService executorService;

    protected ConsumerService initAll(boolean isMockIndex) throws Exception {
        singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init(System.getProperty("CANAL_HOST"), Integer.parseInt(System.getProperty("CANAL_PORT")),
            System.getProperty("CANAL_DESTINATION"), "root", "root");

        dataSourcePackage = DataSourceFactory.build(true);

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
                .build();
        }

        initMaster();
        if (!isMockIndex) {
            initIndex();
        } else {
            indexStorage = new MockIndexStorage();
        }
        initDevOps();

        return initConsumerService();
    }

    protected void closeAll() {
        commitIdStatusService.destroy();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        dataSourcePackage.close();
        if (null != executorService) {
            executorService.shutdownNow();
        }
    }

    protected DataSource buildDataSourceSelectorMaster() {
        return dataSourcePackage.getMaster().get(0);
    }

    private void initIndex() throws SQLException, InterruptedException {
        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector();
        DataSource searchDataSource = buildSearchDataSource();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor writeExecutor = new AutoJoinTransactionExecutor(transactionManager,
            new SphinxQLTransactionResourceFactory(), writeDataSourceSelector, NoSelector.build("oqsindex"));
        TransactionExecutor searchExecutor = new AutoJoinTransactionExecutor(transactionManager,
            new SphinxQLTransactionResourceFactory(), NoSelector.build(searchDataSource), NoSelector.build("oqsindex"));

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();

        Selector<String> indexWriteIndexNameSelector = new SuffixNumberHashSelector("oqsindex", 2);

        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        indexStorage = new SphinxQLManticoreIndexStorage();
        ReflectionTestUtils.setField(indexStorage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "indexWriteIndexNameSelector", indexWriteIndexNameSelector);
        ReflectionTestUtils.setField(indexStorage, "searchTransactionExecutor", searchExecutor);
        ReflectionTestUtils.setField(indexStorage, "writeTransactionExecutor", writeExecutor);
        ReflectionTestUtils.setField(indexStorage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(indexStorage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(indexStorage, "threadPool", executorService);

        ((SphinxQLManticoreIndexStorage) indexStorage).setMaxSearchTimeoutMs(1000);
        indexStorage.init();
    }

    private void initDevOps() throws Exception {

        DataSource devOpsDataSource = buildDevOpsDataSource();

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(masterStorageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        cdcErrorStorage = new SQLCdcErrorStorage();
        ReflectionTestUtils.setField(cdcErrorStorage, "devOpsDataSource", devOpsDataSource);
        cdcErrorStorage.setCdcErrorRecordTable(cdcErrors);
        cdcErrorStorage.init();
    }

    private ConsumerService initConsumerService() throws Exception {

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());


        metaManager = new EntityClassBuilder();
        sphinxSyncExecutor = new SphinxSyncExecutor();


        ReflectionTestUtils.setField(sphinxSyncExecutor, "sphinxQLIndexStorage", indexStorage);
        ReflectionTestUtils.setField(sphinxSyncExecutor, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(sphinxSyncExecutor, "cdcErrorStorage", cdcErrorStorage);
        ReflectionTestUtils.setField(sphinxSyncExecutor, "seqNoGenerator",
            new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0)));
        ReflectionTestUtils.setField(sphinxSyncExecutor, "metaManager", metaManager);

        ConsumerService consumerService = new SphinxConsumerService();

        ReflectionTestUtils.setField(consumerService, "sphinxSyncExecutor", sphinxSyncExecutor);

        return consumerService;
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
        masterStorage.init();
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
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table oqsbigentity");
            st.close();
            conn.close();
        }

        for (DataSource ds : dataSourcePackage.getIndexWriter()) {
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table oqsindex0");
            st.executeUpdate("truncate table oqsindex1");
            st.close();
            conn.close();
        }
    }

    protected class MockIndexStorage implements IndexStorage {

        public int error = 0;

        @Override
        public long clean(IEntityClass entityClass, long maintainId, long start, long end) throws SQLException {
            return 0;
        }

        @Override
        public void saveOrDeleteOriginalEntities(Collection<OriginalEntity> originalEntities) throws SQLException {
            error ++;

            if (error < 3) {
                throw new SQLException("mock error");
            }
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config) throws SQLException {
            return null;
        }
    }
}
