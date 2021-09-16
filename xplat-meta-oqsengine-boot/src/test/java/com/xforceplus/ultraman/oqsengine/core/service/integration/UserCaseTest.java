package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 用户用例测试.
 *
 * @author dongbin
 * @version 0.1 2020/11/29 17:37
 * @since 1.8
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserCaseTest extends AbstractContainerExtends {

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
                    stat.executeUpdate("truncate table oqsindex0");
                    stat.executeUpdate("truncate table oqsindex1");
                }
            }
        }

        Mockito.when(metaManager.load(MockEntityClassDefine.l0EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l0EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l1EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l1EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l2EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l2EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l0EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l0EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l1EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l1EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l2EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l2EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.lookupEntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.lookupEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.lookupEntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.lookupEntityClass));
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
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L)
                    )
                ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, entities.size());
        Assertions.assertTrue(entities.stream().findFirst().get().entityValue().getValue("l0-long").isPresent());
        Assertions.assertEquals(99L,
            entities.stream().findFirst().get().entityValue().getValue("l0-long").get().getValue());

        tx.commit();
        transactionManager.finish();
    }

    @Test
    public void testUpdateAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        entity.entityValue().addValue(new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 1L));
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> result = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
            new Condition(
                MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                ConditionOperator.NOT_EQUALS,
                new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 1L)
            )
        ), MockEntityClassDefine.l2EntityClass.ref(), Page.newSinglePage(10));

        Assertions.assertEquals(0, result.size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.unbind();
    }

    @Test
    public void testDeleteAfterQueryInTx() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        // 等待CDC同步.
        TimeUnit.SECONDS.sleep(3L);

        Transaction tx = transactionManager.create(5000);
        transactionManager.bind(tx.id());

        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.delete(entity).getResultStatus());

        transactionManager.bind(tx.id());
        Collection<IEntity> result = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions().addAnd(
            new Condition(
                MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                ConditionOperator.NOT_EQUALS,
                new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 1L)
            )
        ), MockEntityClassDefine.l2EntityClass.ref(), Page.newSinglePage(10));

        Assertions.assertEquals(0, result.size());

        transactionManager.getCurrent().get().rollback();
        transactionManager.unbind();
    }

    @Test
    public void testUpdateFatherSearchChild() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
        IEntity fatherEntity =
            entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l0EntityClass.ref()).get();

        fatherEntity.entityValue()
            .addValue(new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 100L));

        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(fatherEntity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 100L)
                )),
            MockEntityClassDefine.l2EntityClass.ref(),
            Page.newSinglePage(100)
        );

        Assertions.assertEquals(1, entities.size());

    }

    /**
     * 更新后查询不等值总数匹配.
     */
    @Test
    public void testUpdateAfterNotEqCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 1; i <= TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), Long.toString(i))
            );
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                    )
                ).addAnd(new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L)
                )),
                MockEntityClassDefine.l2EntityClass.ref(),
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
                .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), i),
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                    ))
                ).build();
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            IEntity selectEntity =
                entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();

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
                .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValues(Arrays.asList(
                        new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                    ))
                ).build();
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());
            Assertions
                .assertEquals(ResultStatus.SUCCESS, entityManagementService.deleteForce(entity).getResultStatus());

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                    )
                ),
                MockEntityClassDefine.l2EntityClass.ref(),
                page
            );

            Assertions.assertEquals(0, entities.size());
            Assertions.assertEquals(0, page.getTotalCount());
        }
    }

    /**
     * 测试不断的更新已有数据,并立即查询后的结果.
     */
    @Test
    public void testUpdateAfterRead() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), Long.toString(i))
            );

            OperationResult opResult = entityManagementService.replace(entity);
            ResultStatus status = opResult.getResultStatus();
            Assertions.assertTrue(ResultStatus.SUCCESS == status);

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), Long.toString(i))
                    )
                ),
                MockEntityClassDefine.l2EntityClass.ref(),
                page
            );
            Assertions.assertEquals(0, page.getTotalCount());

            Assertions.assertEquals(0, entities.size());
        }

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(0, commitIdStatusService.size());
    }

    /**
     * 测试排序.
     */
    @Test
    public void testSort() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 0L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 1L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 2L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0"))
            ),
            MockEntityClassDefine.l2EntityClass.ref(),
            Sort.buildAscSort(MockEntityClassDefine.l2EntityClass.field("l0-long").get()), Page.newSinglePage(100));

        Assertions.assertEquals(3, entities.size());
        Assertions.assertEquals(e0.id(), entities.stream().findFirst().get().id());
        Assertions.assertEquals(e1.id(), entities.stream().skip(1).findFirst().get().id());
        Assertions.assertEquals(e2.id(), entities.stream().skip(2).findFirst().get().id());
    }

    // 测试排序,但是记录中没有排序的值.应该使用默认值作为排序字段.
    @Test
    public void testSortButNoValue() throws Exception {
        IEntity e0 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 100L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e0).getResultStatus());

        IEntity e1 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 200L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e1).getResultStatus());

        IEntity e2 = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(e2).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                )),
            MockEntityClassDefine.l2EntityClass.ref(),
            Sort.buildAscSort(MockEntityClassDefine.l2EntityClass.field("l0-long").get()),
            Page.newSinglePage(100));

        Assertions.assertEquals(3, entities.size());

        Assertions.assertFalse(entities.stream().findFirst().get().entityValue().getValue("l0-long").isPresent());

        Assertions.assertEquals(100L,
            entities.stream().skip(1).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
        Assertions.assertEquals(200L,
            entities.stream().skip(2).findFirst().get().entityValue().getValue("l0-long").get().valueToLong());
    }

    /**
     * 测试更新后统计数量.
     */
    @Test
    public void testUpdateAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        for (int i = 0; i < TEST_LOOPS; i++) {
            entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();
            entity.entityValue().addValue(
                new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), i)
            );
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), i))
                ),
                MockEntityClassDefine.l2EntityClass.ref(),
                page
            );

            Assertions.assertEquals(1, page.getTotalCount());
        }
    }

    @Test
    public void testDeleteAfterCount() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new LongValue(MockEntityClassDefine.l2EntityClass.field("l0-long").get(), 99L),
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                ))
            ).build();


        for (int i = 0; i < TEST_LOOPS; i++) {
            entity.resetId(0);
            Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

            entityManagementService.deleteForce(entity);

            Page page = Page.emptyPage();
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "0")
                    )),
                MockEntityClassDefine.l2EntityClass.ref(),
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
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new DecimalValue(MockEntityClassDefine.l2EntityClass.field("l2-dec").get(),
                        new BigDecimal("123.789"))
                ))
            ).build();

        // 新创建
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();

        entity.entityValue().addValue(
            new DecimalValue(MockEntityClassDefine.l2EntityClass.field("l2-dec").get(), new BigDecimal("789.123"))
        );
        // 更新保证进入索引中.
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l2-dec").get(),
                    ConditionOperator.EQUALS,
                    new DecimalValue(MockEntityClassDefine.l2EntityClass.field("l2-dec").get(),
                        new BigDecimal("789.123"))
                )
            ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.size());
        entity = entities.stream().findFirst().get();
        Assertions.assertEquals("789.123", entity.entityValue().getValue("l2-dec").get().valueToString());
    }

    /**
     * 测试多值字符串的写入后查询.
     */
    @Test
    public void testStrings() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringsValue(MockEntityClassDefine.l2EntityClass.field("l0-strings").get(),
                        "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
                ))
            ).build();

        // 新创建
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.build(entity).getResultStatus());

        entity = entitySearchService.selectOne(entity.id(), MockEntityClassDefine.l2EntityClass.ref()).get();

        entity.entityValue().addValue(
            new StringsValue(MockEntityClassDefine.l2EntityClass.field("l0-strings").get(),
                "BLUE", "RED", "YELLOW", "PURPLE", "GOLDEN")
        );
        // 更新保证进入索引中.
        Assertions.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity).getResultStatus());

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l0-strings").get(),
                    ConditionOperator.EQUALS,
                    new StringsValue(MockEntityClassDefine.l2EntityClass.field("l0-strings").get(), "PURPLE")
                )
            ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.size());
    }

    @Test
    public void testLikeWithSegmentation() throws Exception {
        IEntity entity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValues(Arrays.asList(
                    new StringValue(
                        MockEntityClassDefine.l2EntityClass.field("l0-string").get(), "上海票易通股份有限公司分词查询")
                ))
            ).build();

        entityManagementService.build(entity);

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l0-string").get(), "上海")
                    )
                ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.size());

        // 更新会等等CDC同步结束才返回,这里是为了保证查询落在索引中.
        entityManagementService.replace(entity);

        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l0-string").get(), "上海")
                    )
                ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.size());

        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l0-string").get(), "股份有限公司")
                    )
                ),
            MockEntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, entities.size());
    }

    @Test
    public void testlookupString() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v1")
                )
            ).build();
        OperationResult result = entityManagementService.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        // 创建200个lookup实例.
        int lookupSize = 200;
        Collection<IEntity> lookupEntities = new ArrayList<>(lookupSize);
        for (int i = 0; i < lookupSize; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.lookupEntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValue(
                        new LookupValue(
                            MockEntityClassDefine.lookupEntityClass.field("lookup-l2-string").get(),
                            targetEntity.id())
                    ).addValue(
                        new LongValue(MockEntityClassDefine.lookupEntityClass.field("l2-lookup.id").get(),
                            targetEntity.id())
                    )
                ).build();
            lookupEntities.add(lookupEntity);

            result = entityManagementService.build(lookupEntity);
            Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        }

        Collection<IEntity> queryLookupEntities = entitySearchService.selectMultiple(
            lookupEntities.stream().mapToLong(e -> e.id()).toArray(), MockEntityClassDefine.lookupEntityClass.ref());
        Assertions.assertEquals(lookupEntities.size(), queryLookupEntities.size());
        // 验证是否成功lookup.
        queryLookupEntities.forEach(e -> {

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
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v2")
                )
            ).build();
        entityManagementService.replace(newTargetEntity);

        boolean success = false;
        long successSize = 0;
        for (int i = 0; i < 10000; i++) {
            queryLookupEntities = entitySearchService.selectMultiple(
                lookupEntities.stream().mapToLong(e -> e.id()).toArray(),
                MockEntityClassDefine.lookupEntityClass.ref());

            successSize =
                queryLookupEntities.stream()
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

        Collection<IEntity> conditionQueryEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.lookupEntityClass.field("lookup-l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockEntityClassDefine.lookupEntityClass.field("lookup-l2-string").get(), "v2"))
                ),
            MockEntityClassDefine.lookupEntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(200)).build()
        );

        Assertions.assertEquals(lookupSize, conditionQueryEntities.size());
        Assertions.assertEquals(lookupSize, conditionQueryEntities.stream().filter(
            e -> e.entityValue().getValue("lookup-l2-string").get().valueToString().equals("v2")
        ).count());
    }

    @Test
    public void testLookupDec() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new DecimalValue(MockEntityClassDefine.l2EntityClass.field("l2-dec").get(),
                        BigDecimal.valueOf(12.3333D))
                )
            ).build();
        OperationResult result = entityManagementService.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());

        // 创建200个lookup实例.
        int lookupSize = 200;
        Collection<IEntity> lookupEntities = new ArrayList<>(lookupSize);
        for (int i = 0; i < lookupSize; i++) {
            IEntity lookupEntity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.lookupEntityClass.ref())
                .withEntityValue(
                    EntityValue.build().addValue(
                        new LongValue(
                            MockEntityClassDefine.lookupEntityClass.field("lookup-l2-dec").get(),
                            targetEntity.id())
                    ).addValue(
                        new LongValue(MockEntityClassDefine.lookupEntityClass.field("l2-lookup.id").get(),
                            targetEntity.id())
                    )
                ).build();
            lookupEntities.add(lookupEntity);

            result = entityManagementService.build(lookupEntity);
            Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        }

        Collection<IEntity> queryLookupEntities = entitySearchService.selectMultiple(
            lookupEntities.stream().mapToLong(e -> e.id()).toArray(), MockEntityClassDefine.lookupEntityClass.ref());
        Assertions.assertEquals(lookupEntities.size(), queryLookupEntities.size());
        // 验证是否成功lookup.
        queryLookupEntities.forEach(e -> {
            Assertions.assertEquals(
                targetEntity.entityValue().getValue("l2-dec").get().valueToString(),
                e.entityValue().getValue("lookup-l2-dec").get().valueToString());
        });

        IEntity newTargetEntity = Entity.Builder.anEntity()
            .withId(targetEntity.id())
            .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
            .withEntityValue(
                EntityValue.build().addValue(
                    new DecimalValue(MockEntityClassDefine.l2EntityClass.field("l2-dec").get(),
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
                MockEntityClassDefine.lookupEntityClass.ref());

            successSize =
                queryLookupEntities.stream()
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

        Collection<IEntity> conditionQueryEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.lookupEntityClass.field("lookup-l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(MockEntityClassDefine.lookupEntityClass.field("lookup-l2-dec").get(),
                            BigDecimal.valueOf(13.3333D)))
                ),
            MockEntityClassDefine.lookupEntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig()
                .withPage(Page.newSinglePage(200)).build()
        );

        Assertions.assertEquals(lookupSize, conditionQueryEntities.size());
        Assertions.assertEquals(lookupSize, conditionQueryEntities.stream().filter(
            e -> e.entityValue().getValue("lookup-l2-dec").get().valueToString().equals("13.3333")
        ).count());
    }
}
