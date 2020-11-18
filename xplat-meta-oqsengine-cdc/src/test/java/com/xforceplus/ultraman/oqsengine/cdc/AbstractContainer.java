package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxSyncExecutor;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
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
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * desc :
 * name : AbstractContainer
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
@Ignore
public abstract class AbstractContainer {
    protected static GenericContainer manticore0;
    protected static GenericContainer manticore1;
    protected static GenericContainer searchManticore;

    protected static DockerComposeContainer environment;

    protected static GenericContainer mysql;
    protected static GenericContainer cannal;


    protected StorageStrategyFactory masterStorageStrategyFactory;

    protected CommitIdStatusService commitIdStatusService;

    static {
            Network network = Network.newNetwork();
            mysql = new GenericContainer("mysql:5.7")
                    .withNetwork(network)
                    .withNetworkAliases("mysql")
                    .withExposedPorts(3306)
                    .withEnv("MYSQL_DATABASE", "oqsengine")
                    .withEnv("MYSQL_ROOT_USERNAME", "root")
                    .withEnv("MYSQL_ROOT_PASSWORD", "xplat")
                    .withClasspathResourceMapping("mastdb.sql", "/docker-entrypoint-initdb.d/1.sql", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("mysql.cnf", "/etc/my.cnf", BindMode.READ_ONLY)
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

            cannal = new GenericContainer("canal/canal-server:v1.1.4")
                    .withNetwork(network)
                    .withNetworkAliases("cannal")
                    .withExposedPorts(11111)
                    .withEnv("canal.instance.mysql.slaveId", "12")
                    .withEnv("canal.auto.scan", "false")
                    .withEnv("canal.destinations", "nly-v1")
                    .withEnv("canal.instance.master.address", "mysql:3306")
                    .withEnv("canal.instance.dbUsername", "root")
                    .withEnv("canal.instance.dbPassword", "xplat")
                    .withEnv("canal.instance.filter.regex", ".*\\.oqsbigentity.*")
                    .dependsOn(mysql)
                    .waitingFor(Wait.forListeningPort());
            cannal.start();

            System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
            System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

            System.setProperty("MANTICORE0_HOST", manticore0.getContainerIpAddress());
            System.setProperty("MANTICORE0_PORT", manticore0.getFirstMappedPort().toString());

            System.setProperty("MANTICORE1_HOST", manticore1.getContainerIpAddress());
            System.setProperty("MANTICORE1_PORT", manticore1.getFirstMappedPort().toString());

            System.setProperty("SEARCH_MANTICORE_HOST", searchManticore.getContainerIpAddress());
            System.setProperty("SEARCH_MANTICORE_PORT", searchManticore.getFirstMappedPort().toString());

            System.setProperty("CANAL_HOST", cannal.getContainerIpAddress());
            System.setProperty("CANAL_PORT", cannal.getFirstMappedPort().toString());

            System.setProperty(
                    "MYSQL_JDBC_URL",
                    String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                            System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

            System.setProperty("MANTICORE_WRITE0_JDBC_URL",
                    String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                            System.getProperty("MANTICORE0_HOST"), System.getProperty("MANTICORE0_PORT")));

            System.setProperty("MANTICORE_WRITE1_JDBC_URL",
                    String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                            System.getProperty("MANTICORE1_HOST"), System.getProperty("MANTICORE1_PORT")));

            System.setProperty("MANTICORE_SEARCH_JDBC_URL",
                    String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                            System.getProperty("SEARCH_MANTICORE_HOST"), System.getProperty("SEARCH_MANTICORE_PORT")));

            System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");

            System.out.println(System.getProperty("MANTICORE_WRITE0_JDBC_URL"));
            System.out.println(System.getProperty("MANTICORE_WRITE1_JDBC_URL"));
    }


    protected TransactionManager transactionManager;
    protected SphinxQLIndexStorage indexStorage;
    protected DataSourcePackage dataSourcePackage;

    protected SQLMasterStorage masterStorage;
    protected DataSource dataSource;
    protected TransactionExecutor masterTransactionExecutor;
    protected String tableName = "oqsbigentity";

    protected DataSource buildDataSourceSelectorMaster(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return dataSourcePackage.getMaster().get(0);
    }

    protected void initIndex() throws SQLException, InterruptedException {
        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector(
            "./src/test/resources/sql_index_storage.conf");
        DataSource searchDataSource = buildSearchDataSource(
                "./src/test/resources/sql_index_storage.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        if (transactionManager == null) {
            long commitId = 0;
            commitIdStatusService = mock(CommitIdStatusServiceImpl.class);
            when(commitIdStatusService.save(commitId)).thenReturn(commitId++);

            transactionManager = new DefaultTransactionManager(
                new IncreasingOrderLongIdGenerator(0), new IncreasingOrderLongIdGenerator(0));
        }

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

    protected void initMaster() throws Exception {

        dataSource = buildDataSourceSelectorMaster("./src/test/resources/oqsengine-ds.conf");

        if (transactionManager == null) {
            long commitId = 0;
            commitIdStatusService = mock(CommitIdStatusServiceImpl.class);
            when(commitIdStatusService.save(commitId)).thenReturn(commitId++);

            transactionManager = new DefaultTransactionManager(
                    new IncreasingOrderLongIdGenerator(0), new IncreasingOrderLongIdGenerator(0));
        }

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

    protected SphinxSyncExecutor sphinxSyncExecutor;

    protected ConsumerService initConsumerService() throws SQLException, InterruptedException {

        initIndex();

        ExecutorService consumerPool = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2048),
            ExecutorHelper.buildNameThreadFactory("consumerThreads", true),
            new ThreadPoolExecutor.AbortPolicy());

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        IEntityValueBuilder<String> entityValueBuilder = new SQLJsonIEntityValueBuilder();
        ReflectionTestUtils.setField(entityValueBuilder, "storageStrategyFactory", storageStrategyFactory);

        sphinxSyncExecutor = new SphinxSyncExecutor();
        ReflectionTestUtils.setField(sphinxSyncExecutor, "sphinxQLIndexStorage", indexStorage);
        ReflectionTestUtils.setField(sphinxSyncExecutor, "consumerPool", consumerPool);
        ReflectionTestUtils.setField(sphinxSyncExecutor, "entityValueBuilder", entityValueBuilder);

        ConsumerService consumerService = new SphinxConsumerService();

        ReflectionTestUtils.setField(consumerService, "sphinxSyncExecutor", sphinxSyncExecutor);

        return consumerService;
    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        dataSourcePackage = DataSourceFactory.build();

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

    }

    private DataSource buildSearchDataSource(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return dataSourcePackage.getIndexSearch().get(0);
    }
}
