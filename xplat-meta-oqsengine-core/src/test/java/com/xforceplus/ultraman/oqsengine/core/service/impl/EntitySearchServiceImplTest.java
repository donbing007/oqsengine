package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.impl.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.SearchConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EntitySearchServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/18/2021
 * @since <pre>Mar 18, 2021</pre>
 */
public class EntitySearchServiceImplTest {

    private CommitIdStatusService commitIdStatusService;
    private MasterStorage masterStorage;
    private IndexStorage indexStorage;
    private ExecutorService threadPool;
    private MetaManager metaManager;
    private EntitySearchServiceImpl impl;

    @Before
    public void before() throws Exception {
        commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.getMin()).thenReturn((Optional.of(Long.valueOf("1"))));

        threadPool = Executors.newFixedThreadPool(3);
        metaManager = new MockMetaManager();

        masterStorage = mock(MasterStorage.class);
        indexStorage = mock(IndexStorage.class);

        impl = new EntitySearchServiceImpl();
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(impl, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(impl, "threadPool", threadPool);
        ReflectionTestUtils.setField(impl, "commitIdStatusService", commitIdStatusService);
        impl.init();
    }

    @After
    public void after() throws Exception {
        ExecutorHelper.shutdownAndAwaitTermination(threadPool);
    }

    @Test
    public void testCountSearch() throws Exception {

        when(masterStorage.select(
            Conditions.buildEmtpyConditions(),
            MockMetaManager.l1EntityClass,
            SelectConfig.Builder.aSelectConfig().withCommitId(1).withSort(
                Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build()))
            .thenReturn(Arrays.asList(
                new EntityRef(1, OperationType.CREATE.getValue(), OqsVersion.MAJOR),
                new EntityRef(1, OperationType.CREATE.getValue(), OqsVersion.MAJOR)
            ));

        Page indexPage = Page.emptyPage();
        when(indexStorage.select(
            Conditions.buildEmtpyConditions(),
            MockMetaManager.l1EntityClass,
            SelectConfig.Builder.aSelectConfig()
                .withCommitId(1)
                .withPage(indexPage)
                .withExcludedIds(new HashSet())
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer((invocation) -> {
            SelectConfig selectConfig = invocation.getArgument(2, SelectConfig.class);
            selectConfig.getPage().setTotalCount(0);
            return Collections.emptyList();
        });

        Page page = Page.emptyPage();
        Collection<IEntity> entities = impl.selectByConditions(
            Conditions.buildEmtpyConditions(),
            EntityClassRef.Builder.anEntityClassRef()
                .withEntityClassId(MockMetaManager.l1EntityClass.id())
                .withEntityClassCode(MockMetaManager.l1EntityClass.code())
                .build(),
            SearchConfig.Builder.aSearchConfig().withPage(page).build()
        );

        Assert.assertEquals(0, entities.size());
        Assert.assertEquals(2, page.getTotalCount());
    }

    /**
     * 测试主库查询有需要过滤.
     */
    @Test
    public void testFilter() throws Exception {
        when(masterStorage.select(
            Mockito.argThat(conditions -> {
                boolean result = conditions.size() == 0;
                return result;
            }),
            Mockito.argThat(entityClass -> {
                boolean result = entityClass.id() == MockMetaManager.l2EntityClass.id();
                return result;
            }),
            Mockito.argThat(selectConfig -> {
                boolean result = selectConfig.equals(
                    SelectConfig.Builder.aSelectConfig()
                        .withCommitId(1)
                        .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                        .build());
                return result;
            })

        )).thenReturn(Arrays.asList(
            new EntityRef(1, OperationType.CREATE.getValue(), OqsVersion.MAJOR),
            new EntityRef(2, OperationType.CREATE.getValue(), OqsVersion.MAJOR),
            new EntityRef(3, OperationType.DELETE.getValue(), OqsVersion.MAJOR),
            new EntityRef(4, OperationType.UPDATE.getValue(), OqsVersion.MAJOR)
        ));
        when(masterStorage.selectMultiple(new long[]{1, 2, 4}, MockMetaManager.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(2).build(),
                Entity.Builder.anEntity().withId(4).build()
            )
        );

        Page page = Page.newSinglePage(1000);
        when(indexStorage.select(
            Conditions.buildEmtpyConditions(),
            MockMetaManager.l2EntityClass,
            SelectConfig.Builder.aSelectConfig()
                .withCommitId(1)
                .withPage(page)
                .withExcludedIds(new HashSet())
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .withExcludeId(3)
                .withExcludeId(4)
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig selectConfig = invocation.getArgument(2, SelectConfig.class);
            selectConfig.getPage().setTotalCount(0);
            return Collections.emptyList();
        });


        Collection<IEntity> entities = impl.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockMetaManager.l2EntityClass.ref(),
            SearchConfig.Builder.aSearchConfig().withPage(page).build()
        );

        Assert.assertEquals(3, entities.size());
        Assert.assertEquals(3, page.getTotalCount());
    }

    @Test
    public void testSelectOne() throws Exception {
        when(masterStorage.selectOne(1, MockMetaManager.l0EntityClass)).thenReturn(
            Optional.of(Entity.Builder.anEntity().withId(1).build())
        );
        Optional<IEntity> entityOp = impl.selectOne(1, MockMetaManager.l0EntityClass.ref());

        Assert.assertTrue(entityOp.isPresent());
        Assert.assertEquals(1, entityOp.get().id());
    }

    @Test
    public void testSelectMultiple() throws Exception {
        long[] ids = new long[]{
            1, 2, 3
        };
        when(masterStorage.selectMultiple(ids, MockMetaManager.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(2).build(),
                Entity.Builder.anEntity().withId(3).build()
            )
        );

        Collection<IEntity> entities = impl.selectMultiple(ids, MockMetaManager.l2EntityClass.ref());

        Assert.assertEquals(ids.length, entities.size());
        List<IEntity> entityList = new ArrayList<>(entities);
        for (int i = 0; i < ids.length; i++) {
            Assert.assertEquals(ids[i], entityList.get(i).id());
        }
    }

    @Test
    public void testSelectByOndIdCondition() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(EntityField.ID_ENTITY_FIELD, ConditionOperator.EQUALS,
                    new LongValue(EntityField.ID_ENTITY_FIELD, 100L)));

        when(masterStorage.selectOne(100L, MockMetaManager.l2EntityClass)).thenReturn(Optional.of(
            Entity.Builder.anEntity().withId(100L).build()
        ));

        Collection<IEntity> entities = impl.selectByConditions(conditions,
            MockMetaManager.l2EntityClass.ref(),
            SearchConfig.Builder.aSearchConfig().withPage(Page.newSinglePage(1000)).build()
        );

        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(100L, entities.stream().findFirst().get().id());
    }

