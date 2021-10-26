package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 计算字段相关测试.
 *
 * @author dongbin
 * @version 0.1 2021/10/20 17:10
 * @since 1.8
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalculationTest extends AbstractContainerExtends {

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

        MockEntityClassDefine.initMetaManager(metaManager);
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
     * 用户(用户编号, 订单总数(count), 总消费金额(sum), 平均消费金额(avg))
     * ..|---订单 (订单号, 下单时间, 订单项总数(count), 总金额(sum), 用户编号(lookup),订单用户关联)
     * .......|---订单项 (单号(lookup), 物品名称, 金额, 订单项订单关联) <br>
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
        Assertions.assertTrue(user.id() > 0, "The identity of the user entity was expected to be set, but was not.");

//        Assertions.assertEquals(0,
//            user.entityValue().getValue("订单总数(count)").get().valueToLong());
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("总消费金额(sum)").get().getValue()
        );
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            user.entityValue().getValue("平均消费金额(avg)").get().getValue()
        );

        /*
        订单被创建,订单项总数=0, 总金额=0, 用户编号=用户.
        用户应该被修改, 订单总数=1.
         */
        IEntity order = buildOrderEntity(user);
        operationResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        Assertions.assertTrue(order.id() > 0, "The identity of the user entity was expected to be set, but was not.");
//        Assertions.assertEquals(0,
//            order.entityValue().getValue("订单项总数(count)").get().valueToLong());
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            order.entityValue().getValue("总金额(sum)").get().getValue()
        );
        Assertions.assertEquals(
            user.entityValue().getValue("用户编号").get().valueToString(),
            order.entityValue().getValue("用户编号(lookup)").get().valueToString()
        );

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
//        Assertions.assertEquals(1,
//            user.entityValue().getValue("订单总数(count)").get().valueToLong());

        IEntity orderItem = buildOrderItem(order);
        operationResult = entityManagementService.build(orderItem);

        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
        Assertions.assertTrue(orderItem.id() > 0,
            "The identity of the user entity was expected to be set, but was not.");
        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            order.entityValue().getValue("总金额(sum)").get().getValue()
        );
        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            user.entityValue().getValue("总消费金额(sum)").get().getValue()
        );
        Assertions.assertEquals(
            order.entityValue().getValue("总金额(sum)").get().getValue(),
            user.entityValue().getValue("平均消费金额(avg)").get().getValue()
        );
        Assertions.assertEquals(
            order.entityValue().getValue("订单号").get().getValue(),
            orderItem.entityValue().getValue("单号(lookup)").get().getValue()
        );
    }

    @Test
    public void testReplaceCalculation() throws Exception {

    }

    // 构造用户.
    private IEntity buildUserEntity() {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U" + idGenerator.next())
                    )
            ).build();
    }

    // 构造指定用户下的订单.
    private IEntity buildOrderEntity(IEntity user) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockEntityClassDefine.ORDER_CLASS.field("订单号").get(),
                            "O" + idGenerator.next()
                        )
                    )
                    .addValue(
                        new DateTimeValue(
                            MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                            faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        )
                    )
                    .addValue(
                        new LookupValue(
                            MockEntityClassDefine.ORDER_CLASS.field("用户编号(lookup)").get(),
                            user.id()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockEntityClassDefine.ORDER_CLASS.field("订单用户关联").get(),
                            user.id()
                        )
                    )
            ).build();
    }

    // 构造指定订单下的订单项.
    private IEntity buildOrderItem(IEntity order) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.ORDER_ITEM_CLASS.ref())
            .withEntityValue(
                EntityValue.build()
                    .addValue(
                        new StringValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("物品名称").get(),
                            faker.food().fruit()
                        )
                    )
                    .addValue(
                        new DecimalValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                            new BigDecimal(faker.number().randomDouble(3, 1, 1000))
                                .setScale(6, BigDecimal.ROUND_HALF_UP)
                        )
                    )
                    .addValue(
                        new LookupValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("单号(lookup)").get(),
                            order.id()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                            order.id()
                        )
                    )
            ).build();
    }
}
