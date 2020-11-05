import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
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
import org.testcontainers.shaded.org.apache.commons.lang.time.StopWatch;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.EMPTY_BATCH_ID;

/**
 * desc :
 * name : CdcSyncBenchmarkTest
 *
 * @author : xujia
 * date : 2020/11/2
 * @since : 1.8
 */
public class CdcSyncBenchmarkTest {

    private static GenericContainer manticore0;
    private static GenericContainer manticore1;
    private static GenericContainer searchManticore;

    int batchSize = 4096;

    protected TransactionManager transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0));
    protected SphinxQLIndexStorage indexStorage;
    protected DataSourcePackage dataSourcePackage;

    private SQLMasterStorage masterStorage;

    private IEntityField fixStringsField = new EntityField(100000, "strings", FieldType.STRINGS);
    private StringsValue fixStringsValue = new StringsValue(fixStringsField, "1,2,3,500002,测试".split(","));

    private Selector<DataSource> buildDataSourceSelectorMaster(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getMaster());
    }

    private Selector<DataSource> buildDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            dataSourcePackage = DataSourceFactory.build();
        }

        return new HashSelector<>(dataSourcePackage.getIndexWriter());
    }

    private Selector<String> buildTableNameSelector(String base, int size) {
        return new SuffixNumberHashSelector(base, size);
    }

    @Before
    public void before() throws Exception {

        initMaster();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        initIndex();
    }

    private void initIndex() throws SQLException, InterruptedException {
        Selector<DataSource> writeDataSourceSelector = buildWriteDataSourceSelector(
                "./src/test/resources/sql_index_storage.conf");
        Selector<DataSource> searchDataSourceSelector = buildSearchDataSourceSelector(
                "./src/test/resources/sql_index_storage.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(transactionManager,
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

    private void initMaster() throws Exception {

        Selector<String> tableNameSelector = buildTableNameSelector("oqsbigentity", 3);

        Selector<DataSource> dataSourceSelector = buildDataSourceSelectorMaster("./src/test/resources/oqsengine-ds.conf");

        TransactionExecutor executor = new AutoShardTransactionExecutor(
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

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            initData(masterStorage, batchSize);
        } catch (Exception e) {
            //将事务正常提交,并从事务管理器中销毁事务.
            Transaction tx1 = transactionManager.getCurrent().get();
            tx1.rollback();
            transactionManager.finish(tx1);
        }
    }

    @Test
    public void syncReplaceTest() {
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("172.25.8.243",
                11111), "nly-v1", "canal", "canal");

        int emptyCount = 0;
        int count = 0;
        boolean isAck = false;
        try {
            canalConnector.connect();
            //监听的表，    格式为：数据库.表名,数据库.表名
            canalConnector.subscribe(".*\\..*");
            canalConnector.rollback();
            int totalEmptyCount = 120;
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while (emptyCount < totalEmptyCount) {
                if (isAck) {
                    break;
                }
                Message message = canalConnector.getWithoutAck(batchSize);//获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == EMPTY_BATCH_ID || size == 0) {
                    emptyCount++;
                    System.out.println("empty count:" + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    count += printEntry(message.getEntries());
                    if (count >= batchSize) {
                        isAck = true;
                    }
                }
                canalConnector.ack(batchId);
            }
            stopWatch.stop();

            System.out.println("use time : " + stopWatch.getTime());
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            canalConnector.disconnect();
        }
    }

    private int printEntry(List<CanalEntry.Entry> entrys) throws SQLException {
        StopWatch stopWatch = new StopWatch();
        int count = 0;
        stopWatch.start();
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            CanalEntry.RowChange rowChange = null;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            } catch (Exception e) {
            }

            CanalEntry.EventType eventType = rowChange.getEventType();


            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                count++;
                if (eventType == CanalEntry.EventType.DELETE) {
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    replace(rowData.getAfterColumnsList());
                } else {
                    replace(rowData.getAfterColumnsList());
                }
            }
        }
        stopWatch.stop();
        System.out.println("stop watch printEntry " + stopWatch.getTime());
        return count;
    }


    private void replace(List<CanalEntry.Column> columns) throws SQLException {

        IEntityValue entityValue = new EntityValue(Long.parseLong(columns.get(1).getValue()));
        IEntityClass entityClass = new EntityClass(Long.parseLong(columns.get(1).getValue()));
        IEntity entity = new Entity(Long.parseLong(columns.get(0).getValue()),
                entityClass,
                entityValue);

        indexStorage.build(entity);
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "   update=" + column.getUpdated());
        }
    }

    @After
    public void after() throws Exception {
        transactionManager.finish();

        dataSourcePackage.close();

    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);

//        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1,
//                0L, TimeUnit.MILLISECONDS,
//                new ArrayBlockingQueue<>(2000),
//                ExecutorHelper.buildNameThreadFactory("oqs-engine", false),
//                new ThreadPoolExecutor.AbortPolicy()
//        );

        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        expectedEntitys.stream().forEach(e -> {
            try {
                storage.build(e);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

//        CountDownLatch countDownLatch = new CountDownLatch(expectedEntitys.size());
//        List<Future> futures = new ArrayList<>(expectedEntitys.size());
//        expectedEntitys.stream().forEach(e -> {
//            futures.add(threadPool.submit(() -> {
//                try {
//                    storage.build(e);
//                } catch (SQLException ex) {
//                    throw new RuntimeException(ex.getMessage(), ex);
//                }
//            }));
//        });
//
//        try {
//            if (!countDownLatch.await(60, TimeUnit.MILLISECONDS)) {
//                for (Future f : futures) {
//                    f.cancel(true);
//                }
//                throw new SQLException("init data failed", "init data failed");
//            }
//        } catch (InterruptedException e) {
//            throw new SQLException(e.getMessage(), e);
//        }


        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        Collection<IEntityField> fields = buildRandomFields(baseId, 3);
        fields.add(fixStringsField);

        return new Entity(
                baseId,
                new EntityClass(baseId, "test", fields),
                buildRandomValue(baseId, fields)
        );
    }

    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new EntityField(fieldId, "c" + fieldId,
                    ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                case STRINGS:
                    return fixStringsValue;
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
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