    @Test
    public void testSelectNormalFieldCondition() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    MockMetaManager.l0EntityClass.field("l0-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(MockMetaManager.l0EntityClass.field("l0-string").get(), "test")
                )
            );
        Page page = Page.newSinglePage(100);
        when(masterStorage.select(
            conditions,
            MockMetaManager.l2EntityClass,
            SelectConfig.Builder.aSelectConfig()
                .withCommitId(1)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(Arrays.asList(
            new EntityRef(1, OperationType.CREATE.getValue(), OqsVersion.MAJOR),
            new EntityRef(2, OperationType.DELETE.getValue(), OqsVersion.MAJOR)
        ));

        when(indexStorage.select(
            conditions,
            MockMetaManager.l2EntityClass,
            SelectConfig.Builder.aSelectConfig()
                .withCommitId(1)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .withExcludeId(2L)
                .withPage(page)
                .build()
        )).thenReturn(
            Arrays.asList(
                new EntityRef(3, OperationType.CREATE.getValue(), OqsVersion.MAJOR),
                new EntityRef(4, OperationType.CREATE.getValue(), OqsVersion.MAJOR)
            )
        );

        when(masterStorage.selectMultiple(new long[]{1, 3, 4}, MockMetaManager.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(3).build(),
                Entity.Builder.anEntity().withId(4).build()
            )
        );

        List<IEntity> entities = new ArrayList<>(impl.selectByConditions(
            conditions,
            MockMetaManager.l2EntityClass.ref(),
            SearchConfig.Builder.aSearchConfig().withPage(page).build()
        ));

        Assert.assertEquals(3, entities.size());
        long[] expectedIds = new long[]{
            1, 3, 4
        };
        for (int i = 0; i < expectedIds.length; i++) {
            Assert.assertEquals(expectedIds[i], entities.get(i).id());
        }
    }

    @Test(expected = SQLException.class)
    public void testHaveFuzzyFilterConditions() throws Exception {
        impl.selectByConditions(
            Conditions.buildEmtpyConditions(),
            MockMetaManager.l2EntityClass.ref(),
            SearchConfig.Builder.aSearchConfig()
                .withFilter(
                    Conditions.buildEmtpyConditions()
                        .addAnd(
                            new Condition(
                                MockMetaManager.l2EntityClass.field("l0-string").get(),
                                ConditionOperator.LIKE,
                                new StringValue(MockMetaManager.l2EntityClass.field("l0-string").get(), "123")
                            )
                        )
                ).build()
        );
    }
}
