package com.xforceplus.ultraman.oqsengine.storage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
     * 测试检测通过.
     */
    @Test
    public void testCheckOk() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 10);
        long minCommitId = 1000L;

        // 主库存
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(
            Arrays.asList(
                // 1个创建, 2个更新, 2个删除.
                buildUpdateRef(1),
                buildUpdateRef(2),
                buildCreateRef(3),
                buildDeleteRef(4),
                buildDeleteRef(5)
            ),

            // 第二次查询,空返回.
            Arrays.asList()
        );

        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize()))
                // 只过滤更新和删除的实例.
                .withExcludeIds(new long[] {1, 2, 4, 5})
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            // 已同步的数量查询总量是100.
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(9),
                buildCreateRef(10),
                buildCreateRef(11),
                buildCreateRef(12),
                buildCreateRef(13),
                buildCreateRef(14),
                buildCreateRef(15),
                buildCreateRef(16),
                buildCreateRef(17),
                buildCreateRef(18));
        });

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build());

        // 最终数量总量是已同步的100+主库中更新和创建的数量.
        Assertions.assertEquals(103, page.getTotalCount());
        long[] expectedIds = new long[] {
            1, 2, 3, 9, 10, 11, 12, 13, 14, 15
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());

        Mockito.verify(unSyncedStorage, times(2)).select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );
    }

    /**
     * 检测发现错误, 重试一次后成功.
     */
    @Test
    public void testCheckFailReplay() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 10);
        long minCommitId = 1000L;

        // 主库存
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(
            Arrays.asList(
                // 1个创建, 2个更新, 2个删除.
                buildUpdateRef(1),
                buildUpdateRef(2),
                buildCreateRef(3),
                buildDeleteRef(4),
                buildDeleteRef(5)
            ),

            // 第二次查询,是校验.失败.
            Arrays.asList(
                buildUpdateRef(9)
            ),

            Arrays.asList(
                // 1个创建, 2个更新, 2个删除.
                buildUpdateRef(1),
                buildUpdateRef(2),
                buildDeleteRef(40),
                buildDeleteRef(66)
            ),

            Arrays.asList()
        );

        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize()))
                // 只过滤更新和删除的实例.
                .withExcludeIds(new long[] {1, 2, 4, 5})
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            // 已同步的数量查询总量是100.
            config.getPage().setTotalCount(100);

            return Arrays.asList(
                buildCreateRef(9),
                buildCreateRef(10),
                buildCreateRef(11),
                buildCreateRef(12),
                buildCreateRef(13),
                buildCreateRef(14),
                buildCreateRef(15),
                buildCreateRef(16),
                buildCreateRef(17),
                buildCreateRef(18));
        });

        // 第二次查询
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, page.getPageSize()))
                // 只过滤更新和删除的实例.
                .withExcludeIds(new long[] {1, 2, 40, 66})
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenAnswer(invocation -> {
            SelectConfig config = invocation.getArgument(2, SelectConfig.class);
            // 3 创建成功并且同步了,所以已同步的数量从100变为101.
            config.getPage().setTotalCount(101);

            return Arrays.asList(
                buildCreateRef(3),
                buildCreateRef(9),
                buildCreateRef(10),
                buildCreateRef(11),
                buildCreateRef(12),
                buildCreateRef(13),
                buildCreateRef(14),
                buildCreateRef(15),
                buildCreateRef(16),
                buildCreateRef(17));
        });

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build());

        // 最终数量总量是已同步的100+主库中更新和创建的数量.
        Assertions.assertEquals(103, page.getTotalCount());
        long[] expectedIds = new long[] {
            1, 2, 3, 9, 10, 11, 12, 13, 14, 15
        };
        Assertions.assertArrayEquals(expectedIds, refs.stream().mapToLong(r -> r.getId()).toArray());
        Mockito.verify(unSyncedStorage, times(4)).select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );
    }

    @Test
    public void testIdSort() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions();
        Page page = new Page(1, 4);
        long minCommitId = 1000L;

        List<EntityRef> indexRefs = Arrays.asList(
            buildCreateRef(7),
            buildCreateRef(80),
            buildCreateRef(99)
        );
        ConditionsSelectStorage syncedStorage = mock(ConditionsSelectStorage.class);
        when(syncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(new Page(1, 4))
                .withExcludeIds(new long[] {3, 6})
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
            buildCreateRef(700)
        );
        ConditionsSelectStorage unSyncedStorage = mock(ConditionsSelectStorage.class);
        when(unSyncedStorage.select(
            conditions,
            mockEntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        )).thenReturn(masterRefs, Arrays.asList());

        CombinedSelectStorage combinedSelectStorage = new CombinedSelectStorage(unSyncedStorage, syncedStorage);
        Collection<EntityRef> refs = combinedSelectStorage.select(
            conditions, mockEntityClass, SelectConfig.Builder.anSelectConfig()
                .withCommitId(minCommitId)
                .withPage(page)
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(4, refs.size());
        long[] expectedIds = new long[] {
            6, 7, 80, 99
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