package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.impl.mock.EntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
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
import com.xforceplus.ultraman.oqsengine.storage.CombinedSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EntitySearchServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 03/18/2021
 * @since <pre>Mar 18, 2021</pre>
 */
public class EntitySearchServiceImplTest {

    private CommitIdStatusService commitIdStatusService;
    private MasterStorage masterStorage;
    private IndexStorage indexStorage;
    private CombinedSelectStorage combinedSelectStorage;
    private ExecutorService threadPool;
    private MetaManager metaManager;
    private EntitySearchServiceImpl impl;

    /**
     * 每个测试初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.getMin()).thenReturn((Optional.of(Long.valueOf("1"))));

        threadPool = Executors.newFixedThreadPool(3);
        metaManager = EntityClassDefine.getMockMetaManager();

        masterStorage = mock(MasterStorage.class);
        indexStorage = mock(IndexStorage.class);
        combinedSelectStorage = new CombinedSelectStorage(masterStorage, indexStorage);
        combinedSelectStorage.setCommitIdStatusService(commitIdStatusService);

        impl = new EntitySearchServiceImpl();
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(impl, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(impl, "combinedSelectStorage", combinedSelectStorage);
        ReflectionTestUtils.setField(impl, "threadPool", threadPool);
        ReflectionTestUtils.setField(impl, "commitIdStatusService", commitIdStatusService);
        impl.init();
    }

    @AfterEach
    public void after() throws Exception {
        ExecutorHelper.shutdownAndAwaitTermination(threadPool);
    }

    @Test
    public void testCountSearch() throws Exception {

        when(masterStorage.select(
            Conditions.buildEmtpyConditions(),
            EntityClassDefine.l1EntityClass,
            SelectConfig.Builder.anSelectConfig().withCommitId(1).withSort(
                Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build()))
            .thenReturn(Arrays.asList(
                EntityRef.Builder.anEntityRef()
                    .withId(1)
                    .withOp(OperationType.CREATE.getValue())
                    .withMajor(OqsVersion.MAJOR).build(),
                EntityRef.Builder.anEntityRef()
                    .withId(2)
                    .withOp(OperationType.CREATE.getValue())
                    .withMajor(OqsVersion.MAJOR).build()
            ));

        Page indexPage = Page.emptyPage();
        Set<Long> excludeIds = new HashSet<>();
        when(indexStorage.select(
            Conditions.buildEmtpyConditions(),
            EntityClassDefine.l1EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(1)
                .withPage(indexPage)
                .withExcludedIds(excludeIds)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer((invocation) -> {
            SelectConfig selectConfig = invocation.getArgument(2, SelectConfig.class);
            selectConfig.getPage().setTotalCount(0);
            return Collections.emptyList();
        });

        Page page = Page.emptyPage();
        OqsResult<Collection<IEntity>> entities = impl.selectByConditions(
            Conditions.buildEmtpyConditions(),
            EntityClassRef.Builder.anEntityClassRef()
                .withEntityClassId(EntityClassDefine.l1EntityClass.id())
                .withEntityClassCode(EntityClassDefine.l1EntityClass.code())
                .build(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build()
        );

        Assertions.assertTrue(entities.isSuccess());
        Assertions.assertEquals(0, entities.getValue().get().size());
        Assertions.assertEquals(2, page.getTotalCount());
    }

    /**
     * 测试主库查询有需要过滤.
     */
    @Test
    public void testFilter() throws Exception {
        when(masterStorage.select(
                Conditions.buildEmtpyConditions(),
                EntityClassDefine.l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                    .withCommitId(1)
                    .build()
            )
        ).thenReturn(Arrays.asList(
            EntityRef.Builder.anEntityRef()
                .withId(1)
                .withOp(OperationType.CREATE.getValue())
                .withMajor(OqsVersion.MAJOR).build(),
            EntityRef.Builder.anEntityRef()
                .withId(2)
                .withOp(OperationType.CREATE.getValue())
                .withMajor(OqsVersion.MAJOR).build(),
            EntityRef.Builder.anEntityRef()
                .withId(3)
                .withOp(OperationType.CREATE.getValue())
                .withMajor(OqsVersion.MAJOR).build(),
            EntityRef.Builder.anEntityRef()
                .withId(4)
                .withOp(OperationType.DELETE.getValue())
                .withMajor(OqsVersion.MAJOR).build()
        ));
        when(masterStorage.selectMultiple(new long[] {1, 2, 3}, EntityClassDefine.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(2).build(),
                Entity.Builder.anEntity().withId(3).build()
            )
        );

        Page indexPage = Page.newSinglePage(100);
        indexPage.setTotalCount(0);
        when(indexStorage.select(
            Conditions.buildEmtpyConditions(),
            EntityClassDefine.l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(1)
                .withPage(indexPage)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .withExcludeId(4)
                .build()
        )).thenReturn(Collections.emptyList());


        Page page = Page.newSinglePage(100);
        OqsResult<Collection<IEntity>> entities = impl.selectByConditions(
            Conditions.buildEmtpyConditions(),
            EntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build()
        );

        Assertions.assertEquals(3, entities.getValue().get().size());
        // 总量会多出100,是由于index没有真的查询数据.
        Assertions.assertEquals(3 + 100, page.getTotalCount());
    }

