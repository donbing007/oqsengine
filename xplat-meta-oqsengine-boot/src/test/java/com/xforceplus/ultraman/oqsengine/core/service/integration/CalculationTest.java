package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorFieldUnit;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.CalculationInitInstance;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;
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

    @Resource
    private CalculationInitInstance calculationInitInstance;

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

    @Test
    public void testAggregationCollect() throws SQLException {
        IEntity collect = entityHelper.buildCollectEntity();
        OqsResult<IEntity> results = entityManagementService.build(collect);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectedA = entityHelper.buildCollectedEntity("A", collect.id());
        results = entityManagementService.build(collectedA);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectedB = entityHelper.buildCollectedEntity("B", collect.id());
        results = entityManagementService.build(collectedB);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        Conditions conditions = Conditions.buildEmtpyConditions();
        conditions.addAnd(new Condition(
            MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
            ConditionOperator.EQUALS,
            new StringsValue(
                MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
                "s-string" + "_" + "A"
            )));

        Collection<IEntity> rsls =
            entitySearchService.selectByConditions(conditions, collect.entityClassRef(),
                                                                    ServiceSelectConfig.Builder
                                                                        .anSearchConfig()
                                                                        .withPage(Page.newSinglePage(10))
                                                                        .build()
            ).getValue().get();
        Assertions.assertEquals(1, rsls.size());

        IValue<String[]> value = ((List<IEntity>) rsls).get(0).entityValue().getValue("f-collect-s").get();
        Assertions.assertEquals("s-string_A,s-string_B", value.valueToString());
        Assertions.assertEquals("1,1", value.getAttachment().get());


        // test in
        IEntity collectIn1 = entityHelper.buildCollectEntity();
        results = entityManagementService.build(collectIn1);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectIn2 = entityHelper.buildCollectEntity();
        results = entityManagementService.build(collectIn2);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectIn3 = entityHelper.buildCollectEntity();
        results = entityManagementService.build(collectIn3);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectedInA = entityHelper.buildCollectedEntity("AB", collectIn1.id());
        results = entityManagementService.build(collectedInA);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectedInB = entityHelper.buildCollectedEntity("BA", collectIn2.id());
        results = entityManagementService.build(collectedInB);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity collectedInC = entityHelper.buildCollectedEntity("AAB", collectIn3.id());
        results = entityManagementService.build(collectedInC);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        Conditions conditionsIn = Conditions.buildEmtpyConditions();
        conditionsIn.addAnd(new Condition(
            MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
            ConditionOperator.MULTIPLE_EQUALS,
            new StringsValue(
                MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
                "s-string" + "_" + "AB"
            ),
            new StringsValue(
            MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
            "s-string" + "_" + "BA"
            )
        ));

        rsls =
            entitySearchService.selectByConditions(conditionsIn, collect.entityClassRef(),
                ServiceSelectConfig.Builder
                    .anSearchConfig()
                    .withPage(Page.newSinglePage(10))
                    .build()
            ).getValue().get();
        Assertions.assertEquals(2, rsls.size());

        Assertions.assertEquals(collectIn1.id(), ((List<IEntity>) rsls).get(0).id());
        Assertions.assertEquals("s-string_AB",
                            ((List<IEntity>) rsls).get(0).entityValue().getValue("f-collect-s").get().valueToString());

        Assertions.assertEquals(collectIn2.id(), ((List<IEntity>) rsls).get(1).id());
        Assertions.assertEquals("s-string_BA",
            ((List<IEntity>) rsls).get(1).entityValue().getValue("f-collect-s").get().valueToString());

        Conditions conditionsNotIn = Conditions.buildEmtpyConditions();
        conditionsNotIn.addAnd(new Condition(
            MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
            ConditionOperator.NOT_EQUALS,
            new StringsValue(
                MockEntityClassDefine.COLLECT_MAIN_CLASS.field("f-collect-s").get(),
                "s-string" + "_" + "AB"
            )
        ));

        rsls =
            entitySearchService.selectByConditions(conditionsNotIn, collect.entityClassRef(),
                ServiceSelectConfig.Builder
                    .anSearchConfig()
                    .withPage(Page.newSinglePage(10))
                    .build()
            ).getValue().get();
        Assertions.assertEquals(3, rsls.size());

        Assertions.assertEquals(collect.id(), ((List<IEntity>) rsls).get(0).id());
        Assertions.assertEquals("s-string_A,s-string_B",
            ((List<IEntity>) rsls).get(0).entityValue().getValue("f-collect-s").get().valueToString());

        Assertions.assertEquals(collectIn2.id(), ((List<IEntity>) rsls).get(1).id());
        Assertions.assertEquals("s-string_BA",
            ((List<IEntity>) rsls).get(1).entityValue().getValue("f-collect-s").get().valueToString());

        Assertions.assertEquals(collectIn3.id(), ((List<IEntity>) rsls).get(2).id());
        Assertions.assertEquals("s-string_AAB",
            ((List<IEntity>) rsls).get(2).entityValue().getValue("f-collect-s").get().valueToString());
    }

    /**
     * 测试订单条件聚合订单项中数量大于1的订单项金额.
     * 一开始创建的订单项数量只有1,所以应该不会被聚合.
     * 测试会修改订单项的数量为2,这会满足条件应该被聚合.
     */
    @Test
    public void testConditionSum() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        OqsResult<IEntity> results = entityManagementService.build(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity order = entityHelper.buildOrderEntity(user);
        results = entityManagementService.build(order);
        Assertions.assertEquals(ResultStatus.HALF_SUCCESS, results.getResultStatus(), results.getMessage());

        // 数量只有1,应该不会被order的 "订单项数量大于1金额sum" 字段聚合.
        IEntity orderItem = entityHelper.buildOrderItem(order);
        orderItem.entityValue().addValue(
            new LongValue(
                MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                1L
            )
        );
        results = entityManagementService.build(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(new BigDecimal("0.0"),
            order.entityValue().getValue("订单项数量大于1金额sum").get().getValue());

        // 更新数量为2,应该被order "订单项数量大于1金额sum" 字段聚合.
        orderItem = entitySearchService.selectOne(
            orderItem.id(), MockEntityClassDefine.ORDER_ITEM_CLASS.ref()).getValue().get();
        orderItem.entityValue().addValue(
            new LongValue(
                MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                2L
            )
        );
        OqsResult<Map.Entry<IEntity, IValue[]>> replaceResult = entityManagementService.replace(orderItem);
        Assertions.assertEquals(ResultStatus.SUCCESS, replaceResult.getResultStatus(), replaceResult.getMessage());

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            order.entityValue().getValue("订单项数量大于1金额sum").get().getValue()
        );

        Assertions.assertEquals(
            orderItem.entityValue().getValue("金额").get().getValue(),
            order.entityValue().getValue("总金额sum").get().getValue()
        );

    }

    /**
     * 静态对象 lookup 动态对象的创建更新.
     */
    @Test
    public void testOriginalLookupDynamic() throws Exception {
        IEntity targetEntity = entityHelper.buildOdLookupTargetEntity();
        OqsResult<IEntity> results = entityManagementService.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        IEntity lookupOrigianlEntity = entityHelper.buildOdLookupEntity(targetEntity);
        results = entityManagementService.build(lookupOrigianlEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus(), results.getMessage());

        lookupOrigianlEntity = entitySearchService.selectOne(
            lookupOrigianlEntity.id(), lookupOrigianlEntity.entityClassRef()).getValue().get();

        Assertions.assertEquals(
            targetEntity.entityValue().getValue("od-lookup-target-long").get().valueToLong(),
            lookupOrigianlEntity.entityValue().getValue("od-lookup-original-long").get().valueToLong()
        );

        targetEntity.entityValue().addValue(
            new LongValue(
                MockEntityClassDefine.OD_LOOKUP_TARGET_ENTITY_CLASS.field("od-lookup-target-long").get(),
                faker.number().numberBetween(100, 10000)
            )
        );
        // 应该在事务内完成的.更新后lookup仍然保持一致.
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(targetEntity).getResultStatus());
        lookupOrigianlEntity = entitySearchService.selectOne(
            lookupOrigianlEntity.id(), lookupOrigianlEntity.entityClassRef()).getValue().get();
        Assertions.assertEquals(
            targetEntity.entityValue().getValue("od-lookup-target-long").get().valueToLong(),
            lookupOrigianlEntity.entityValue().getValue("od-lookup-original-long").get().valueToLong()
        );
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
        IEntity finalUser = user;
        IEntity[] orderEntities =
            IntStream.range(0, orderSize).mapToObj(i -> entityHelper.buildOrderEntity(finalUser))
                .toArray(IEntity[]::new);
        logger.info("Create {} order.", orderSize);
        entityManagementService.build(orderEntities);
        logger.info("Successfully created {} order.", orderSize);


        logger.info("Query {} orders.", orderSize);
        OqsResult<Collection<IEntity>> orders = entitySearchService.selectByConditions(
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
        Transaction tx = transactionManager.create(Integer.MAX_VALUE);
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

        int size = 3;
        long userId = user.id();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(size);
        Queue<IEntity> queue = new ConcurrentLinkedQueue();
        for (int i = 0; i < size; i++) {
            taskThreadPool.submit(() -> {
                // 阻塞等待同时开始.
                try {
                    startLatch.await();
                    entityManagementService.build(entityHelper.buildOrderEntity(user));

                    OqsResult<IEntity> currentUserOp = entitySearchService.selectOne(
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

        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(),
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
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(),
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
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(),
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

        // 判断lookup的用户编码 order 是否顺利更新.
        String newUserCode = "U" + idGenerator.next();
        user = Entity.Builder.anEntity()
            .withId(user.id())
            .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
            .withValue(
                new StringValue(
                    MockEntityClassDefine.USER_CLASS.field("用户编号").get(), newUserCode)
            ).build();
        oqsResult = entityManagementService.replace(user);
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(), oqsResult.getMessage());

        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(newUserCode,
            order.entityValue().getValue("用户编号lookup").get().valueToString());
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
        Assertions.assertEquals(ResultStatus.SUCCESS, oqsResult.getResultStatus(),
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
    public void testBatchReplaceCalculation() throws Exception {
        IEntity[] users = IntStream.range(0, 10).mapToObj(i -> entityHelper.buildUserEntity()).toArray(IEntity[]::new);
        entityManagementService.build(users);

        IEntity[] orders = Arrays.stream(users).map(u -> entityHelper.buildOrderEntity(u)).toArray(IEntity[]::new);
        entityManagementService.build(orders);

        String newUserCode = "U" + idGenerator.next();
        for (IEntity user : users) {
            user.entityValue().clear().addValue(
                new StringValue(
                    MockEntityClassDefine.USER_CLASS.field("用户编号").get(), newUserCode)
            );
        }
        entityManagementService.replace(users);

        Collection<IEntity> replaceOrders = entitySearchService.selectMultiple(
                Arrays.stream(orders).mapToLong(o -> o.id()).toArray(), MockEntityClassDefine.ORDER_CLASS.ref())
            .getValue().get();

        for (IEntity o : replaceOrders) {
            Assertions.assertEquals(newUserCode, o.entityValue().getValue("用户编号lookup").get().valueToString());
        }

    }

    @Test
    public void testLookupChangeTarget() throws Exception {
        IEntity u0 = entityHelper.buildUserEntity();
        IEntity u1 = entityHelper.buildUserEntity();

        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(u0));
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(u1));

        u0 = entitySearchService.selectOne(u0.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();
        u1 = entitySearchService.selectOne(u1.id(), MockEntityClassDefine.USER_CLASS.ref()).getValue().get();

        IEntity order = entityHelper.buildOrderEntity(u0);
        Assertions.assertEquals(OqsResult.halfSuccess().getResultStatus(),
            entityManagementService.build(order).getResultStatus());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        // 确认原始lookup成功.
        Assertions.assertEquals(
            order.entityValue().getValue("用户编号lookup").get().getValue(),
            u0.entityValue().getValue("用户编号").get().getValue()
        );

        // 更改成lookupu1.
        order.entityValue().addValue(
            new LookupValue(
                MockEntityClassDefine.ORDER_CLASS.field("用户编号lookup").get(),
                u1.id()
            )
        );
        Assertions.assertEquals(OqsResult.halfSuccess().getResultStatus(),
            entityManagementService.replace(order).getResultStatus());
        order = entitySearchService.selectOne(order.id(), MockEntityClassDefine.ORDER_CLASS.ref()).getValue().get();
        Assertions.assertEquals(
            order.entityValue().getValue("用户编号lookup").get().getValue(),
            u1.entityValue().getValue("用户编号").get().getValue()
        );
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
        Collection<IEntity> entities = new ArrayList<>(size);
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

        List<IEntityField> test = initCalculationManager.initAppCalculations("test");
        OqsResult<IEntity> entity1;
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

    @Test
    public void testDryRunCalculateSingle() throws Exception {
        Entity entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
                .withId(1)
                .withValue(new DateTimeValue(
                        MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )).build();

        int size = 200;
        Collection<IEntity> entities = new ArrayList<>(size);
        entities = buildDetails(size, entity);
        OqsResult<IEntity[]> detailRes = entityManagementService.build(entities.toArray(new Entity[entities.size()]));

        OqsResult<IEntity> mainRes = entityManagementService.build(entity);

        Optional<IEntityField> orderCount = MockEntityClassDefine.ORDER_CLASS.field("订单项总数count");

        Optional<IEntityField> amountSum = MockEntityClassDefine.ORDER_CLASS.field("总金额sum");

        Optional<IEntityField> amountMax = MockEntityClassDefine.ORDER_CLASS.field("最大金额max");

        Optional<IEntityField> amountMin = MockEntityClassDefine.ORDER_CLASS.field("最小金额min");

        Optional<IEntityField> quantitySum = MockEntityClassDefine.ORDER_CLASS.field("总数量sum");

        Optional<IEntityField> quantityMax = MockEntityClassDefine.ORDER_CLASS.field("最大数量max");

        Optional<IEntityField> quantityMin = MockEntityClassDefine.ORDER_CLASS.field("最小数量min");

        Optional<IEntityField> unitPrice = MockEntityClassDefine.ORDER_CLASS.field("订单项平均价格formula");

        Optional<IEntityField> sumCondition = MockEntityClassDefine.ORDER_CLASS.field("订单项数量大于1金额sum");


        Optional<ErrorCalculateInstance> errorCalculateInstance = calculationInitInstance
                .initCheckField(entity.id(),
                        MockEntityClassDefine.ORDER_CLASS,
                        Arrays.asList(orderCount.get(),
                                amountSum.get(),
                                amountMax.get(),
                                amountMin.get(),
                                quantitySum.get(),
                                quantityMax.get(),
                                quantityMin.get(),
                                unitPrice.get(),
                                sumCondition.get()));

        Assertions.assertTrue(errorCalculateInstance.isPresent());

        Assertions.assertEquals(errorCalculateInstance.get().getErrorFieldUnits().size(), 9);

        for (ErrorFieldUnit errorFieldUnit : errorCalculateInstance.get().getErrorFieldUnits()) {

            int sum = 0;
            for (int i = 0; i < size; i++) {
                sum += i;
            }

            switch (errorFieldUnit.getField().name()) {
                case "订单项总数count":
                    Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), size);

                    Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                    break;
                case "总金额sum":
                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), sum);

                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);

                    break;
                case "最大金额max":
                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), size - 1);

                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                    break;
                case "最小金额min":
                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), 0);

                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                    break;
                case "总数量sum":
                    Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), sum);

                    Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                    break;
                case "订单项数量大于1金额sum":
                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), sum - 1);

                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                    break;
                case "最大数量max":
                    Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), size - 1);

                    Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                    break;
                case "最小数量min":
                    Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), 0);

                    Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                    break;
                case "订单项平均价格formula":
                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue(), new BigDecimal("99.500000"));

                    Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                    break;
                default:
                    break;
            }
        }

    }

    @Test
    public void testDryRunCalculateMulti() throws Exception {
        Entity entity1 = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
                .withId(1)
                .withValue(new DateTimeValue(
                        MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )).build();

        Entity entity2 = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
                .withId(2)
                .withValue(new DateTimeValue(
                        MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )).build();


        int size = 200;
        Collection<IEntity> entities1 = buildDetails(size, entity1);

        Collection<IEntity> entities2 = buildDetails(size, entity2);

        Collection<IEntity> entities = new ArrayList<>();
        entities.addAll(entities1);
        entities.addAll(entities2);

        OqsResult<IEntity[]> detailRes = entityManagementService.build(entities.toArray(new Entity[entities.size()]));

        OqsResult<IEntity[]> mainRes = entityManagementService.build(new Entity[]{entity2, entity1});

        Optional<IEntityField> orderCount = MockEntityClassDefine.ORDER_CLASS.field("订单项总数count");

        Optional<IEntityField> amountSum = MockEntityClassDefine.ORDER_CLASS.field("总金额sum");

        Optional<IEntityField> amountMax = MockEntityClassDefine.ORDER_CLASS.field("最大金额max");

        Optional<IEntityField> amountMin = MockEntityClassDefine.ORDER_CLASS.field("最小金额min");

        Optional<IEntityField> quantitySum = MockEntityClassDefine.ORDER_CLASS.field("总数量sum");

        Optional<IEntityField> quantityMax = MockEntityClassDefine.ORDER_CLASS.field("最大数量max");

        Optional<IEntityField> quantityMin = MockEntityClassDefine.ORDER_CLASS.field("最小数量min");

        Optional<IEntityField> unitPrice = MockEntityClassDefine.ORDER_CLASS.field("订单项平均价格formula");
        List<ErrorCalculateInstance> errorCalculateInstances = calculationInitInstance
                .initCheckFields(Arrays.asList(entity1.id(), entity2.id()),
                        MockEntityClassDefine.ORDER_CLASS,
                        Arrays.asList(orderCount.get(),
                                amountSum.get(),
                                amountMax.get(),
                                amountMin.get(),
                                quantitySum.get(),
                                quantityMax.get(),
                                quantityMin.get(),
                                unitPrice.get()),
                        200L);


        Assertions.assertEquals(errorCalculateInstances.size(), 2);

        for (ErrorCalculateInstance errorCalculateInstance : errorCalculateInstances) {
            for (ErrorFieldUnit errorFieldUnit : errorCalculateInstance.getErrorFieldUnits()) {

                int sum = 0;
                for (int i = 0; i < size; i++) {
                    sum += i;
                }

                switch (errorFieldUnit.getField().name()) {
                    case "订单项总数count":
                        Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), size);

                        Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                        break;
                    case "总金额sum":
                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), sum);

                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);

                        break;
                    case "最大金额max":
                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), size - 1);

                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                        break;
                    case "最小金额min":
                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue().longValue(), 0);

                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                        break;
                    case "总数量sum":
                        Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), sum);

                        Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                        break;
                    case "最大数量max":
                        Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), size - 1);

                        Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                        break;
                    case "最小数量min":
                        Assertions.assertEquals(((LongValue) errorFieldUnit.getExpect()).getValue(), 0);

                        Assertions.assertEquals(((LongValue) errorFieldUnit.getNow()).getValue(), 0);
                        break;
                    case "订单项平均价格formula":
                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getExpect()).getValue(), new BigDecimal("99.500000"));

                        Assertions.assertEquals(((DecimalValue) errorFieldUnit.getNow()).getValue().longValue(), 0);
                        break;
                    default:
                        break;
                }
            }

        }

    }


    @Test
    public void testReCalculate() throws Exception {
        Entity userEntity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
                .withId(3)
                .withValue(new StringValue(
                        MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                        "test"
                )).build();


        Entity entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
                .withId(1)
                .withValue(new DateTimeValue(
                        MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                        faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )).withValue(
                        new LongValue(
                                MockEntityClassDefine.ORDER_CLASS.field("订单用户关联").get(),
                                userEntity.id()
                        )
                ).build();

        int size = 200;
        Collection<IEntity> entities = buildDetails(size, entity);
        OqsResult<IEntity[]> detailRes = entityManagementService.build(entities.toArray(new Entity[entities.size()]));

        OqsResult<IEntity> mainRes = entityManagementService.build(entity);

        OqsResult<IEntity> userRes = entityManagementService.build(userEntity);

        Optional<IEntityField> orderCount = MockEntityClassDefine.ORDER_CLASS.field("订单项总数count");

        Optional<IEntityField> amountSum = MockEntityClassDefine.ORDER_CLASS.field("总金额sum");

        Optional<IEntityField> amountMax = MockEntityClassDefine.ORDER_CLASS.field("最大金额max");

        Optional<IEntityField> amountMin = MockEntityClassDefine.ORDER_CLASS.field("最小金额min");

        Optional<IEntityField> quantitySum = MockEntityClassDefine.ORDER_CLASS.field("总数量sum");

        Optional<IEntityField> quantityMax = MockEntityClassDefine.ORDER_CLASS.field("最大数量max");

        Optional<IEntityField> quantityMin = MockEntityClassDefine.ORDER_CLASS.field("最小数量min");

        Optional<IEntityField> unitPrice = MockEntityClassDefine.ORDER_CLASS.field("订单项平均价格formula");



        OqsResult<Map<IEntity, IValue[]>> mapOqsResult = entityManagementService.reCalculate(new IEntity[]{entity},
                MockEntityClassDefine.ORDER_CLASS.ref(),
                Arrays.asList(orderCount.get().name(),
                amountSum.get().name(),
                amountMax.get().name(),
                amountMin.get().name(),
                quantitySum.get().name(),
                quantityMax.get().name(),
                quantityMin.get().name(),
                unitPrice.get().name()));


        // 重算入库后dryRun无错误字段
        Optional<ErrorCalculateInstance> errorCalculateInstance = calculationInitInstance
                .initCheckField(entity.id(),
                        MockEntityClassDefine.ORDER_CLASS,
                        Arrays.asList(orderCount.get(),
                                amountSum.get(),
                                amountMax.get(),
                                amountMin.get(),
                                quantitySum.get(),
                                quantityMax.get(),
                                quantityMin.get(),
                                unitPrice.get()));

        Assertions.assertFalse(errorCalculateInstance.isPresent());

        OqsResult<IEntity> entityOqsResult = entitySearchService.selectOne(userEntity.id(), userEntity.entityClassRef());


        Collection<IValue> values = entityOqsResult.getValue().get().entityValue().values();


        IEntityValue entityValue = entityOqsResult.getValue().get().entityValue();

        Assertions.assertEquals(entityValue.getValue("总消费金额sum").get().valueToString(), "19900.0");
        Assertions.assertEquals(entityValue.getValue("平均消费金额avg").get().valueToString(), "19900.0");
        Assertions.assertEquals(entityValue.getValue("最大消费金额max").get().valueToString(), "19900.0");
        Assertions.assertEquals(entityValue.getValue("最小消费金额min").get().valueToString(), "19900.0");

    }

    private List<IEntity> buildDetails(int size, IEntity entity) {
        List<IEntity> entities = new ArrayList<>(size);
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
                                    new BigDecimal(i)
                            )
                    )
                    .withValue(
                            new LongValue(
                                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                                    i
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
                                    new Date((Long.MAX_VALUE - i)).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            )
                    )
                    .build();
            entities.add(e);
        }
        return entities;
    }


}
