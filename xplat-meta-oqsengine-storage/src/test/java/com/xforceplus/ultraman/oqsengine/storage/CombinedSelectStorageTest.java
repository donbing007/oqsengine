package com.xforceplus.ultraman.oqsengine.storage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 联合查询实现测试.
 *
 * @author dongbin
 * @version 0.1 2022/9/27 09:57
 * @since 1.8
 */
public class CombinedSelectStorageTest {

    private static final IEntityClass mockEntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withCode("mock")
        .build();

    /**
     * 预期溢出失败异常.
     */
    @Test
    public void testOverflowFail() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = Page.newSinglePage(3);
        long minCommitId = 1000L;

        // 第一次查询
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.1F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3));
        });

        // 第二次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.3F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3),
                buildCreateRef(4));
        });

        // 第三次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.5F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3),
                buildCreateRef(4),
                buildCreateRef(5));
        });

        // 第四次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.8F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3),
                buildCreateRef(4),
                buildCreateRef(5)
            );
        });

        // 第五次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 1.0F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3),
                buildCreateRef(4),
                buildCreateRef(5),
                buildCreateRef(6)
            );
        });

        // 全部标记被删除.
        List<EntityRef> masterRefs = Arrays.asList(
            buildDeleteRef(1),
            buildDeleteRef(2),
            buildDeleteRef(3),
            buildDeleteRef(4),
            buildDeleteRef(5)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs);

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Assertions.assertThrows(SQLException.class, () -> combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, 3))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        ));
    }


    /**
     * 以10%的比例查询成功.
     */
    @Test
    public void testNoOverFlow() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 3);
        long minCommitId = 1000L;

        List<EntityRef> indexRefs = Arrays.asList(
            buildCreateRef(1),
            buildCreateRef(7),
            buildCreateRef(8)
        );
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.1F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(3);
            return indexRefs;
        });

        List<EntityRef> masterRefs = Arrays.asList(
            buildDeleteRef(3),
            buildUpdateRef(2),
            buildCreateRef(6)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs);

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(3, refs.size());
        long[] expectedIds = new long[] {
            1L, 2L, 6L
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());
        Assertions.assertEquals(5, page.getTotalCount());
    }

    /**
     * 数量不足够当前页,但又没有更多数据.
     */
    @Test
    public void testOverflowNoMoreEntity() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 30);
        long minCommitId = 1000L;

        AtomicInteger indexSelectTime = new AtomicInteger();
        // 只会查询一次.
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.1F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(3);

            indexSelectTime.getAndAdd(1);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(4),
                buildCreateRef(5));
        });

        List<EntityRef> masterRefs = Arrays.asList(
            buildDeleteRef(2),
            buildDeleteRef(3)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs);


        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(3, refs.size());
        long[] expectedIds = new long[] {
            1L, 4L, 5L,
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());
        Assertions.assertEquals(3, page.getTotalCount());
    }

    /**
     * 数据溢出,直到比例到50%才满足.
     */
    @Test
    public void testOverflow() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 3);
        long minCommitId = 1000L;

        AtomicInteger indexSelectTime = new AtomicInteger();
        // 第一次查询
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.1F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(5);

            indexSelectTime.getAndAdd(1);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3));
        });

        // 第二次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize() + (long) (page.getPageSize() * 0.3F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(5);

            indexSelectTime.getAndAdd(1);

            return Arrays.asList(
                buildCreateRef(1),
                buildCreateRef(2),
                buildCreateRef(3),
                buildCreateRef(4));
        });

        List<EntityRef> masterRefs = Arrays.asList(
            buildDeleteRef(6),
            buildDeleteRef(7)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs);

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(2, indexSelectTime.get());
        Assertions.assertEquals(3, refs.size());
        long[] expectedIds = new long[] {
            1L, 2L, 3L
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());
        Assertions.assertEquals(5, page.getTotalCount());
    }

    @Test
    public void testIdSort() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = Page.newSinglePage(3);
        long minCommitId = 1000L;

        List<EntityRef> indexRefs = Arrays.asList(
            buildCreateRef(3),
            buildCreateRef(1),
            buildCreateRef(2)
        );
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(Page.newSinglePage(page.getPageSize() + (long) (page.getPageSize() * 0.1F)))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            config.getPage().setTotalCount(3);
            return indexRefs;
        });

        List<EntityRef> masterRefs = Arrays.asList(
            buildDeleteRef(3),
            buildUpdateRef(6),
            buildCreateRef(7)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs);

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(Page.newSinglePage(3))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(3, refs.size());
        long[] expectedIds = new long[] {
            1L, 2L, 6L
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());
    }

    private static EntityRef buildCreateRef(long id) {
        return buildEntityRef(id, OperationType.CREATE);
    }

    private static EntityRef buildUpdateRef(long id) {
        return buildEntityRef(id, OperationType.UPDATE);
    }

    private static EntityRef buildDeleteRef(long id) {
        return buildEntityRef(id, OperationType.DELETE);
    }

    private static EntityRef buildEntityRef(long id, OperationType type) {
        return EntityRef.Builder.anEntityRef().withId(id).withOp(type.getValue()).build();
    }
}