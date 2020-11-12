package com.xforceplus.ultraman.oqsengine.core.service.integration;


import com.google.common.collect.Comparators;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchTest extends AbstractMysqlTest {

    private static GenericContainer manticore;

    private boolean initialization;

    private Collection<IEntityField> mainFields;
    private Collection<IEntityField> driverFields;
    private IEntityClass mainEntityClass;
    private IEntityClass driverEntityClass;
    private List<IEntity> entities;
    private List<IEntity> driverEntities;
    private long bigDriverSelectEntityId;

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private EntityManagementService managementService;

    @Resource
    private LongIdGenerator idGenerator;

    @Resource
    private IndexStorage indexStorage;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private StatusService statusService;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private TransactionManagementService transactionManagementService;

    @AfterClass
    public static void cleanEnvironment() throws Exception {
        if (manticore != null) {
            manticore.close();
        }
    }

    @BeforeClass
    public static void prepareEnvironment() throws Exception {
        manticore = new GenericContainer<>("manticoresearch/manticore:3.4.2")
                .withExposedPorts(9306)
                .withNetworkAliases("manticore")
                .withClasspathResourceMapping("manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
        manticore.start();

        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                manticore.getContainerIpAddress(),
                manticore.getFirstMappedPort());

        System.setProperty("MANTICORE_JDBC_URL", jdbcUrl);

        String mysqlJdbc = String.format("jdbc:mysql://%s:%d/oqsengine", mysql0.getContainerIpAddress(), mysql0.getFirstMappedPort());

        System.setProperty("MYSQL_JDBC_URL", mysqlJdbc);

        System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");
    }

    private void initData(int masterSize, int indexSize) throws SQLException {

        long driverId = 1000L;

        int max = 1000;
        int min = -1000;

        double minD = 400.0;
        double maxD = 2 * min;

        LocalDateTime start = LocalDateTime.now();

        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        Random random = new Random();

        entities = IntStream.range(0, masterSize).mapToObj(
                i -> {

                    int nextInt = random.nextInt(max - min) + min;
                    Double nextD = Math.random() * maxD - minD;

                    LocalDateTime randomDate = start.plusDays(new Random().nextInt( 1000 + 1));

                    return new Entity(
                            idGenerator.next(),
                            mainEntityClass,
                            new EntityValue(0).addValues(Arrays.asList(
                                    new StringValue(mainFields.stream().findFirst().get(), generatedString),
                                    new LongValue(mainFields.stream().skip(1).findFirst().get(), nextInt),
                                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal(nextD.toString())),
                                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), randomDate)
                            )));
                }
        ).collect(Collectors.toList());


//        entities = new ArrayList(Arrays.asList(
//
//                ,
//                new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value1"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("1232.12"))
//                        )))
//                ,
//                new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value2"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-1232.12"))
//                        )))
//                , new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value3"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-1232.32"))
//                        )))
//
//        ));

        buildEntities(entities, true);


        List<IEntity> entities2 = IntStream.range(0, masterSize).mapToObj(
                i -> {

                    int nextInt = random.nextInt(max - min) + min;
                    Double nextD = Math.random() * maxD - minD;

                    LocalDateTime randomDate = start.plusDays(new Random().nextInt( 1000 + 1));

                    return new Entity(
                            idGenerator.next(),
                            mainEntityClass,
                            new EntityValue(0).addValues(Arrays.asList(
                                    new StringValue(mainFields.stream().findFirst().get(), "main.c1.value0"),
                                    new LongValue(mainFields.stream().skip(1).findFirst().get(), nextInt),
                                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal(nextD.toString())),
                                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), randomDate)
                            )));
                }
        ).collect(Collectors.toList());



