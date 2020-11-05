package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 关联查询集成测试.
 *
 * @author dongbin
 * @version 0.1 2020/4/28 16:27
 * @since 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JoinSelectTest {

    private static GenericContainer manticore;

    final Logger logger = LoggerFactory.getLogger(JoinSelectTest.class);

    @Resource
    private LongIdGenerator idGenerator;

    @Resource
    private EntityManagementService managementService;
    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private TransactionManagementService transactionManagementService;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    private boolean initialization;


    private Collection<IEntityField> mainFields;
    private Collection<IEntityField> driverFields;
    private IEntityClass mainEntityClass;
    private IEntityClass driverEntityClass;
    private List<IEntity> entities;
    private List<IEntity> driverEntities;
    private long bigDriverSelectEntityId;

    @Before
    public void before() throws Exception {

        initialization = false;

        mainFields = Arrays.asList(
            new EntityField(idGenerator.next(), "c1", FieldType.STRING, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "rel0.id", FieldType.LONG, FieldConfig.build().searchable(true))
        );

        driverFields = Arrays.asList(
            new EntityField(idGenerator.next(), "rel0.name", FieldType.STRING, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG, FieldConfig.build().searchable(true))
        );

        mainEntityClass = new EntityClass(idGenerator.next(), "main", null, null, null, mainFields);
        driverEntityClass = new EntityClass(idGenerator.next(), "driver", null, null, null, driverFields);

        initData();

        initialization = true;
    }

    @After
    public void after() throws Exception {
        if (initialization) {
            clear();
        }

        initialization = false;
    }

    @Test
    public void testJoinSearch() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    driverEntityClass,
                    driverFields.stream().findFirst().get(),
                    ConditionOperator.EQUALS,
                    new StringValue(driverFields.stream().findFirst().get(), "name0"))
            );

        Collection<IEntity> results =
            entitySearchService.selectByConditions(conditions, mainEntityClass, Page.newSinglePage(100));
        Assert.assertEquals(2, results.size());
        long[] expectedIds = new long[]{
            entities.stream().findFirst().get().id(),
            entities.stream().skip(1).findFirst().get().id()
        };
        Arrays.sort(expectedIds);
        for (long expectedId : expectedIds) {
            long count = results.stream().filter(e -> e.id() == expectedId).count();
            Assert.assertEquals(1, count);
        }

        // 大型 driver
        conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    driverEntityClass,
                    driverFields.stream().skip(1).findFirst().get(),
                    ConditionOperator.EQUALS,
                    new LongValue(driverFields.stream().skip(1).findFirst().get(), Long.MAX_VALUE)
                )
            );
        results = entitySearchService.selectByConditions(conditions, mainEntityClass, Page.newSinglePage(100));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(bigDriverSelectEntityId, results.stream().findFirst().get().id());

    }

    private void initData() throws SQLException {
        // driver entity.
        driverEntities = new ArrayList<>(Arrays.asList(
            new Entity(
                idGenerator.next(),
                driverEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(driverFields.stream().findFirst().get(), "name0"),
                    new LongValue(driverFields.stream().skip(1).findFirst().get(), 0)
                )))
            ,
            new Entity(
                idGenerator.next(),
                driverEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(driverFields.stream().findFirst().get(), "name1"),
                    new LongValue(driverFields.stream().skip(1).findFirst().get(), 1)
                )))
        ));

        for (int i = 0; i < 1000; i++) {
            driverEntities.add(
                new Entity(
                    idGenerator.next(),
                    driverEntityClass,
                    new EntityValue(0).addValues(Arrays.asList(
                        new StringValue(driverFields.stream().findFirst().get(), "name2"),
                        new LongValue(driverFields.stream().skip(1).findFirst().get(), Long.MAX_VALUE)
                    ))
                )
            );
        }

        buildEntities(driverEntities);

        // main entity.
        long driverId = driverEntities.get(0).id();
        entities = new ArrayList(Arrays.asList(
            new Entity(
                idGenerator.next(),
                mainEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "main.c1.value0"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId)
                )))
            ,
            new Entity(
                idGenerator.next(),
                mainEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "main.c1.value1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), driverId)
                )))
        ));

        // big driver main entity.
        entities.add(new Entity(
            0,
            mainEntityClass,
            new EntityValue(0).addValues(Arrays.asList(
                new StringValue(mainFields.stream().findFirst().get(), "main.c1.bigvalue"),
                new LongValue(mainFields.stream().skip(1).findFirst().get(), driverEntities.get(driverEntities.size() - 1).id())
            ))
        ));

        buildEntities(entities);

        bigDriverSelectEntityId = entities.get(entities.size() - 1).id();
    }

    private void clear() throws SQLException {
        Collection<IEntity> iEntities = new ArrayList<>();
        iEntities.addAll(entities != null ? entities : Collections.emptyList());
        iEntities.addAll(driverEntities != null ? driverEntities : Collections.emptyList());

        long txId = transactionManagementService.begin();

        for (IEntity e : iEntities) {
            transactionManagementService.restore(txId);
            managementService.delete(e);
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

    private void buildEntities(List<IEntity> entities) throws SQLException {
        long txId = transactionManagementService.begin();
        for (IEntity e : entities) {
            transactionManagementService.restore(txId);
            managementService.build(e);
        }
        transactionManagementService.restore(txId);
        transactionManagementService.commit();
    }

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

        System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");
    }

}
