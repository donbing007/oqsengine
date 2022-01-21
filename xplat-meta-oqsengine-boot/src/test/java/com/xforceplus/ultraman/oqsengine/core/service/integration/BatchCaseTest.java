package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
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
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntitys;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 批量集成测试.
 *
 * @author dongbin
 * @version 0.1 2022/1/13 15:20
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
public class BatchCaseTest {

    final Logger logger = LoggerFactory.getLogger(BatchCaseTest.class);

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
    private ResourceLocker resourceLocker;

    @Resource
    private SegmentStorage segmentStorage;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    @Value("${locker.try.timeoutMs}")
    private long lockerTimeoutMs;

    private MockEntityHelper entityHelper;

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

        segmentStorage.build(segmentInfo);

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

        segmentStorage.delete(segmentInfo);
    }

    @Test
    public void testMaintainConflictRestore() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(user));

        // 保证进行了同步.
        while (commitIdStatusService.size() > 0) {
            logger.info("Wait sync...");

            TimeUnit.MILLISECONDS.sleep(200);
        }

        /*
        订单的创建应该失败,因为影响的user一直处于加锁中.
        注意: 这里不能使用当前线程加锁,否则会触发重入造成加锁成功.
         */
        AtomicBoolean ok = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            try {
                ok.set(resourceLocker.tryLock(30000, IEntitys.resource(user.id())));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } finally {
                latch.countDown();
            }

            // 锁定时间为最大尝试时间减少100毫秒后解锁,这里造成之后的写入延时.
            try {
                TimeUnit.MILLISECONDS.sleep(lockerTimeoutMs - 100);
            } catch (InterruptedException e) {
                // do nothing
            }

            resourceLocker.unlock(IEntitys.resource(user.id()));
        });
        latch.await(3000, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(ok.get());
        Assertions.assertTrue(resourceLocker.isLocking(IEntitys.resource(user.id())));

        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] orders = IntStream.range(0, 10)
            .mapToObj(i -> entityHelper.buildOrderEntity(user)).toArray(
                com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[]::new);
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(orders));
    }

    /**
     * 测试维护时和更新产生冲突的情况.
     * 预期最终都会成功,除非加锁等待时间超时.
     */
    @Test
    public void testMaintainConflict() throws Exception {
        IEntity user = entityHelper.buildUserEntity();
        Assertions.assertEquals(OqsResult.success(), entityManagementService.build(user));

        // 保证进行了同步.
        while (commitIdStatusService.size() > 0) {
            logger.info("Wait sync...");

            TimeUnit.MILLISECONDS.sleep(200);
        }

        /*
        订单的创建应该失败,因为影响的user一直处于加锁中.
        注意: 这里不能使用当前线程加锁,否则会触发重入造成加锁成功.
         */
        AtomicBoolean ok = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            try {
                ok.set(resourceLocker.tryLock(30000, IEntitys.resource(user.id())));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });
        latch.await(3000, TimeUnit.MILLISECONDS);

        try {
            Assertions.assertTrue(ok.get());
            Assertions.assertTrue(resourceLocker.isLocking(IEntitys.resource(user.id())));

            com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] orders = IntStream.range(0, 10)
                .mapToObj(i -> entityHelper.buildOrderEntity(user)).toArray(
                    com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[]::new);
            try {
                entityManagementService.build(orders);
                Assertions.fail("CalculationException was expected to be thrown, but was not.");
            } catch (SQLException ex) {
                // ok
            }
        } finally {
            if (ok.get()) {
                resourceLocker.unlock(IEntitys.resource(user.id()));
            }
        }
    }

    /**
     * 测试批量创建.
     * 同时创建两种类型的实例.
     */
    @Test
    public void testBatchBuild() throws Exception {
        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] targetEntities = Stream.concat(
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                    .withValues(
                        Arrays.asList(
                            new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                        )
                    ).build()
            ),
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U123")
                    )
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                            "test")
                    )
                    .build()
            )).toArray(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[]::new);
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(targetEntities).getResultStatus());
        Assertions.assertEquals(0, Arrays.stream(targetEntities).filter(e -> e.isDirty()).count());

        OqsResult<Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity>> entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(10, entitiesResult.getValue().get().size());
        entitiesResult.getValue().get().forEach(e -> {
            Assertions.assertEquals(990L, e.entityValue().getValue("l0-long").get().valueToLong());
        });

        entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.USER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(10, entitiesResult.getValue().get().size());
        entitiesResult.getValue().get().forEach(e -> {
            Assertions.assertEquals("U123", e.entityValue().getValue("用户编号").get().getValue());
            Assertions.assertEquals("test", e.entityValue().getValue("用户名称").get().getValue());
        });
    }

    /**
     * 批量更新.
     */
    @Test
    public void testReplace() throws Exception {
        /*
        创建测试目标.
        在同一批中,两个互不相关,之间没有计算字段依赖.
         */
        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] targetEntities = Stream.concat(
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                    .withValues(
                        Arrays.asList(
                            new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                        )
                    ).build()
            ),
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U123")
                    )
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                            "test")
                    )
                    .build()
            )).toArray(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[]::new);
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(targetEntities).getResultStatus());
        Assertions.assertEquals(0, Arrays.stream(targetEntities).filter(e -> e.isDirty()).count());

        // 修改测试目标字段 l0-long值为100.
        for (int i = 0; i < 5; i++) {
            targetEntities[i].entityValue().addValue(
                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 100L)
            );
            Assertions.assertTrue(targetEntities[i].isDirty());
        }
        // 修改user的用户编号,如果有order应该触发order的lookup.
        for (int i = 10; i < 20; i++) {
            targetEntities[i].entityValue().addValue(
                new StringValue(
                    MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                    "U456")
            );
            Assertions.assertTrue(targetEntities[i].isDirty());
        }

        Assertions.assertEquals(ResultStatus.SUCCESS,
            entityManagementService.replace(targetEntities).getResultStatus());

        OqsResult<Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity>> entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 100L)
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(5, entitiesResult.getValue().get().size());
        entitiesResult.getValue().get().forEach(e -> {
            Assertions.assertEquals(100L, e.entityValue().getValue("l0-long").get().valueToLong());
        });

        entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U456")
                    )
                ),
            MockEntityClassDefine.USER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(10, entitiesResult.getValue().get().size());
        entitiesResult.getValue().get().forEach(e -> {
            Assertions.assertEquals("U456", e.entityValue().getValue("用户编号").get().getValue());
            Assertions.assertEquals("test", e.entityValue().getValue("用户名称").get().getValue());
        });
    }

    @Test
    public void testDeletes() throws Exception {
        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[] targetEntities = Stream.concat(
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                    .withValues(
                        Arrays.asList(
                            new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                        )
                    ).build()
            ),
            IntStream.range(0, 10).mapToObj(i ->
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                            "U123")
                    )
                    .withValue(
                        new StringValue(
                            MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                            "test")
                    )
                    .build()
            )).toArray(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity[]::new);
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(targetEntities).getResultStatus());
        Assertions.assertEquals(0, Arrays.stream(targetEntities).filter(e -> e.isDirty()).count());

        while (commitIdStatusService.size() > 0) {
            logger.info("Wait sync...");

            TimeUnit.MILLISECONDS.sleep(200);
        }

        entityManagementService.delete(targetEntities[0]);
        Assertions.assertTrue(targetEntities[0].isDeleted());

        entityManagementService.delete(targetEntities);
        Assertions.assertEquals(0, Arrays.stream(targetEntities).filter(e -> !e.isDeleted()).count());

        Page page = Page.newSinglePage(100);
        OqsResult<Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity>> entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(page).build()
        );
        Assertions.assertEquals(0, entitiesResult.getValue().get().size());
        Assertions.assertEquals(0, page.getTotalCount());

        entitiesResult = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.USER_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(0, entitiesResult.getValue().get().size());
        Assertions.assertEquals(0, page.getTotalCount());
    }
}
