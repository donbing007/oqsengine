package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.lock.LockHelper;
import com.xforceplus.ultraman.oqsengine.common.lock.process.ProcessLockFactory;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.SQLCdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.DevOpsRebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.LockExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Ignore;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * desc :
 * name : AbstractContainer
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
@Ignore
public abstract class AbstractContainer {
    protected static GenericContainer mysql;
    protected static GenericContainer manticore0;
    protected static GenericContainer manticore1;
    protected static GenericContainer searchManticore;

    protected static DataSourcePackage dataSourcePackage;

    protected static LongIdGenerator idGenerator;
    protected static DataSource dataSource;
    protected static SQLCdcErrorStorage cdcErrorStorage;
    protected static SQLMasterStorage masterStorage;
    protected static SphinxQLIndexStorage indexStorage;
    protected static StorageStrategyFactory masterStorageStrategyFactory;
    protected static CommitIdStatusService commitIdStatusService;
    protected static TransactionManager transactionManager;
    protected static TransactionExecutor masterTransactionExecutor;
    protected static DevOpsRebuildIndexExecutor taskExecutor;
    protected static String tableName = "oqsbigentity";
    protected static String cdcErrorsTableName = "cdcerrors";
    protected static String rebuildTableName = "devopstasks";

    static {
        Network network = Network.newNetwork();
        mysql = new GenericContainer("mysql:5.7")
                .withNetwork(network)
                .withNetworkAliases("mysql")
                .withExposedPorts(3306)
                .withEnv("MYSQL_DATABASE", "oqsengine")
                .withEnv("MYSQL_ROOT_USERNAME", "root")
                .withEnv("MYSQL_ROOT_PASSWORD", "root")
                .withClasspathResourceMapping("mastdb.sql", "/docker-entrypoint-initdb.d/1.sql", BindMode.READ_ONLY)
                .waitingFor(Wait.forListeningPort());
        mysql.start();

        manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore0")
                .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
        manticore0.start();

        manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore1")
                .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
        manticore1.start();

        searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("searchManticore")
                .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .dependsOn(manticore0, manticore1)
                .waitingFor(Wait.forListeningPort());
        searchManticore.start();

        System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
        System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

        System.setProperty("MANTICORE0_HOST", manticore0.getContainerIpAddress());
        System.setProperty("MANTICORE0_PORT", manticore0.getFirstMappedPort().toString());

        System.setProperty("MANTICORE1_HOST", manticore1.getContainerIpAddress());
        System.setProperty("MANTICORE1_PORT", manticore1.getFirstMappedPort().toString());

        System.setProperty("SEARCH_MANTICORE_HOST", searchManticore.getContainerIpAddress());
        System.setProperty("SEARCH_MANTICORE_PORT", searchManticore.getFirstMappedPort().toString());

        System.setProperty(
                "MYSQL_JDBC_URL",
                String.format("jdbc:p6spy:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                        System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

        System.setProperty("MANTICORE_WRITE0_JDBC_URL",
                String.format("jdbc:p6spy:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                        System.getProperty("MANTICORE0_HOST"), System.getProperty("MANTICORE0_PORT")));

        System.setProperty("MANTICORE_WRITE1_JDBC_URL",
                String.format("jdbc:p6spy:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                        System.getProperty("MANTICORE1_HOST"), System.getProperty("MANTICORE1_PORT")));

        System.setProperty("MANTICORE_SEARCH_JDBC_URL",
                String.format("jdbc:p6spy:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                        System.getProperty("SEARCH_MANTICORE_HOST"), System.getProperty("SEARCH_MANTICORE_PORT")));

        System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");

        System.out.println(System.getProperty("MANTICORE_WRITE0_JDBC_URL"));
        System.out.println(System.getProperty("MANTICORE_WRITE1_JDBC_URL"));
    }

    protected static void start() throws Exception {
        dataSourcePackage = DataSourceFactory.build();

        if (transactionManager == null) {
            long commitId = 0;
            commitIdStatusService = mock(CommitIdStatusServiceImpl.class);
            when(commitIdStatusService.save(commitId)).thenReturn(commitId++);

            transactionManager = new DefaultTransactionManager(
                    new IncreasingOrderLongIdGenerator(0), new IncreasingOrderLongIdGenerator(0));
        }

        idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));
        initMaster();
        initIndex();
        initDevOps();
    }

    protected static void close() {
        dataSourcePackage.close();
    }

    protected static DataSource buildDataSourceSelectorMaster() {
        return dataSourcePackage.getMaster().get(0);
    }

    private static void initMaster() throws Exception {

        dataSource = buildDataSourceSelectorMaster();

        masterTransactionExecutor = new AutoJoinTransactionExecutor(
                transactionManager, new SqlConnectionTransactionResourceFactory(tableName, commitIdStatusService));


        masterStorageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        masterStorageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        masterStorageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        IEntityValueBuilder<String> entityValueBuilder = new SQLJsonIEntityValueBuilder();
        ReflectionTestUtils.setField(entityValueBuilder, "storageStrategyFactory", masterStorageStrategyFactory);

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(masterStorageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        masterStorage = new SQLMasterStorage();
        ReflectionTestUtils.setField(masterStorage, "masterDataSource", dataSource);
        ReflectionTestUtils.setField(masterStorage, "transactionExecutor", masterTransactionExecutor);
        ReflectionTestUtils.setField(masterStorage, "storageStrategyFactory", masterStorageStrategyFactory);
        ReflectionTestUtils.setField(masterStorage, "entityValueBuilder", entityValueBuilder);
        ReflectionTestUtils.setField(masterStorage, "conditionsBuilderFactory", sqlJsonConditionsBuilderFactory);
        masterStorage.setTableName(tableName);
        masterStorage.init();
    }

    private static void initIndex() throws SQLException, InterruptedException {
        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector();
        DataSource searchDataSource = buildSearchDataSource();

        TransactionExecutor executor = new AutoJoinTransactionExecutor(transactionManager,
                new SphinxQLTransactionResourceFactory());

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();

        Selector<String> indexWriteIndexNameSelector =
                new SuffixNumberHashSelector("oqsindex", 3);

        indexStorage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(indexStorage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "searchDataSource", searchDataSource);
        ReflectionTestUtils.setField(indexStorage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(indexStorage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(indexStorage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(indexStorage, "indexWriteIndexNameSelector", indexWriteIndexNameSelector);
        indexStorage.setSearchIndexName("oqsindex");
        indexStorage.setMaxSearchTimeoutMs(1000);
        indexStorage.init();
    }

    private static void initDevOps() throws Exception {

        DataSource devOpsDataSource = buildDevOpsDataSource();

        cdcErrorStorage = new SQLCdcErrorStorage();
        ReflectionTestUtils.setField(cdcErrorStorage, "devOpsDataSource", devOpsDataSource);
        cdcErrorStorage.setCdcErrorRecordTable(cdcErrorsTableName);
        cdcErrorStorage.init();

        initTaskStorage(devOpsDataSource);
    }

    private static void initTaskStorage(DataSource devOpsDataSource) throws IllegalAccessException, InstantiationException {

        SQLTaskStorage sqlTaskStorage = new SQLTaskStorage();
        ReflectionTestUtils.setField(sqlTaskStorage, "devOpsDataSource", devOpsDataSource);
        sqlTaskStorage.setTable(rebuildTableName);

        LockExecutor lockExecutor = new LockExecutor();
        ReflectionTestUtils.setField(lockExecutor, "lockFactory",
                new ProcessLockFactory(LockHelper.ProcessHandlerLevel.LOCAL.name()));

        taskExecutor = new DevOpsRebuildIndexExecutor(10, 3000, 30000,
                100, 30, 300, 3000, 100);

        ReflectionTestUtils.setField(taskExecutor, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(taskExecutor, "sqlTaskStorage", sqlTaskStorage);
        ReflectionTestUtils.setField(taskExecutor, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(taskExecutor, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(taskExecutor, "lockExecutor", lockExecutor);
        taskExecutor.init();
    }

    private static DataSource buildDevOpsDataSource() {
        return dataSourcePackage.getDevOps();
    }

    private static Selector<DataSource> buildWriteDataSourceSelector() {
        return new HashSelector<>(dataSourcePackage.getIndexWriter());
    }


    private static DataSource buildSearchDataSource() {
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
            st.executeUpdate("truncate table oqsindex2");
            st.close();
            conn.close();
        }

        DataSource ds = dataSourcePackage.getDevOps();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate("truncate table " + cdcErrorsTableName);
        st.executeUpdate("truncate table " + rebuildTableName);
        st.close();
        conn.close();
    }
}
