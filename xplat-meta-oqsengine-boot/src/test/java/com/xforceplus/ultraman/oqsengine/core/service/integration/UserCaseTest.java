package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
 * 用户用例测试.
 *
 * @author dongbin
 * @version 0.1 2020/11/29 17:37
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
public class UserCaseTest {

    final Logger logger = LoggerFactory.getLogger(UserCaseTest.class);

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


    /**
     * 测试的重复次数.
     */
    private static final int TEST_LOOPS = 10;

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
            TimeUnit.MILLISECONDS.sleep(100);
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

    @Test
    public void testCountEmptyPage() throws Exception {
        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        IEntity[] targetEntities = IntStream.range(0, 10).mapToObj(i ->
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(
                    Arrays.asList(
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                    )
                ).build()
        ).toArray(IEntity[]::new);

        entityManagementService.build(targetEntities);

        // 事务内统计.
        Page page = Page.emptyPage();
        transactionManager.bind(tx.id());
        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build()
        );
        Assertions.assertEquals(0, entities.getValue().get().size());
        Assertions.assertEquals(targetEntities.length, page.getTotalCount());

        transactionManager.bind(tx.id());
        tx.commit();
        transactionManager.finish();

        // 事务外统计.
        page = Page.emptyPage();
        entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 990L)
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build()
        );
    }

    @Test
    public void testUncommitSearch() throws Exception {
        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        transactionManager.bind(tx.id());
        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L)
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, entities.getValue().get().size());
        Assertions.assertTrue(
            entities.getValue().get().stream().findFirst().get().entityValue().getValue("l0-long").isPresent());
        Assertions.assertEquals(99L,
            entities.getValue().get().stream().findFirst().get().entityValue().getValue("l0-long").get().getValue());

        tx.commit();
        transactionManager.finish();
    }

    @Test
    public void testUpdateAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        entity.entityValue().addValue(new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 1L));
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        transactionManager.bind(tx.id());
        OqsResult<Collection<IEntity>> result =
            entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                    ConditionOperator.NOT_EQUALS,
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 1L)
                )
            ), MockEntityClassDefine.L2_ENTITY_CLASS.ref(), Page.newSinglePage(10));

        Assertions.assertEquals(0, result.getValue().get().size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();
    }

    @Test
    public void testDeleteAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.delete(entity).getResultStatus());

        transactionManager.bind(tx.id());
        OqsResult<Collection<IEntity>> result =
            entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                    ConditionOperator.NOT_EQUALS,
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 1L)
                )
            ), MockEntityClassDefine.L2_ENTITY_CLASS.ref(), Page.newSinglePage(10));

        Assertions.assertEquals(0, result.getValue().get().size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();
    }

    @Test
    public void testUpdateFatherSearchChild() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
        IEntity fatherEntity =
            entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L0_ENTITY_CLASS.ref()).getValue().get();

        fatherEntity.entityValue()
            .addValue(new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 100L));

        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(fatherEntity).getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 100L)
                )),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            Page.newSinglePage(100)
        );

        Assertions.assertEquals(1, entities.getValue().get().size());

    }

    /**
     * 更新后查询不等值总数匹配.
     */
    @Test
    public void testUpdateAfterNotEqCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 1; i <= TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref()).getValue()
                .get();
            entity.entityValue().addValue(
                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), Long.toString(i))
            );
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                    )
                ).addAnd(new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L)
                )),
                MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
                page
            );

            Assertions.assertEquals(1, page.getTotalCount());
        }

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(0, commitIdStatusService.size());
    }

    /**
     * 不断创建马上查询.
     */
    @Test
    public void testBuildAfterRead() throws Exception {
        IEntity entity;
        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), i),
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                    )
                ).build();
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            IEntity selectEntity =
                entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref()).getValue()
                    .get();

            Assertions.assertNotEquals(0, selectEntity.id());

            Assertions.assertEquals(i, selectEntity.entityValue().getValue("l0-long").get().valueToLong());
            Assertions.assertEquals("0", selectEntity.entityValue().getValue("l2-string").get().valueToString());
        }

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(0, commitIdStatusService.size());
    }

    @Test
    public void testBuildAfterDelete() throws Exception {
        IEntity entity;
        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                    )
                ).build();
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            Assertions
                .assertEquals(ResultStatus.SUCCESS, entityManagementService.deleteForce(entity).getResultStatus());

            Page page = Page.newSinglePage(100);
            OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                    )
                ),
                MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
                page
            );

            Assertions.assertEquals(0, entities.getValue().get().size());
            Assertions.assertEquals(0, page.getTotalCount());
        }
    }

    /**
     * 测试不断的更新已有数据,并立即查询后的结果.
     */
    @Test
    public void testUpdateAfterRead() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .getValue().get();
            entity.entityValue().addValue(
                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), Long.toString(i))
            );

            OqsResult opResult = entityManagementService.replace(entity);
            ResultStatus status = opResult.getResultStatus();
            Assertions.assertTrue(ResultStatus.SUCCESS == status);

            Page page = Page.newSinglePage(100);
            OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), Long.toString(i))
                    )
                ),
                MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
                page
            );
            Assertions.assertEquals(0, page.getTotalCount());

            Assertions.assertEquals(0, entities.getValue().get().size());
        }

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(0, commitIdStatusService.size());
    }

    @Test
    public void testOneSort() throws Exception {
        long[] fistFieldValues = new long[] {
            10, 9, 5, 8, 6, 10
        };

        List<IEntity> expectedEntities = new ArrayList<>(fistFieldValues.length);
        for (int i = 0; i < fistFieldValues.length; i++) {
            expectedEntities.add(Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                            fistFieldValues[i])
                    )
                ).build());
        }
        OqsResult results = entityManagementService.build(expectedEntities.stream().toArray(IEntity[]::new));
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100))
                .withSort(Sort.buildDescSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get()))
                .build()
        );

        long[] expectedFirstValues = new long[] {
            10, 10, 9, 8, 6, 5
        };

        long[] firstValues = entities.getValue().get().stream()
            .mapToLong(e -> e.entityValue().getValue("l0-long").get().valueToLong()).toArray();
        Assertions.assertArrayEquals(expectedFirstValues, firstValues);

        // 两个数值一致的对象id应该从小到大.
        IEntity firstEntity = entities.getValue().get().stream().findFirst().get();
        IEntity secondEntity = entities.getValue().get().stream().skip(1).findFirst().get();

        Assertions.assertTrue(firstEntity.id() < secondEntity.id(),
            String.format("The first ID (%d) is expected to be less than the second (%d), but it is not.",
                firstEntity.id(), secondEntity.id()));

        // 为了保证已经同步.
        for (IEntity entity : expectedEntities) {
            entityManagementService.replace(entity);
        }

        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100))
                .withSort(Sort.buildDescSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get()))
                .build()
        );

        expectedFirstValues = new long[] {
            10, 10, 9, 8, 6, 5
        };

        firstValues = entities.getValue().get().stream()
            .mapToLong(e -> e.entityValue().getValue("l0-long").get().valueToLong()).toArray();
        Assertions.assertArrayEquals(expectedFirstValues, firstValues);

        // 两个数值一致的对象id应该从小到大.
        firstEntity = entities.getValue().get().stream().findFirst().get();
        secondEntity = entities.getValue().get().stream().skip(1).findFirst().get();

        Assertions.assertTrue(firstEntity.id() < secondEntity.id(),
            String.format("The first ID (%d) is expected to be less than the second (%d), but it is not.",
                firstEntity.id(), secondEntity.id()));
    }

    /**
     * 测试三字段排序.
     */
    @Test
    public void testThreeSort() throws Exception {
        long[] fistFieldValues = new long[] {
            10, 9, 5, 8, 6, 10
        };
        long[] secondFieldValues = new long[] {
            7, 6, 4, 4, 5, 4
        };
        long[] thridFieldValues = new long[] {
            5, 3, 3, 3, 4, 3
        };

        List<IEntity> expectedEntities = new ArrayList<>(fistFieldValues.length);
        for (int i = 0; i < fistFieldValues.length; i++) {
            expectedEntities.add(Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                            fistFieldValues[i]),
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l1-long").get(),
                            secondFieldValues[i]),
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-long").get(),
                            thridFieldValues[i])
                    )
                ).build());
        }
        OqsResult results = entityManagementService.build(expectedEntities.stream().toArray(IEntity[]::new));
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100))
                .withSort(Sort.buildDescSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get()))
                .withSecondarySort(Sort.buildDescSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l1-long").get()))
                .withThridSort(Sort.buildAscSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-long").get()))
                .build()
        );

        long[] expectedFirstValues = new long[] {
            10, 10, 9, 8, 6, 5
        };
        long[] expectedSecondValues = new long[] {
            7, 4, 6, 4, 5, 4
        };
        long[] expectedThridValues = new long[] {
            5, 3, 3, 3, 4, 3
        };

        long[] firstValues = entities.getValue().get().stream()
            .mapToLong(e -> e.entityValue().getValue("l0-long").get().valueToLong()).toArray();
        long[] secondValues = entities.getValue().get().stream()
            .mapToLong(e -> e.entityValue().getValue("l1-long").get().valueToLong()).toArray();
        long[] thridValues = entities.getValue().get().stream()
            .mapToLong(e -> e.entityValue().getValue("l2-long").get().valueToLong()).toArray();

        Assertions.assertArrayEquals(expectedFirstValues, firstValues);
        Assertions.assertArrayEquals(expectedSecondValues, secondValues);
        Assertions.assertArrayEquals(expectedThridValues, thridValues);
    }

    @Test
    public void testSortTwoDec() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        new BigDecimal("123.17")),
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec2").get(),
                        new BigDecimal("123.15"))
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        new BigDecimal("123.17")),
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec2").get(),
                        new BigDecimal("122"))
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        entityManagementService.replace(e0);
        entityManagementService.replace(e1);

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(1000))
                .withSort(Sort.buildAscSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get()))
                .withSecondarySort(Sort.buildAscSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec2").get()))
                .build()
        );

        Assertions.assertEquals(2, entities.getValue().get().size());
        Assertions.assertEquals(e1.id(), entities.getValue().get().stream().findFirst().get().id());
        Assertions.assertEquals(e0.id(), entities.getValue().get().stream().skip(1).findFirst().get().id());
    }

    // 测试排序,但是记录中没有排序的值.应该使用默认值作为排序字段.
    @Test
    public void testSortButNoValue() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 100L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 200L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2).getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            Sort.buildAscSort(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get()),
            Page.newSinglePage(100));

        Assertions.assertEquals(3, entities.getValue().get().size());

        Assertions.assertFalse(entities.getValue().get().stream()
            .findFirst().get().entityValue().getValue("l0-long").isPresent());

        Assertions.assertEquals(100L,
            entities.getValue().get().stream().skip(1).findFirst().get().entityValue().getValue("l0-long").get()
                .valueToLong());
        Assertions.assertEquals(200L,
            entities.getValue().get().stream().skip(2).findFirst().get().entityValue().getValue("l0-long").get()
                .valueToLong());
    }

    /**
     * 测试更新后统计数量.
     */
    @Test
    public void testUpdateAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity =
                entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref()).getValue()
                    .get();
            entity.entityValue().addValue(
                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), i)
            );
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), i))
                ),
                MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
                page
            );

            Assertions.assertEquals(1, page.getTotalCount());
        }
    }

    @Test
    public void testDeleteAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                )
            ).build();


        for (int i = 0; i < TEST_LOOPS; i++) {
            entity.resetId(0);
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

            entityManagementService.deleteForce(entity);

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "0")
                    )),
                MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
                page
            );

            Assertions.assertEquals(0, page.getTotalCount());
        }

    }

    /**
     * 测试浮点数是否正确写入并可以正确查询.
     */
    @Test
    public void testDecimal() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        new BigDecimal("123.789"))
                )
            ).build();

        // 新创建
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity =
            entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref()).getValue().get();

        entity.entityValue().addValue(
            new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(), new BigDecimal("789.123"))
        );
        // 更新保证进入索引中.
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                    ConditionOperator.EQUALS,
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        new BigDecimal("789.123"))
                )
            ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());
        entity = entities.getValue().get().stream().findFirst().get();
        Assertions.assertEquals("789.123", entity.entityValue().getValue("l2-dec").get().valueToString());
    }

    /**
     * 测试多值字符串的写入后查询.
     */
    @Test
    public void testStrings() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringsValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-strings").get(),
                        "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
                )
            ).build();

        // 新创建
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity =
            entitySearchService.selectOne(entity.id(), MockEntityClassDefine.L2_ENTITY_CLASS.ref()).getValue().get();

        entity.entityValue().addValue(
            new StringsValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-strings").get(),
                "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
        );
        // 更新保证进入索引中.
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-strings").get(),
                    ConditionOperator.EQUALS,
                    new StringsValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-strings").get(), "PURPLE")
                )
            ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());
    }

    @Test
    public void testLikeWithSegmentation() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringValue(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "上海票易通股份有限公司分词查询")
                )
            ).build();

        entityManagementService.build(entity);

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "上海")
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());

        // 更新会等等CDC同步结束才返回,这里是为了保证查询落在索引中.
        entityManagementService.replace(entity);

        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "上海")
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());

        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "股份有限公司")
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());
    }

    @Test
    public void testlookupString() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "v1")
                )
            ).build();
        OqsResult result = entityManagementService.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        // 创建200个lookup实例.
        int lookupSize = 200;
        Collection<IEntity> lookupEntities = new ArrayList<>(lookupSize);
        for (int i = 0; i < lookupSize; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LookupValue(
                            MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-string").get(),
                            targetEntity.id()),
                        new LongValue(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("l2-lookup.id").get(),
                            targetEntity.id())
                    )
                ).build();
            lookupEntities.add(lookupEntity);

            result = entityManagementService.build(lookupEntity);
            Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        }


        OqsResult<Collection<IEntity>> queryLookupEntities = entitySearchService.selectMultiple(
            lookupEntities.stream().mapToLong(e -> e.id()).toArray(), MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref());
        Assertions.assertEquals(lookupEntities.size(), queryLookupEntities.getValue().get().size());
        // 验证是否成功lookup.
        queryLookupEntities.getValue().get().forEach(e -> {

            // 此次更新是为了验证,已经lookup过的字段再次更新是否正确.
            try {
                entityManagementService.replace(e);
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }

            Assertions.assertEquals(
                targetEntity.entityValue().getValue("l2-string").get().valueToString(),
                e.entityValue().getValue("lookup-l2-string").get().valueToString());
        });


        IEntity newTargetEntity = Entity.Builder.anEntity()
            .withId(targetEntity.id())
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "v2")
                )
            ).build();
        result = entityManagementService.replace(newTargetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        boolean success = false;
        long successSize = 0;
        for (int i = 0; i < 10000; i++) {
            queryLookupEntities = entitySearchService.selectMultiple(
                lookupEntities.stream().mapToLong(e -> e.id()).toArray(),
                MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref());

            successSize =
                queryLookupEntities.getValue().get().stream()
                    .filter(
                        e -> e.entityValue().getValue("lookup-l2-string").get().valueToString().equals("v2"))
                    .count();

            if (lookupSize != successSize) {
                logger.info("There are {} entities lookup target, currently agreed number {}, remaining {}.",
                    lookupSize, successSize, lookupSize - successSize);
            } else {
                success = true;
                break;
            }
        }

        Assertions.assertTrue(success, String.format("The expected number of lookups is %d, but it is %d.",
            lookupSize, successSize));

        OqsResult<Collection<IEntity>> conditionQueryEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-string").get(),
                            "v2"))
                ),
            MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(200)).build()
        );

        Assertions.assertEquals(lookupSize, conditionQueryEntities.getValue().get().size());
        Assertions.assertEquals(lookupSize, conditionQueryEntities.getValue().get().stream().filter(
            e -> e.entityValue().getValue("lookup-l2-string").get().valueToString().equals("v2")
        ).count());
    }

    @Test
    public void testLookupDec() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        BigDecimal.valueOf(12.3333D))
                )
            ).build();
        OqsResult result = entityManagementService.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        // 创建200个lookup实例.
        int lookupSize = 200;
        Collection<IEntity> lookupEntities = new ArrayList<>(lookupSize);
        for (int i = 0; i < lookupSize; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                        new LookupValue(
                            MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-dec").get(),
                            targetEntity.id()),
                        new LongValue(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("l2-lookup.id").get(),
                            targetEntity.id())
                    )
                ).build();
            lookupEntities.add(lookupEntity);

            result = entityManagementService.build(lookupEntity);
            Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        }

        OqsResult<Collection<IEntity>> queryLookupEntities = entitySearchService.selectMultiple(
            lookupEntities.stream().mapToLong(e -> e.id()).toArray(), MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref());
        Assertions.assertEquals(lookupEntities.size(), queryLookupEntities.getValue().get().size());
        // 验证是否成功lookup.
        queryLookupEntities.getValue().get().forEach(e -> {
            Assertions.assertEquals(
                targetEntity.entityValue().getValue("l2-dec").get().valueToString(),
                e.entityValue().getValue("lookup-l2-dec").get().valueToString());
        });

        IEntity newTargetEntity = Entity.Builder.anEntity()
            .withId(targetEntity.id())
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-dec").get(),
                        BigDecimal.valueOf(13.3333D))
                )
            ).build();
        result = entityManagementService.replace(newTargetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        boolean success = false;
        long successSize = 0;
        for (int i = 0; i < 10000; i++) {
            queryLookupEntities = entitySearchService.selectMultiple(
                lookupEntities.stream().mapToLong(e -> e.id()).toArray(),
                MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref());

            successSize =
                queryLookupEntities.getValue().get().stream()
                    .filter(
                        e -> e.entityValue().getValue("lookup-l2-dec").get().valueToString().equals("13.3333"))
                    .count();

            if (lookupSize != successSize) {
                logger.info("There are {} entities lookup target, currently agreed number {}, remaining {}.",
                    lookupSize, successSize, lookupSize - successSize);
            } else {
                success = true;
                break;
            }
        }

        Assertions.assertTrue(success, String.format("The expected number of lookups is %d, but it is %d.",
            lookupSize, successSize));

        OqsResult<Collection<IEntity>> conditionQueryEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(MockEntityClassDefine.LOOKUP_ENTITY_CLASS.field("lookup-l2-dec").get(),
                            BigDecimal.valueOf(13.3333D)))
                ),
            MockEntityClassDefine.LOOKUP_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(200)).build()
        );

        Assertions.assertEquals(lookupSize, conditionQueryEntities.getValue().get().size());
        Assertions.assertEquals(lookupSize, conditionQueryEntities.getValue().get().stream().filter(
            e -> e.entityValue().getValue("lookup-l2-dec").get().valueToString().equals("13.3333")
        ).count());
    }

    @Test
    public void testSegmentation() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
            .withValues(Arrays.asList(
                    new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "上海云砺有限公司")
                )
            ).build();

        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(targetEntity).getResultStatus());

        OqsResult<Collection<IEntity>> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "有限公司")
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, entities.getValue().get().size());
        Assertions.assertEquals("上海云砺有限公司",
            entities.getValue().get().stream().findFirst().get().entityValue().getValue("l0-string").get()
                .valueToString());

        entityManagementService.replace(targetEntity);
        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-string").get(), "有限公司")
                    )
                ),
            MockEntityClassDefine.L2_ENTITY_CLASS.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, entities.getValue().get().size());
        Assertions.assertEquals("上海云砺有限公司",
            entities.getValue().get().stream().findFirst().get().entityValue().getValue("l0-string").get()
                .valueToString());
    }
}
