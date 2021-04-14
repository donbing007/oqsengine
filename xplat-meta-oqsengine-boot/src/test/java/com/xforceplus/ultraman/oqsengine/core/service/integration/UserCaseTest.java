package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
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
    public void testUpdateAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        entity.entityValue().addValue(new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L));
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity));

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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.delete(entity));

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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));
        IEntity fatherEntity = entitySearchService.selectOne(entity.id(), MockMetaManager.l0EntityClass.ref()).get();

        fatherEntity.entityValue().addValue(new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 100L));

        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(fatherEntity));

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
     *
     * @throws Exception
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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

        for (int i = 1; i <= TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), Long.toString(i))
            );
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity));

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
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));
            IEntity selectEntity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();

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
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.deleteForce(entity));

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
     *
     * @throws Exception
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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

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
     *
     * @throws Exception
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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0));

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 1L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1));

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 2L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2));

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(MockMetaManager.l2EntityClass.field("l2-string").get(), ConditionOperator.EQUALS,
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0"))
            ),
            MockMetaManager.l2EntityClass.ref(),
            Sort.buildAscSort(MockMetaManager.l2EntityClass.field("l2-string").get()), Page.newSinglePage(100));

        Assert.assertEquals(3, entities.size());
        Assert.assertEquals(0L,
            entities.stream().findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
        Assert.assertEquals(1L,
            entities.stream().skip(1).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
        Assert.assertEquals(2L,
            entities.stream().skip(2).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0));

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 200L),
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1));

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockMetaManager.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringValue(MockMetaManager.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2));

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
     *
     * @throws Exception
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
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockMetaManager.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), i)
            );
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity));

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
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity));

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
}