//        ArrayList entities2 = new ArrayList(Arrays.asList(
//                new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value4"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-0.32"))
//                        )))
//                , new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value5"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-0.46"))
//                        )))
//                , new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value6"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("0.32"))
//                        )))
//                , new Entity(
//                        idGenerator.next(),
//                        mainEntityClass,
//                        new EntityValue(0).addValues(Arrays.asList(
//                                new StringValue(mainFields.stream().findFirst().get(), "main.c1.value7"),
//                                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId),
//                                new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("0.46"))
//                        )))
//        ));

        buildEntities(entities2, false);
        bigDriverSelectEntityId = entities.get(entities.size() - 1).id();
    }

    private void buildEntities(List<IEntity> entities, boolean insertIndex) throws SQLException {
        long txId = transactionManagementService.begin();

            for (IEntity e : entities) {
                transactionManagementService.restore(txId);
                managementService.build(e);
            }


        transactionManagementService.restore(txId);
        transactionManagementService.commit();


        if(insertIndex) {

            long txId2 = transactionManagementService.begin();
            Long commitId = statusService.getCurrentCommitLowBound(50_000L);

            for (IEntity e : entities) {
                transactionManagementService.restore(txId2);
                StorageEntity storageEntity = new StorageEntity();
                storageEntity.setId(e.id());
                storageEntity.setEntity(e.entityClass().id());
                storageEntity.setTx(txId);
                storageEntity.setCommitId(commitId);
                indexStorage.buildOrReplace(storageEntity, e.entityValue(), false);
            }

            transactionManagementService.restore(txId2);
            transactionManagementService.commit();
        }
    }

    @Before
    public void before() throws Exception {

        initialization = false;
        mainFields = Arrays.asList(
                new EntityField(idGenerator.next(), "c1", FieldType.STRING, FieldConfig.build().searchable(true)),
                new EntityField(idGenerator.next(), "c2", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(idGenerator.next(), "c3", FieldType.DECIMAL, FieldConfig.build().searchable(true)),
                new EntityField(idGenerator.next(), "c4", FieldType.DATETIME, FieldConfig.build().searchable(true))
        );

        mainEntityClass = new EntityClass(idGenerator.next(), "main", null, null, null, mainFields);

        initData(100, 100);

        initialization = true;
    }

    @After
    public void after() throws Exception {
        if (initialization) {
            clear();
        }

        initialization = false;
    }

    private void clear() throws SQLException {
        Collection<IEntity> iEntities = new ArrayList<>();
        iEntities.addAll(entities != null ? entities : Collections.emptyList());
        iEntities.addAll(driverEntities != null ? driverEntities : Collections.emptyList());

        long txId = transactionManagementService.begin();

        for (IEntity e : iEntities) {
            transactionManagementService.restore(txId);
            managementService.delete(e);
            indexStorage.delete(e);
        }
        transactionManagementService.restore(txId);
        transactionManagementService.commit();

        DataSource ds = indexWriteDataSourceSelector.select("any");
        Connection conn = ds.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select count(*) as count from oqsindex");
        rs.next();
        long size = rs.getLong(1);
        try {
            Assert.assertEquals(0, size);
        } finally {
            rs.close();
            statement.close();
            conn.close();
        }
    }

    @Test
    public void basicSearch() throws SQLException {

        Long currentCommitLowBound = statusService.getCurrentCommitLowBound(50_000L) + 1;

        Page page = new Page(0, 100);
        Sort sort = Sort.buildAscSort(mainEntityClass.fields().get(2));

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), mainEntityClass, sort, page, currentCommitLowBound);

        List<BigDecimal> bigDecimals = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DecimalValue).findFirst().get();
            return ((DecimalValue) iValue).getValue();
        }).collect(Collectors.toList());

        Comparator<BigDecimal> bigDecimalComparator = (o1, o2) -> o1.compareTo(o2);

        assertTrue(Comparators.isInOrder(bigDecimals, bigDecimalComparator));

        assertTrue(page.getTotalCount() == 200);

        Page page2 = new Page(2, 100);
        Sort sort2 = Sort.buildDescSort(mainEntityClass.fields().get(1));

        Collection<IEntity> iEntities2 = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), mainEntityClass, sort2, page2, currentCommitLowBound);

        List<String> stringList = iEntities2.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof StringValue).findFirst().get();
            return ((StringValue) iValue).getValue();
        }).collect(Collectors.toList());

        Comparator<String> comparator = String::compareTo;
        assertTrue(Comparators.isInOrder(stringList, comparator.reversed()));
    }
}
