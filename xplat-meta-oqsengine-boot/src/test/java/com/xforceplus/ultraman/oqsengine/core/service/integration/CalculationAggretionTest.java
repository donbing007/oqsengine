package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockSimpleEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 聚合字段相关测试.
 *
 * @author 王政
 * @version 0.1 2021/10/20 17:10
 * @since 1.8
 */
@ExtendWith({
    RedisContainer.class,
    MysqlContainer.class,
    ManticoreContainer.class,
    CanalContainer.class,
    SpringExtension.class
})
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CalculationAggretionTest extends AbstractContainerExtends {

    final Logger logger = LoggerFactory.getLogger(UserCaseTest.class);

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private EntityManagementService entityManagementService;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private TransactionManager transactionManager;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    private Faker faker = new Faker(Locale.CHINA);

    /**
     * 每个测试的初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:oqsengine-ds.conf");

        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }


        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex");
                }
            }
        }

        MockSimpleEntityClassDefine.initMetaManager(metaManager);
    }

    /**
     * 每个测试的清理.
     */
    @AfterEach
    public void after() throws Exception {
        while (commitIdStatusService.size() > 0) {
            logger.info("Wait for CDC synchronization to complete.");
            TimeUnit.MILLISECONDS.sleep(10);
        }

        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }

        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex");
                }
            }
        }

    }

    /**
     * 测试目标结构如下.
     * <br>
     * 用户(用户编号, 订单总数count, 总消费金额sum, 平均消费金额avg, 最大消费金额max, 最小消费金额min)
     * ..|---订单 (订单号, 下单时间, 订单项总数count, 总金额sum, 总数量, 最大数量, 最小数量, 平均数量, 订单项平均价格formula, 订单用户关联)
     * .......|---订单项 (单号lookup, 物品名称, 金额, 订单项订单关联) <br>
     * <br>
     */
    @Test
    public void testBuildCalculation() throws Exception {
        /*
        用户被创建,其订单总数,总消费金额,平均消费金额应该为0.
         */
        IEntity user = buildUserEntity();
        OperationResult operationResult = entityManagementService.build(user);

        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        Assertions.assertTrue(user.id() > 0,
            "The identity of the user entity was expected to be set, but was not.");

        Assertions.assertEquals(0,
            user.entityValue().getValue("订单总数count").get().valueToLong());
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("总消费金额sum").get().getValue()
        );
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("平均消费金额avg").get().getValue()
        );
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("最大消费金额max").get().getValue()
        );
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("最小消费金额min").get().getValue()
        );

        /*
        订单被创建,订单项总数=0, 总金额=0, 用户编号=用户.
        用户应该被修改, 订单总数=1.
         */
        IEntity order = buildOrderEntity(user);
        operationResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        Assertions.assertTrue(order.id() > 0, "The identity of the user entity was expected to be set, but was not.");
        Assertions.assertEquals(0,
            order.entityValue().getValue("订单项总数count").get().valueToLong());
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            order.entityValue().getValue("总金额sum").get().getValue()
        );
        Assertions.assertEquals(
            new BigDecimal("0.000000"),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );

        user = entitySearchService.selectOne(user.id(), MockSimpleEntityClassDefine.USER_CLASS.ref()).get();
        order = entitySearchService.selectOne(order.id(), MockSimpleEntityClassDefine.ORDER_CLASS.ref()).get();
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("最小数量min").get().valueToLong()
        );
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("最大数量max").get().valueToLong()
        );
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("总数量sum").get().valueToLong()
        );

        Assertions.assertEquals(1,
            user.entityValue().getValue("订单总数count").get().valueToLong());

        IEntity orderItem = buildOrderItem(order);
        operationResult = entityManagementService.build(orderItem);

        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockSimpleEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockSimpleEntityClassDefine.USER_CLASS.ref()).get();
        Assertions.assertTrue(orderItem.id() > 0,
            "The identity of the user entity was expected to be set, but was not.");
        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            order.entityValue().getValue("总金额sum").get().getValue()
        );
        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            user.entityValue().getValue("总消费金额sum").get().getValue()
        );
        Assertions.assertEquals(
            order.entityValue().getValue("总金额sum").get().getValue(),
            user.entityValue().getValue("平均消费金额avg").get().getValue()
        );
        Assertions.assertEquals(
            ((BigDecimal) order.entityValue().getValue("总金额sum").get().getValue()).divide(BigDecimal.ONE),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );
        Assertions.assertNotEquals(
            0,
            order.entityValue().getValue("最小数量min").get().valueToLong()
        );
        Assertions.assertNotEquals(
            0,
            order.entityValue().getValue("最大数量max").get().valueToLong()
        );
        Assertions.assertNotEquals(
            0,
            order.entityValue().getValue("总数量sum").get().valueToLong()
        );

        IEntity order1 = buildOrderEntity(user);
        operationResult = entityManagementService.build(order1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        user = entitySearchService.selectOne(user.id(), MockSimpleEntityClassDefine.USER_CLASS.ref()).get();
        IEntity orderItem1 = buildOrderItem(order1);
        operationResult = entityManagementService.build(orderItem1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        IEntity orderItem2 = buildOrderItem(order1);
        operationResult = entityManagementService.build(orderItem2);
        order1 = entitySearchService.selectOne(order1.id(), MockSimpleEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockSimpleEntityClassDefine.USER_CLASS.ref()).get();
        Assertions.assertNotEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("平均消费金额avg").get().getValue()
        );
        Assertions.assertNotEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("最大消费金额max").get().getValue()
        );
        Assertions.assertNotEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("最小消费金额min").get().getValue()
        );
        Assertions.assertNotEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("总消费金额sum").get().getValue()
        );

    }


    // 构造用户.
    private IEntity buildUserEntity() {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockSimpleEntityClassDefine.USER_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockSimpleEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U" + idGenerator.next())
                    )
            ).build();
    }

    // 构造指定用户下的订单.
    private IEntity buildOrderEntity(IEntity user) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockSimpleEntityClassDefine.ORDER_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockSimpleEntityClassDefine.ORDER_CLASS.field("订单号").get(),
                            "O" + idGenerator.next()
                        )
                    )
                    .addValue(
                        new DateTimeValue(
                            MockSimpleEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                            faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockSimpleEntityClassDefine.ORDER_CLASS.field("订单用户关联").get(),
                            user.id()
                        )
                    )
            ).build();
    }

    // 构造指定订单下的订单项.
    private IEntity buildOrderItem(IEntity order) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockSimpleEntityClassDefine.ORDER_ITEM_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockSimpleEntityClassDefine.ORDER_ITEM_CLASS.field("物品名称").get(),
                            faker.food().fruit()
                        )
                    )
                    .addValue(
                        new DecimalValue(
                            MockSimpleEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                            new BigDecimal(faker.number().randomDouble(3, 1, 1000))
                                .setScale(6, BigDecimal.ROUND_HALF_UP)
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockSimpleEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                            faker.number().randomNumber()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockSimpleEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                            order.id()
                        )
                    )
            ).build();
    }
}