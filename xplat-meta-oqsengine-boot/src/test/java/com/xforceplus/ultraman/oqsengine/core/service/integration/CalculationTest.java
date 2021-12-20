package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.LookupCalculationLogic;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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

    private Faker faker = new Faker(Locale.CHINA);

    private SegmentInfo segmentInfo = MockEntityClassDefine.getDefaultSegmentInfo();

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
        IEntity user = buildUserEntity();
        user.entityValue().remove(MockEntityClassDefine.USER_CLASS.field("用户编号").get());
        OperationResult operationResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());

        IEntity order = buildOrderEntity(user);
        operationResult = entityManagementService.build(order);
        // 由于公式计算触发了除0异常,所以这里是半成功.
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        Assertions.assertFalse(order.entityValue().getValue("用户编号lookup").isPresent());
    }

    /**
     * 测试对已经存在的的实体中的lookup字段进行更新.
     * 预计应该重新lookup.
     */
    @Test
    public void testLookupReplace() throws Exception {
        IEntity user0 = buildUserEntity();
        IEntity user1 = buildUserEntity();
        entityManagementService.build(new IEntity[] {user0, user1});

        IEntity order = buildOrderEntity(user0);
        entityManagementService.build(order);

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
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
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
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

        IEntity user = buildUserEntity();
        entityManagementService.build(user);

        int orderSize = transactionLimitNumber + outTransactionNumber;
        Transaction tx = transactionManager.create(TimeUnit.SECONDS.toMillis(300));
        for (int i = 0; i < orderSize; i++) {
            transactionManager.bind(tx.id());
            entityManagementService.build(buildOrderEntity(user));
            logger.info("Successfully created order.[{}/{}]", i + 1, orderSize);
        }
        transactionManager.bind(tx.id());
        tx.commit();
        transactionManager.bind(tx.id());
        transactionManager.finish();


        logger.info("Query {} orders.", orderSize);
        Collection<IEntity> orders = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.ORDER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(orderSize)).build()
        );

        String userNumber = user.entityValue().getValue("用户编号").get().valueToString();
        logger.info("Verify that the value of the \"用户编号lookup\" field in {} orders is equal to {}.",
            orderSize, userNumber);
        for (IEntity order : orders) {
            Assertions.assertEquals(Long.toString(user.id()),
                order.entityValue().getValue("用户编号lookup").get().getAttachment().get());
            Assertions.assertEquals(userNumber,
                order.entityValue().getValue("用户编号lookup").get().getValue());
        }

        String newUseNumber = "U" + idGenerator.next();
        logger.info("The user id is changed from {} to {}.", userNumber, newUseNumber);
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
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
        long syncedOrderCount = orders.stream().filter(e ->
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
            syncedOrderCount = orders.stream().filter(e ->
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
        IEntity user = buildUserEntity();
        entityManagementService.build(user);

        int size = 30;
        long userId = user.id();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(size);
        Queue<IEntity> queue = new ConcurrentLinkedQueue();
        for (int i = 0; i < size; i++) {
            taskThreadPool.submit(() -> {
                // 阻塞等待同时开始.
                try {
                    startLatch.await();
                    entityManagementService.build(buildOrderEntity(user));

                    Optional<IEntity> currentUserOp = entitySearchService.selectOne(
                        userId, MockEntityClassDefine.USER_CLASS.ref());
                    queue.add(currentUserOp.get());

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

        IEntity currentUser = entitySearchService.selectOne(user.id(), user.entityClassRef()).get();
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
        IEntity user = buildUserEntity();
        OperationResult operationResult = entityManagementService.build(user);

        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
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
            user.entityValue().getValue("用户编号").get().valueToString(),
            order.entityValue().getValue("用户编号lookup").get().valueToString()
        );
        Assertions.assertEquals(
            new BigDecimal("0.000000"),
            order.entityValue().getValue("订单项平均价格formula").get().getValue()
        );

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
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

        IEntity orderItem = buildOrderItem(order);
        operationResult = entityManagementService.build(orderItem);

        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
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

        IEntity order1 = buildOrderEntity(user);
        operationResult = entityManagementService.build(order1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        order = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
        IEntity orderItem1 = buildOrderItem(order1);
        operationResult = entityManagementService.build(orderItem1);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());

        order = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();

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

        IEntity orderItem2 = buildOrderItem(order1);
        operationResult = entityManagementService.build(orderItem2);
        order1 = entitySearchService.selectOne(order1.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();
        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
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
        IEntity user = buildUserEntity();
        OperationResult operationResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        IEntity order = buildOrderEntity(user);
        operationResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        IEntity orderItem = buildOrderItem(order);
        operationResult = entityManagementService.build(orderItem);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();

        orderItem = Entity.Builder.anEntity()
            .withId(orderItem.id())
            .withEntityClassRef(MockEntityClassDefine.ORDER_ITEM_CLASS.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new DecimalValue(
                        MockEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                        new BigDecimal("100.0")
                    )
                )
            ).build();

        operationResult = entityManagementService.replace(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());

        //  这里设置AUTO_FILL为空，判断是否重置了autoFill字段，期望是不会改变
        order = Entity.Builder.anEntity()
            .withId(order.id())
            .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new StringValue(
                        MockEntityClassDefine.ORDER_CLASS.field("订单号").get(),
                        ""
                    )
                )
            ).withEntityValue(
                // 此值是为了防止空对象更新检查被触发.
                EntityValue.build().addValue(
                    new DateTimeValue(
                        MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    )
                )
            ).build();

        operationResult = entityManagementService.replace(order);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();

        Assertions.assertTrue(order.entityValue().getValue("订单号").isPresent()
            && !((String) order.entityValue().getValue("订单号").get().getValue()).isEmpty()
        );

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();

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
        IEntity user = buildUserEntity();
        OperationResult operationResult = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());
        IEntity order = buildOrderEntity(user);
        operationResult = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());
        IEntity orderItem = buildOrderItem(order);
        operationResult = entityManagementService.build(orderItem);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, operationResult.getResultStatus(),
            operationResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();

        operationResult = entityManagementService.delete(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus(), operationResult.getMessage());

        user = entitySearchService.selectOne(user.id(), MockEntityClassDefine.USER_CLASS.ref()).get();
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).get();

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
                    ).addValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                            faker.name().name())
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
                            MockEntityClassDefine.ORDER_CLASS.field("用户编号lookup").get(),
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
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("单号lookup").get(),
                            order.id()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                            order.id()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                            faker.number().randomNumber()
                        )
                    )
                    .addValue(
                        new LongValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                            order.id()
                        )
                    )
                    .addValue(
                        new DateTimeValue(
                            MockEntityClassDefine.ORDER_ITEM_CLASS.field("时间").get(),
                            faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        )
                    )
            ).build();
    }
}
