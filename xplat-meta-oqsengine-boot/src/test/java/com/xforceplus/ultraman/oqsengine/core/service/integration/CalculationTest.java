package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.InitCalculationManager;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.LookupCalculationLogic;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityHelper;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.vavr.control.Either;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 计算字段相关测试.
 *
 * @author dongbin
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
@ActiveProfiles("integration")
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CalculationTest extends AbstractContainerExtends {

    final Logger logger = LoggerFactory.getLogger(CalculationTest.class);

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
    private SegmentStorage segmentStorage;

    @Resource
    private TransactionManager transactionManager;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    @Resource(name = "taskThreadPool")
    private ExecutorService taskThreadPool;

    @Resource
    private InitCalculationManager initCalculationManager;

    private Faker faker = new Faker(Locale.CHINA);

    private SegmentInfo segmentInfo = MockEntityClassDefine.getDefaultSegmentInfo();

    private MockEntityHelper entityHelper;

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

        initSegment(segmentInfo);

        MockEntityClassDefine.initMetaManager(metaManager);

        entityHelper = new MockEntityHelper(idGenerator);
    }

    /**
     * 每个测试的清理.
     */
    @AfterEach
    public void after() throws Exception {
        boolean clear = false;
        long[] commitIds = new long[0];
        for (int i = 0; i < 1000; i++) {
            commitIds = commitIdStatusService.getAll();
            if (commitIds.length > 0) {
                logger.info("Wait for CDC synchronization to complete.[{}]", Arrays.toString(commitIds));
                TimeUnit.MILLISECONDS.sleep(500);
            } else {
                clear = true;
                break;
            }
        }
        Assertions.assertTrue(clear,
            String.format("Failed to process unsynchronized commit numbers as expected, leaving %s.",
                Arrays.toString(commitIds)));

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

        deleteSegment(segmentInfo);
    }

    public void deleteSegment(SegmentInfo info) throws SQLException {
        segmentStorage.delete(info);
    }

    public void initSegment(SegmentInfo info) throws SQLException {
        segmentStorage.build(info);
    }

    /**
     * lookup目标对象字段值不存在.
     * 预计发起lookup的对象此值也不存在.
     */
    @Test
    public void testLookupEmptyValue() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        user.entityValue().remove(MockEntityClassDefine.USER_CLASS.field("用户编号").get());
        OqsResult oqsResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());

        IEntity order = entityHelper.buildOrderEntity(user);
        oqsResult = entityManagementService.build(order);
        // 由于公式计算触发了除0异常,所以这里是半成功.
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertFalse(order.entityValue().getValue("用户编号lookup").isPresent());
    }

    /**
     * 测试对已经存在的的实体中的lookup字段进行更新.
     * 预计应该重新lookup.
     */
    @Test
    public void testLookupReplace() throws Exception {
        IEntity user0 = entityHelper.buildUserEntity();
        IEntity user1 = entityHelper.buildUserEntity();
        entityManagementService.build(new com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] {user0, user1});

        IEntity order = entityHelper.buildOrderEntity(user0);
        entityManagementService.build(order);

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            user0.entityValue().getValue("用户编号").get().getValue(),
            order.entityValue().getValue("用户编号lookup").get().getValue()
        );

        order.entityValue()
            .addValue(
                new LookupValue(
                    MockEntityClassDefine.ORDER_CLASS.field("用户编号lookup").get(),
                    user1.id()
                )
            )
            .addValue(
                new LongValue(
                    MockEntityClassDefine.ORDER_CLASS.field("订单用户关联").get(),
                    user1.id()
                )
            );

        entityManagementService.replace(order);
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            user1.entityValue().getValue("用户编号").get().getValue(),
            order.entityValue().getValue("用户编号lookup").get().getValue()
        );
    }

    /**
     * 测试lookup目标字段被更新,需要更新的lookup实例超过事务上限.
     */
    @Test
    public void testLookupReplaceOutofTx() throws Exception {
        Field field = LookupCalculationLogic.class.getDeclaredField("TRANSACTION_LIMIT_NUMBER");
        field.setAccessible(true);
        // 事务内处理上限.
        int transactionLimitNumber = (int) field.get(null);
        int outTransactionNumber = 1000;

        IEntity user = entityHelper.buildUserEntity();
        entityManagementService.build(user);

        int orderSize = transactionLimitNumber + outTransactionNumber;
        Transaction tx = transactionManager.create(TimeUnit.SECONDS.toMillis(300));
        for (int i = 0; i < orderSize; i++) {
            transactionManager.bind(tx.id());
            entityManagementService.build(entityHelper.buildOrderEntity(user));
            logger.info("Successfully created order.[{}/{}]", i + 1, orderSize);
        }
        transactionManager.bind(tx.id());
        tx.commit();
        transactionManager.bind(tx.id());
        transactionManager.finish();


        logger.info("Query {} orders.", orderSize);
        OqsResult<Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity>> orders = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.ORDER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(orderSize)).build()
        );

        String userNumber = user.entityValue().getValue("用户编号").get().valueToString();
        logger.info("Verify that the value of the \"用户编号lookup\" field in {} orders is equal to {}.",
            orderSize, userNumber);
        for (IEntity order : orders.getValue().get()) {
            Assertions.assertEquals(Long.toString(user.id()),
                order.entityValue().getValue("用户编号lookup").get().getAttachment().get());
            Assertions.assertEquals(userNumber,
                order.entityValue().getValue("用户编号lookup").get().getValue());
        }

        String newUseNumber = "U" + idGenerator.next();
        logger.info("The user id is changed from {} to {}.", userNumber, newUseNumber);
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        user.entityValue().addValue(
            new StringValue(
                MockEntityClassDefine.USER_CLASS.field("用户编号").get(), newUseNumber)
        );

        // 在事务内更新.
        tx = transactionManager.create(Integer.MAX_VALUE);
        transactionManager.bind(tx.id());
        entityManagementService.replace(user);

        transactionManager.bind(tx.id());
        orders = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.ORDER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(3000)).build()
        );
        // 事务内
        long syncedOrderCount = orders.getValue().get().stream().filter(e ->
            Objects.equals(
                e.entityValue().getValue("用户编号lookup").get().getValue(),
                newUseNumber
            )
        ).count();
        Assertions.assertEquals(transactionLimitNumber, syncedOrderCount);
        logger.info("The \"用户编号lookup\" field for the expected 1000 orders has been changed from {} to {}.",
            userNumber, newUseNumber);

        transactionManager.bind(tx.id());
        tx = transactionManager.getCurrent().get();
        // 应该触发外部任务,将额外的1000 order中的 "用户编号lookup"字段进行更新.
        tx.commit();
        transactionManager.finish();

        orders = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.ORDER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(3000)).build());

        boolean fail = true;
        for (int i = 0; i < 10000; i++) {
            syncedOrderCount = orders.getValue().get().stream().filter(e ->
                Objects.equals(
                    e.entityValue().getValue("用户编号lookup").get().getValue(),
                    newUseNumber
                )
            ).count();

            if (syncedOrderCount == orderSize) {
                fail = false;
                break;
            } else {

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5000));

                logger.info("There are {} orders that have not been processed.", orderSize - syncedOrderCount);

                orders = entitySearchService.selectByConditions(
                    Conditions.buildEmtpyConditions(),
                    MockEntityClassDefine.ORDER_CLASS.ref(),
                    ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(3000)).build()
                );
            }
        }

        if (fail) {
            Assertions.fail("The number of transactions exceeded was not completed in the expected time.");
        }
    }

    /**
     * 测试如下场景.
     * 1 : N
     * User对象  ->  Order对象.
     * <br>
     * 并发的创建某个User实例关联的Order对象实例最终判断User实例上的计算字段的正确性.
     */
    @Test
    public void testBuildCalculationConcurrent() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        entityManagementService.build(user);

        int size = 30;
        long userId = user.id();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(size);
        Queue<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> queue = new ConcurrentLinkedQueue();
        for (int i = 0; i < size; i++) {
            taskThreadPool.submit(() -> {
                // 阻塞等待同时开始.
                try {
                    startLatch.await();
                    entityManagementService.build(entityHelper.buildOrderEntity(user));

                    OqsResult<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> currentUserOp = entitySearchService.selectOne(
                        userId, MockEntityClassDefine.USER_CLASS.ref());
                    queue.add(currentUserOp.getValue().get());

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return;
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        finishLatch.await();

        Assertions.assertEquals(size, queue.size());

        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity
            currentUser = entitySearchService.selectOne(user.id(), user.entityClassRef()).getValue().get();
        long max = currentUser.entityValue().getValue("订单总数count").get().valueToLong();
        Assertions.assertEquals(size, max);

    }

    /**
     * 测试目标结构如下.
     * <br>
     * 用户(用户编号, 订单总数count, 总消费金额sum, 平均消费金额avg, 最大消费金额max, 最小消费金额min)
     * ..|---订单 (订单号, 下单时间, 订单项总数count, 总金额sum, 用户编号lookup,订单项平均价格formula, 订单用户关联)
     * .......|---订单项 (单号lookup, 物品名称, 金额, 订单项订单关联) <br>
     * <br>
     */
    @Test
    public void testBuildCalculation() throws Exception {
        /*
        用户被创建,其订单总数,总消费金额,平均消费金额应该为0.
         */
        IEntity user = entityHelper.buildUserEntity();
        OqsResult oqsResult = entityManagementService.build(user);

        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());
        Assertions.assertTrue(user.id() > 0, "The identity of the user entity was expected to be set, but was not.");

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
        IEntity order = entityHelper.buildOrderEntity(user);
        oqsResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());
        Assertions.assertTrue(order.id() > 0, "The identity of the user entity was expected to be set, but was not.");
        Assertions.assertEquals(0,
            order.entityValue().getValue("订单项总数count").get().valueToLong());
        Assertions.assertEquals(
            new BigDecimal("0.0"),
            order.entityValue().getValue("总金额sum").get().getValue()
        );
        Assertions.assertEquals(
            user.entityValue().getValue("用户编号").get().valueToString(),
            order.entityValue().getValue("用户编号lookup").get().valueToString()
        );
        Assertions.assertEquals(
            new BigDecimal("0.000000"),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("最小数量min").get().valueToLong()
        );
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("最大数量max").get().valueToLong()
        );
        Assertions.assertEquals(
            DateTimeValue.MIN_DATE_TIME,
            order.entityValue().getValue("最小时间min").get().getValue()
        );
        Assertions.assertEquals(
            DateTimeValue.MIN_DATE_TIME,
            order.entityValue().getValue("最大时间max").get().getValue()
        );
        Assertions.assertEquals(
            0,
            order.entityValue().getValue("总数量sum").get().valueToLong()
        );
        Assertions.assertEquals(1,
            user.entityValue().getValue("订单总数count").get().valueToLong());

        IEntity orderItem = entityHelper.buildOrderItem(order);
        oqsResult = entityManagementService.build(orderItem);

        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
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
            order.entityValue().getValue("订单号").get().getValue(),
            orderItem.entityValue().getValue("单号lookup").get().getValue()
        );
        Assertions.assertEquals(
            ((BigDecimal) order.entityValue().getValue("总金额sum").get().getValue()).divide(BigDecimal.ONE),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );
        Assertions.assertNotEquals(
            DateTimeValue.MIN_DATE_TIME,
            order.entityValue().getValue("最小时间min").get().getValue()
        );
        Assertions.assertNotEquals(
            DateTimeValue.MIN_DATE_TIME,
            order.entityValue().getValue("最大时间max").get().getValue()
        );

        IEntity order1 = entityHelper.buildOrderEntity(user);
        oqsResult = entityManagementService.build(order1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());
        order = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        IEntity orderItem1 = entityHelper.buildOrderItem(order1);
        oqsResult = entityManagementService.build(orderItem1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());

        order = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();

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
        Assertions.assertNotNull(
            order1.entityValue().getValue("最小时间min").get().valueToLong()
        );
        Assertions.assertNotNull(
            order1.entityValue().getValue("最大时间max").get().valueToLong()
        );

        IEntity orderItem2 = entityHelper.buildOrderItem(order1);
        oqsResult = entityManagementService.build(orderItem2);
        order1 = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
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
        Assertions.assertNotNull(
            order1.entityValue().getValue("最小时间min").get().valueToLong()
        );
        Assertions.assertNotNull(
            order1.entityValue().getValue("最大时间max").get().valueToLong()
        );

    }

    /**
     * 测试目标结构如下.
     * <br>
     * 用户(用户编号, 订单总数count, 总消费金额sum, 平均消费金额avg)
     * ..|---订单 (订单号, 下单时间, 订单项总数count, 总金额sum, 用户编号lookup,订单项平均价格formula, 订单用户关联)
     * .......|---订单项 (单号lookup, 物品名称, 金额, 订单项订单关联) <br>
     * <br>
     * 对订单项中的金额进行更新,其中应该造成订单和用户的金额重新计算.
     */
    @Test
    public void testReplaceCalculation() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        OqsResult oqsResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());
        IEntity order = entityHelper.buildOrderEntity(user);
        oqsResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());
        IEntity orderItem = entityHelper.buildOrderItem(order);
        oqsResult = entityManagementService.build(orderItem);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();

        orderItem = Entity.Builder.anEntity()
            .withId(orderItem.id())
            .withEntityClassRef(MockEntityClassDefine.ORDER_ITEM_CLASS.ref())
            .withValue(
                new DecimalValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                    new BigDecimal("100.0")
                )
            ).build();

        oqsResult = entityManagementService.replace(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());

        //  这里设置AUTO_FILL为空，判断是否重置了autoFill字段，期望是不会改变
        order = Entity.Builder.anEntity()
            .withId(order.id())
            .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
            .withValue(
                new StringValue(
                    MockEntityClassDefine.ORDER_CLASS.field("订单号").get(),
                    ""
                )
            )
            .withValue(
                new DateTimeValue(
                    MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )
            )
            .build();

        oqsResult = entityManagementService.replace(order);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();

        Assertions.assertTrue(order.entityValue().getValue("订单号").isPresent()
            && !((String) order.entityValue().getValue("订单号").get().getValue()).isEmpty()
        );

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();

        Assertions.assertEquals(new BigDecimal("100.000000"),
            order.entityValue().getValue("总金额sum").get().getValue());
        Assertions.assertEquals(new BigDecimal("100.000000"),
            user.entityValue().getValue("总消费金额sum").get().getValue());
        Assertions.assertEquals(new BigDecimal("100.000000"),
            user.entityValue().getValue("平均消费金额avg").get().getValue());
        Assertions.assertEquals(new BigDecimal("100.000000"),
            user.entityValue().getValue("最大消费金额max").get().getValue());
        Assertions.assertEquals(new BigDecimal("100.000000"),
            user.entityValue().getValue("最小消费金额min").get().getValue());
        Assertions.assertEquals(new BigDecimal("100.000000"),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );
    }

    @Test
    public void testDeleteCalculation() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        OqsResult oqsResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());
        IEntity order = entityHelper.buildOrderEntity(user);
        oqsResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());
        IEntity orderItem = entityHelper.buildOrderItem(order);
        oqsResult = entityManagementService.build(orderItem);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, oqsResult.getResultStatus(),
            oqsResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();

        oqsResult = entityManagementService.delete(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();

        Assertions.assertEquals(new BigDecimal("0.000000"),
            order.entityValue().getValue("总金额sum").get().getValue());
        Assertions.assertEquals(new BigDecimal("0.000000"),
            user.entityValue().getValue("总消费金额sum").get().getValue());
        Assertions.assertEquals(new BigDecimal("0.000000"),
            user.entityValue().getValue("平均消费金额avg").get().getValue());
        Assertions.assertEquals(new BigDecimal("0.000000"),
            user.entityValue().getValue("最大消费金额max").get().getValue());
        Assertions.assertEquals(new BigDecimal("0.000000"),
            user.entityValue().getValue("最小消费金额min").get().getValue());
        Assertions.assertEquals(new BigDecimal("0.000000"),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );
    }

    /**
     * 测试批量创建的时候计算字段.
     * 测试目标结构如下.
     * <br>
     * 用户(用户编号, 订单总数count, 总消费金额sum, 平均消费金额avg, 最大消费金额max, 最小消费金额min)
     * ..|---订单 (订单号, 下单时间, 订单项总数count, 总金额sum, 用户编号lookup,订单项平均价格formula, 订单用户关联)
     * .......|---订单项 (单号lookup, 物品名称, 金额, 订单项订单关联) <br>
     * <br>
     */
    @Test
    public void testBathBuildCalculation() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        Assertions.assertTrue(user.isDirty());
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(user));
        Assertions.assertFalse(user.isDirty());
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();

        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] orders = new com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[10];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = entityHelper.buildOrderEntity(user);
        }
        Assertions.assertEquals(orders.length, Arrays.stream(orders).filter(o -> o.isDirty()).count());
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(orders));
        // 预期全部订单实例状态都为干净.
        Assertions.assertEquals(0, Arrays.stream(orders).filter(o -> o.isDirty()).count());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            orders.length,
            user.entityValue().getValue("订单总数count").get().valueToLong());
    }

    @Test
    public void testInitCalculation() throws Exception {
        IEntityClass orderClass = MockEntityClassDefine.ORDER_CLASS;
        IEntityClass orderItemClass = MockEntityClassDefine.ORDER_ITEM_CLASS;
        MockEntityClassDefine.changeOrder(metaManager);
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.SIMPLE_ORDER_CLASS.ref())
            .withId(1)
            .withValue(
                new DateTimeValue(
                    MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )
            )
            .build();

        int size = 200;
        Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            IEntity e = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.ORDER_ITEM_CLASS.ref())
                .withValue(
                    new StringValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("物品名称").get(),
                        faker.food().fruit()
                    )
                )
                .withValue(
                    new DecimalValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                        new BigDecimal(faker.number().randomDouble(3, 1, 1000))
                            .setScale(6, BigDecimal.ROUND_HALF_UP)
                    )
                )
                .withValue(
                    new LongValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                        faker.number().randomNumber()
                    )
                )
                .withValue(
                    new LongValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                        entity.id()
                    )
                )
                .withValue(
                    new DateTimeValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    )
                )
                .build();
            entities.add(e);

            OqsResult result = entityManagementService.build(e);
            Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        }


        CountDownLatch latch = new CountDownLatch(1);

        OqsResult build = entityManagementService.build(entity);

        MockEntityClassDefine.initMetaManager(metaManager);

        Either<String, List<IEntityField>> test = initCalculationManager.initAppCalculations("test");
        OqsResult<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entity1;
        while (true) {
            entity1 = entitySearchService.selectOne(entity.id(), entity.entityClassRef());
            if (entity1.getValue().get().entityValue().size() >= 11) {
                latch.countDown();
                break;
            }
        }
        latch.await();
        Assertions.assertEquals(200,
            entity1.getValue().get().entityValue().getValue("订单项总数count").get().valueToLong());
    }
}