    @Test
    public void testSelectOne() throws Exception {
        when(masterStorage.selectOne(1, EntityClassDefine.l0EntityClass)).thenReturn(
            Optional.of(Entity.Builder.anEntity().withId(1).build())
        );
        OqsResult<IEntity> entityOp = impl.selectOne(1, EntityClassDefine.l0EntityClass.ref());

        Assertions.assertTrue(entityOp.getValue().isPresent());
        Assertions.assertEquals(1, entityOp.getValue().get().id());
    }

    @Test
    public void testSelectMultiple() throws Exception {
        long[] ids = new long[] {
            1, 2, 3
        };
        when(masterStorage.selectMultiple(ids, EntityClassDefine.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(2).build(),
                Entity.Builder.anEntity().withId(3).build()
            )
        );

        OqsResult<Collection<IEntity>> entities = impl.selectMultiple(ids, EntityClassDefine.l2EntityClass.ref());

        Assertions.assertEquals(ids.length, entities.getValue().get().size());
        List<IEntity> entityList = new ArrayList(entities.getValue().get());
        for (int i = 0; i < ids.length; i++) {
            Assertions.assertEquals(ids[i], entityList.get(i).id());
        }
    }

    @Test
    public void testSelectByOndIdCondition() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(EntityField.ID_ENTITY_FIELD, ConditionOperator.EQUALS,
                    new LongValue(EntityField.ID_ENTITY_FIELD, 100L)));

        when(masterStorage.selectOne(100L, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(
            Entity.Builder.anEntity().withId(100L).build()
        ));

        OqsResult<Collection<IEntity>> entities = impl.selectByConditions(conditions,
            EntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.newSinglePage(1000)).build()
        );

        Assertions.assertEquals(1, entities.getValue().get().size());
        Assertions.assertEquals(100L, entities.getValue().get().stream().findFirst().get().id());
    }

    @Test
    public void testSelectNormalFieldCondition() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    EntityClassDefine.l0EntityClass.field("l0-string").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(EntityClassDefine.l0EntityClass.field("l0-string").get(), "test")
                )
            );
        Page page = Page.newSinglePage(100);
        when(masterStorage.select(
            conditions,
            EntityClassDefine.l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(1)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(Arrays.asList(
            EntityRef.Builder.anEntityRef()
                .withId(1)
                .withOp(OperationType.CREATE.getValue())
                .withMajor(OqsVersion.MAJOR).build(),
            EntityRef.Builder.anEntityRef()
                .withId(2)
                .withOp(OperationType.CREATE.getValue())
                .withMajor(OqsVersion.MAJOR).build()
        ));

        // 索引需要排除的.
        Set<Long> excludeIds = new HashSet<>();
        when(indexStorage.select(
            conditions,
            EntityClassDefine.l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(1)
                .withExcludedIds(excludeIds)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .withPage(page)
                .build()
        )).thenReturn(
            Arrays.asList(
                EntityRef.Builder.anEntityRef()
                    .withId(3)
                    .withOp(OperationType.CREATE.getValue())
                    .withMajor(OqsVersion.MAJOR).build(),
                EntityRef.Builder.anEntityRef()
                    .withId(4)
                    .withOp(OperationType.CREATE.getValue())
                    .withMajor(OqsVersion.MAJOR).build()
            )
        );

        when(masterStorage.selectMultiple(new long[] {1, 2, 3, 4}, EntityClassDefine.l2EntityClass)).thenReturn(
            Arrays.asList(
                Entity.Builder.anEntity().withId(1).build(),
                Entity.Builder.anEntity().withId(2).build(),
                Entity.Builder.anEntity().withId(3).build(),
                Entity.Builder.anEntity().withId(4).build()
            )
        );

        List<IEntity> entities = new ArrayList(impl.selectByConditions(
            conditions,
            EntityClassDefine.l2EntityClass.ref(),
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build()
        ).getValue().get());

        Assertions.assertEquals(4, entities.size());
        long[] expectedIds = new long[] {
            1, 2, 3, 4
        };
        for (int i = 0; i < expectedIds.length; i++) {
            Assertions.assertEquals(expectedIds[i], entities.get(i).id());
        }
    }

    @Test
    public void testHaveFuzzyFilterConditions() throws Exception {
        assertThrows(SQLException.class, () -> {
            impl.selectByConditions(
                Conditions.buildEmtpyConditions(),
                EntityClassDefine.l2EntityClass.ref(),
                ServiceSelectConfig.Builder.anSearchConfig()
                    .withFilter(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    EntityClassDefine.l2EntityClass.field("l0-string").get(),
                                    ConditionOperator.LIKE,
                                    new StringValue(EntityClassDefine.l2EntityClass.field("l0-string").get(), "123")
                                )
                            )
                    ).build()
            );
        });
    }
}
