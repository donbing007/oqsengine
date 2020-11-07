package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Ignore;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
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
public abstract class AbstractContainer {
    protected static GenericContainer manticore0;
    protected static GenericContainer manticore1;
    protected static GenericContainer searchManticore;

    protected static DockerComposeContainer environment;

    static {
        Network network = Network.newNetwork();
        initDockerCompose();
        initManticore(network);
    }

    private static void initDockerCompose() {
        environment =
                new DockerComposeContainer(new File("src/test/resources/compose-all.yaml"))
                        .withExposedService("mysql_1", 3306)
                        .withExposedService("canal-server_1", 11111);

        environment.start();

        String mysqlUrl = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                environment.getServiceHost("mysql_1", 3306), environment.getServicePort("mysql_1", 3306));

        System.setProperty("MYSQL_JDBC_URL", mysqlUrl);
    }


    private static void initManticore(Network network) {

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



    protected TransactionManager transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0));
    protected SphinxQLIndexStorage indexStorage;
    protected DataSourcePackage dataSourcePackage;

    protected SQLMasterStorage masterStorage;


    protected Selector<DataSource> buildDataSourceSelectorMaster(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getMaster());
    }

    protected Selector<DataSource> buildDataSourceSelectorIndex(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());
    }

    protected Selector<String> buildTableNameSelector(String base, int size) {

        return new SuffixNumberHashSelector(base, size);
    }


    protected void initIndex() throws SQLException, InterruptedException {
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

        indexStorage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(indexStorage, "writerDataSourceSelector", writeDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "searchDataSourceSelector", searchDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(indexStorage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(indexStorage, "storageStrategyFactory", storageStrategyFactory);
        indexStorage.setIndexTableName("oqsindex");
        indexStorage.setMaxQueryTimeMs(1000);
        indexStorage.init();
    }

    protected void initMaster() throws Exception {

        Selector<String> tableNameSelector = buildTableNameSelector("oqsbigentity", 3);

        Selector<DataSource> dataSourceSelector = buildDataSourceSelectorMaster("./src/test/resources/oqsengine-ds.conf");

        TransactionExecutor executor = new AutoJoinTransactionExecutor(
                transactionManager, ConnectionTransactionResource.class);


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(500),
                ExecutorHelper.buildNameThreadFactory("oqs-engine", false),
                new ThreadPoolExecutor.AbortPolicy()
        );

        masterStorage = new SQLMasterStorage();
        ReflectionTestUtils.setField(masterStorage, "dataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(masterStorage, "tableNameSelector", tableNameSelector);
        ReflectionTestUtils.setField(masterStorage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(masterStorage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(masterStorage, "threadPool", threadPool);
        masterStorage.init();
    }

    protected ConsumerService initConsumerService() throws SQLException, InterruptedException {
        initIndex();

        ExecutorService consumerPool = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2048),
                ExecutorHelper.buildNameThreadFactory("consumerThreads", true),
                new ThreadPoolExecutor.AbortPolicy());

        IEntityValueBuilder<String> entityValueBuilder = new SQLJsonIEntityValueBuilder();

        ConsumerService consumerService = new SphinxConsumerService();
        ReflectionTestUtils.setField(consumerService, "sphinxQLIndexStorage", indexStorage);
        ReflectionTestUtils.setField(consumerService, "consumerPool", consumerPool);
        ReflectionTestUtils.setField(consumerService, "entityValueBuilder", entityValueBuilder);

        return consumerService;
    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        dataSourcePackage = DataSourceFactory.build();

        return new HashSelector<>(dataSourcePackage.getIndexWriter());

    }

    private Selector<DataSource> buildSearchDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getIndexSearch());
    }
}
