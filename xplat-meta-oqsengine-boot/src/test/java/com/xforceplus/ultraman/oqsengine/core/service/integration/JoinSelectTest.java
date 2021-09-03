package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
 * 关联查询集成测试.
 *
 * @author dongbin
 * @version 0.1 2020/4/28 16:27
 * @since 1.8
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JoinSelectTest extends AbstractContainerExtends {

    final Logger logger = LoggerFactory.getLogger(JoinSelectTest.class);

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource
    private EntityManagementService managementService;

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private TransactionManagementService transactionManagementService;

    @Resource
    private DataSourcePackage dataSourcePackage;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    private boolean initialization;

    private List<IEntity> entities;
    private List<IEntity> driverEntities;

    public JoinSelectTest() throws IllegalAccessException {
    }

    /**
     * 每个测试的初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        initialization = false;

        Mockito.when(metaManager.load(MockEntityClassDefine.l0EntityClass.id()))
            .thenReturn(Optional.of(MockEntityClassDefine.l0EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l0EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l0EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l0EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l0EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l1EntityClass.id()))
            .thenReturn(Optional.of(MockEntityClassDefine.l1EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l1EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l1EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l1EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l1EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l2EntityClass.id()))
            .thenReturn(Optional.of(MockEntityClassDefine.l2EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l2EntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.l2EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.l2EntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.l2EntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.driverEntityClass.id()))
            .thenReturn(Optional.of(MockEntityClassDefine.driverEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.driverEntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.driverEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.driverEntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.driverEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.lookupEntityClass.id()))
            .thenReturn(Optional.of(MockEntityClassDefine.lookupEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.lookupEntityClass.id(), null))
            .thenReturn(Optional.of(MockEntityClassDefine.lookupEntityClass));

        Mockito.when(metaManager.load(MockEntityClassDefine.lookupEntityClass.id(), OqsProfile.UN_DEFINE_PROFILE))
            .thenReturn(Optional.of(MockEntityClassDefine.lookupEntityClass));

        initData();

        initialization = true;
    }

    /**
     * 每个测试后的清理.
     */
    @AfterEach
    public void after() throws Exception {
        if (initialization) {
            clear();
        }

        initialization = false;
    }

    /**
     * 驱动实体为空,主实体有一条记录.
     * 应该返回空记录.
     */
    @Test
    public void testDriverEmpty() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    MockEntityClassDefine.driverEntityClass.ref(),
                    MockEntityClassDefine.driverEntityClass.field("driver-long").get(),
                    ConditionOperator.EQUALS,
                    3L,
                    new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), Long.MAX_VALUE)
                )
            )
            .addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v0")
                )
            );

        Page page = new Page(1, 100);
        Collection<IEntity> results =
            entitySearchService.selectByConditions(conditions, MockEntityClassDefine.l2EntityClass.ref(), page);
        Assertions.assertEquals(0, results.size());
        Assertions.assertEquals(0, page.getTotalCount());
    }

    @Test
    public void testJoinSearch() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    MockEntityClassDefine.driverEntityClass.ref(),
                    MockEntityClassDefine.driverEntityClass.field("driver-long").get(),
                    ConditionOperator.EQUALS,
                    3L,
                    new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 1L)
                )
            )
            .addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v0")
                )
            );

        Page page = Page.newSinglePage(100);
        Collection<IEntity> results =
            entitySearchService.selectByConditions(conditions, MockEntityClassDefine.l2EntityClass.ref(),
                ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1, page.getTotalCount());

        conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    MockEntityClassDefine.driverEntityClass.ref(),
                    MockEntityClassDefine.driverEntityClass.field("driver-long").get(),
                    ConditionOperator.EQUALS,
                    3L,
                    new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 2L)
                )
            )
            .addAnd(
                new Condition(
                    MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v1")
                )
            );

        page = Page.newSinglePage(100);
        results =
            entitySearchService.selectByConditions(conditions, MockEntityClassDefine.l2EntityClass.ref(), page);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1, page.getTotalCount());
    }

    /**
     * 测试驱动实例超出设定上限.
     */
    @Test
    public void testDriverInstanceLimitExceeded() throws Exception {
        Assertions.assertThrows(SQLException.class, () -> {
            Conditions conditions = Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.driverEntityClass.ref(),
                        MockEntityClassDefine.driverEntityClass.field("driver-long").get(),
                        ConditionOperator.EQUALS,
                        3L,
                        new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 100L)
                    )
                )
                .addAnd(
                    new Condition(
                        MockEntityClassDefine.l2EntityClass.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v3")
                    )
                );

            Page page = Page.newSinglePage(100);
            entitySearchService.selectByConditions(conditions, MockEntityClassDefine.l2EntityClass.ref(), page);
        });

    }

    private void initData() throws SQLException {

        long txid = transactionManagementService.begin(300000);
        /*
         * 总共会有1003个驱动实例.
         * 除了最开始的2个,剩余的1001个会被一起命中.
         */
        driverEntities = new ArrayList<>(Arrays.asList(
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.driverEntityClass.ref())
                .withMajor(OqsVersion.MAJOR)
                .withEntityValue(EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 1L)
                    )
                )).build(),
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.driverEntityClass.ref())
                .withMajor(OqsVersion.MAJOR)
                .withEntityValue(EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 2L)
                    )
                )).build()
        ));

        // 1001个上驱动上限.
        for (int i = 0; i < 1001; i++) {
            driverEntities.add(
                Entity.Builder.anEntity()
                    .withEntityClassRef(MockEntityClassDefine.driverEntityClass.ref())
                    .withMajor(OqsVersion.MAJOR)
                    .withEntityValue(EntityValue.build().addValues(
                        Arrays.asList(
                            new LongValue(MockEntityClassDefine.driverEntityClass.field("driver-long").get(), 100L)
                        )
                    )).build()
            );
        }

        buildEntities(driverEntities, txid);

        /*
         * 两个实例,分别和驱动实例中的第一个和第二个关联.
         */
        entities = new ArrayList<>(Arrays.asList(
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
                .withMajor(OqsVersion.MAJOR)
                .withEntityValue(
                    EntityValue.build()
                        .addValue(
                            new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v0")
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l1-long").get(), 1000L)
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l2-driver.id").get(),
                                driverEntities.get(0).id())
                        )
                ).build(),
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
                .withMajor(OqsVersion.MAJOR)
                .withEntityValue(
                    EntityValue.build()
                        .addValue(
                            new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v1")
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l1-long").get(), 2000L)
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l2-driver.id").get(),
                                driverEntities.get(1).id())
                        )
                ).build()
        ));

        /*
         * 除第0个和第1个以外的所有驱动实例关联.
         */
        entities.add(
            Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.l2EntityClass.ref())
                .withMajor(OqsVersion.MAJOR)
                .withEntityValue(
                    EntityValue.build()
                        .addValue(
                            new StringValue(MockEntityClassDefine.l2EntityClass.field("l2-string").get(), "v3")
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l1-long").get(), 3000L)
                        )
                        .addValue(
                            new LongValue(MockEntityClassDefine.l2EntityClass.field("l2-driver.id").get(),
                                driverEntities.get(3).id())
                        )
                ).build()
        );

        buildEntities(entities, txid);
        transactionManagementService.restore(txid);
        transactionManagementService.commit();
    }

    private void clear() throws Exception {
        while (commitIdStatusService.size() > 0) {
            logger.info("Wait for CDC synchronization to complete.");
            TimeUnit.MILLISECONDS.sleep(10);
        }

        for (DataSource ds : dataSourcePackage.getMaster()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("truncate table oqsbigentity");
                }
            }
        }

        for (DataSource ds : dataSourcePackage.getIndexWriter()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("truncate table oqsindex0");
                    st.executeUpdate("truncate table oqsindex1");
                }
            }
        }
    }

    private void buildEntities(List<IEntity> entities, long txId) throws SQLException {
        for (IEntity e : entities) {
            transactionManagementService.restore(txId);
            managementService.build(e);
        }
    }
}
