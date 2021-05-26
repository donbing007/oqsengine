package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 用户用例测试.
 *
 * @author dongbin
 * @version 0.1 2020/11/29 17:37
 * @since 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @MockBean
    private MetaManager metaManager;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    private static final int TEST_LOOPS = 10;

    @Before
    public void before() throws Exception {
        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }


        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex0");
                    stat.executeUpdate("truncate table oqsindex1");
                }
            }
        }

        Mockito.when(metaManager.load(MockMetaManager.l0EntityClass.id()))
            .thenReturn(Optional.of(MockMetaManager.l0EntityClass));

        Mockito.when(metaManager.load(MockMetaManager.l1EntityClass.id()))
            .thenReturn(Optional.of(MockMetaManager.l1EntityClass));

        Mockito.when(metaManager.load(MockMetaManager.l2EntityClass.id()))
            .thenReturn(Optional.of(MockMetaManager.l2EntityClass));
    }

    @After
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
                    stat.executeUpdate("truncate table oqsindex0");
                    stat.executeUpdate("truncate table oqsindex1");
                }
            }
        }

    }

    @Test
    public void testUncommitSearch() throws Exception {
        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L)
                    )
                ),
            MockMetaManager.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(99L,
            entities.stream().findFirst().get().entityValue().getValue("l0-long").get().getValue());

        tx.commit();
        transactionManager.finish();
    }

    @Test
    public void testUpdateAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        entity.entityValue().addValue(new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L));
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> result = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
            new Condition(
                MockMetaManager.l2EntityClass.field("l0-long").get(),
                ConditionOperator.NOT_EQUALS,
                new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L)
            )
        ), MockMetaManager.l2EntityClass.ref(), Page.newSinglePage(10));

        Assert.assertEquals(0, result.size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.unbind();
    }

    @Test
    public void testDeleteAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.delete(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> result = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
            new Condition(
                MockMetaManager.l2EntityClass.field("l0-long").get(),
                ConditionOperator.NOT_EQUALS,
                new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L)
            )
        ), MockMetaManager.l2EntityClass.ref(), Page.newSinglePage(10));

        Assert.assertEquals(0, result.size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.unbind();
    }

    @Test
    public void testUpdateFatherSearchChild() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
        IEntity fatherEntity = entitySearchService.selectOne(entity.id(), MockMetaManager.l0EntityClass.ref()).get();

        fatherEntity.entityValue().addValue(new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 100L));

        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(fatherEntity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    MockMetaManager.l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 100L)
                )),
            MockMetaManager.l2EntityClass.ref(),
            Page.newSinglePage(100)
        );

        Assert.assertEquals(1, entities.size());

    }

    /**
     * 更新后查询不等值总数匹配.
     */
    @Test
    public void testUpdateAfterNotEqCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 1; i <= TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), Long.toString(i))
            );
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                    )
                ).addAnd(new Condition(
                    MockMetaManager.l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L)
                )),
                MockMetaManager.l2EntityClass.ref(),
                page
            );

            Assert.assertEquals(1, page.getTotalCount());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());

    }

    /**
     * 不断创建马上查询.
     */
    @Test
    public void testBuildAfterRead() throws Exception {
        IEntity entity;
        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValues(Arrays.asList(
                        new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), i),
                        new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                    ))
                ).build();
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            IEntity selectEntity =
                entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();

            Assert.assertNotEquals(0, selectEntity.id());

            Assert.assertEquals(i, selectEntity.entityValue().getValue("l0-long").get().valueToLong());
            Assert.assertEquals("0", selectEntity.entityValue().getValue("l2-string").get().valueToString());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());
    }

    @Test
    public void testBuildAfterDelete() throws Exception {
        IEntity entity;
        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValues(Arrays.asList(
                        new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                        new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                    ))
                ).build();
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.deleteForce(entity).getResultStatus());

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                    )
                ),
                MockMetaManager.l2EntityClass.ref(),
                page
            );

            Assert.assertEquals(0, entities.size());
            Assert.assertEquals(0, page.getTotalCount());
        }
    }

    /**
     * 测试不断的更新已有数据,并立即查询后的结果.
     */
    @Test
    public void testUpdateAfterRead() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), Long.toString(i))
            );

            OperationResult opResult = entityManagementService.replace(entity);
            ResultStatus status = opResult.getResultStatus();
            Assert.assertTrue(ResultStatus.SUCCESS == status);

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(MockMetaManager.l2EntityClass.field("l2-string").get(), Long.toString(i))
                    )
                ),
                MockMetaManager.l2EntityClass.ref(),
                page
            );
            Assert.assertEquals(0, page.getTotalCount());

            Assert.assertEquals(0, entities.size());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());
    }

    /**
     * 测试排序.
     */
    @Test
    public void testSort() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 0L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 2L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(MockMetaManager.l2EntityClass.field("l2-string").get(), ConditionOperator.EQUALS,
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0"))
            ),
            MockMetaManager.l2EntityClass.ref(),
            Sort.buildAscSort(MockMetaManager.l2EntityClass.field("l0-long").get()), Page.newSinglePage(100));

        Assert.assertEquals(3, entities.size());
        Assert.assertEquals(e0.id(), entities.stream().findFirst().get().id());
        Assert.assertEquals(e1.id(), entities.stream().skip(1).findFirst().get().id());
        Assert.assertEquals(e2.id(), entities.stream().skip(2).findFirst().get().id());
    }

    // 测试排序,但是记录中没有排序的值.应该使用默认值作为排序字段.
    @Test
    public void testSortButNoValue() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 100L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 200L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockMetaManager.l2EntityClass.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                )),
            MockMetaManager.l2EntityClass.ref(),
            Sort.buildAscSort(MockMetaManager.l2EntityClass.field("l0-long").get()),
            Page.newSinglePage(100));

        Assert.assertEquals(3, entities.size());

        Assert.assertFalse(entities.stream().findFirst().get().entityValue().getValue("l0-long").isPresent());

        Assert.assertEquals(100L,
            entities.stream().skip(1).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
        Assert.assertEquals(200L,
            entities.stream().skip(2).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
    }

    /**
     * 测试更新后统计数量.
     */
    @Test
    public void testUpdateAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), i)
            );
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), i))
                ),
                MockMetaManager.l2EntityClass.ref(),
                page
            );

            Assert.assertEquals(1, page.getTotalCount());
        }
    }

    @Test
    public void testDeleteAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();


        for (int i = 0; i < TEST_LOOPS; i++) {
            entity.resetId(0);
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

            entityManagementService.deleteForce(entity);

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockMetaManager.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                    )),
                MockMetaManager.l2EntityClass.ref(),
                page
            );

            Assert.assertEquals(0, page.getTotalCount());
        }
    }

    /**
     * 测试浮点数是否正确写入并可以正确查询.
     */
    @Test
    public void testDecimal() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new DecimalValue(MockMetaManager.l2EntityClass.field("l2-dec").get(), new BigDecimal("123.789"))
                ))
            ).build();

        // 新创建
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();

        entity.entityValue().addValue(
            new DecimalValue(MockMetaManager.l2EntityClass.field("l2-dec").get(), new BigDecimal("789.123"))
        );
        // 更新保证进入索引中.
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockMetaManager.l2EntityClass.field("l2-dec").get(),
                    ConditionOperator.EQUALS,
                    new DecimalValue(MockMetaManager.l2EntityClass.field("l2-dec").get(), new BigDecimal("789.123"))
                )
            ),
            MockMetaManager.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assert.assertEquals(1, entities.size());
        entity = entities.stream().findFirst().get();
        Assert.assertEquals("789.123", entity.entityValue().getValue("l2-dec").get().valueToString());
    }

    /**
     * 测试多值字符串的写入后查询.
     */
    @Test
    public void testStrings() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringsValue(MockMetaManager.l2EntityClass.field("l0-strings").get(),
                        "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
                ))
            ).build();

        // 新创建
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();

        entity.entityValue().addValue(
            new StringsValue(MockMetaManager.l2EntityClass.field("l0-strings").get(),
                "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
        );
        // 更新保证进入索引中.
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockMetaManager.l2EntityClass.field("l0-strings").get(),
                    ConditionOperator.EQUALS,
                    new StringsValue(MockMetaManager.l2EntityClass.field("l0-strings").get(), "PURPLE")
                )
            ),
            MockMetaManager.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assert.assertEquals(1, entities.size());
    }
}
