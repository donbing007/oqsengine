package com.xforceplus.ultraman.oqsengine.metadata.cache;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 测试.
 *
 * @author xujia 2021/2/16
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class CacheExecutorTest {

    private CacheExecutor cacheExecutor = MetaInitialization.getInstance().getCacheExecutor();

    public CacheExecutorTest() throws IllegalAccessException {
    }

    @BeforeEach
    public void before() throws Exception {
        cacheExecutor = MetaInitialization.getInstance().getCacheExecutor();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void prepare9to13Test() {
        /*
         * 设置prepare appId = 1 version = 9，预期返回true
         */
        boolean ret = cacheExecutor.prepare("1", 9);
        Assertions.assertTrue(ret);

        /*
         * 重置当前的版本version = 9，预期返回true
         */
        ret = cacheExecutor.resetVersion("1", 9, null);
        Assertions.assertTrue(ret);

        /*
         * 结束当前的appId prepare，预期返回true
         */
        ret = cacheExecutor.endPrepare("1");
        Assertions.assertTrue(ret);

        /*
         * 设置prepare appId = 1 version = 13，预期返回true
         */
        ret = cacheExecutor.prepare("1", 13);
        Assertions.assertTrue(ret);

        /*
         * 重置当前的版本version = 13，预期返回true
         */
        ret = cacheExecutor.resetVersion("1", 13, null);
        Assertions.assertTrue(ret);

        /*
         * 结束当前的appId prepare，预期返回true
         */
        ret = cacheExecutor.endPrepare("1");
        Assertions.assertTrue(ret);

        /*
         * 设置prepare appId = 1 version = 9，预期返回true
         */
        ret = cacheExecutor.prepare("1", 9);
        Assertions.assertFalse(ret);


        /*
         * 设置prepare appId = 1 version = 13，预期返回true
         */
        ret = cacheExecutor.prepare("1", 101);
        Assertions.assertTrue(ret);

        /*
         * 重置当前的版本version = 13，预期返回true
         */
        ret = cacheExecutor.resetVersion("1", 101, null);
        Assertions.assertTrue(ret);

        /*
         * 结束当前的appId prepare，预期返回true
         */
        ret = cacheExecutor.endPrepare("1");
        Assertions.assertTrue(ret);
    }

    @Test
    public void prepareTest() throws InterruptedException {
        /*
         * 设置prepare appId = 1 version = 1，预期返回true
         */
        boolean ret = cacheExecutor.prepare("1", 1);
        Assertions.assertTrue(ret);

        /*
         * 当前appId = 1 已经被锁定，重复发起会被拒绝，预期返回false
         */
        ret = cacheExecutor.prepare("1", 2);
        Assertions.assertFalse(ret);

        /*
         * 结束当前的appId prepare，预期返回true
         */
        ret = cacheExecutor.endPrepare("1");
        Assertions.assertTrue(ret);

        /*
         * 结束一个不存在的(appId = 2)prepare，预期返回false
         */
        ret = cacheExecutor.endPrepare("2");
        Assertions.assertFalse(ret);

        /*
         * 重置当前的版本version = 2，预期返回true
         */
        ret = cacheExecutor.resetVersion("1", 2, null);
        Assertions.assertTrue(ret);

        /*
         * 更新版本小于当前活动版本,拒绝，预期返回false
         */
        ret = cacheExecutor.prepare("1", 1);
        Assertions.assertFalse(ret);

        /*
         * 更新版本等于当前活动版本,拒绝，预期返回false
         */
        ret = cacheExecutor.prepare("1", 2);
        Assertions.assertFalse(ret);

        /*
         * 更新版本大于当前活动版本,接收，预期返回true
         */
        ret = cacheExecutor.prepare("1", 3);
        Assertions.assertTrue(ret);

        /*
         * 等待时间未到达指定时常，将被拒绝
         */
        Thread.sleep(30_000);
        ret = cacheExecutor.prepare("1", 4);
        Assertions.assertFalse(ret);

        /*
         * 等待时间超过expired time，将被接收
         */
        Thread.sleep(31_000);
        ret = cacheExecutor.prepare("1", 4);
        Assertions.assertTrue(ret);

        /*
         * 结束当前prepare
         */
        ret = cacheExecutor.endPrepare("1");
        Assertions.assertTrue(ret);
    }

    @Test
    public void versionTest() {
        /*
         * 当前的版本version = 2，预期返回true
         */
        String appId = "testResetVersion";
        int expectedVersion = 2;
        List<Long> expectedIds = Arrays.asList(1L, 2L);

        boolean ret = cacheExecutor.resetVersion(appId, expectedVersion, expectedIds);
        Assertions.assertTrue(ret);

        Assertions.assertEquals(expectedVersion, cacheExecutor.version(appId));

        Assertions.assertEquals(expectedVersion, cacheExecutor.version(expectedIds.get(0)));

        Assertions.assertEquals(expectedVersion, cacheExecutor.version(expectedIds.get(1)));
        /*
         * 使用一个未关联的entityId 3 进行版本信息查询，将返回NOT_EXIST_VERSION
         */
        Assertions.assertEquals(NOT_EXIST_VERSION, cacheExecutor.version(3L));
    }

    @Test
    public void entityClassStorageQueryTest() throws JsonProcessingException {

        List<EntityClassStorage> entityClassStorageList = new ArrayList<>();
        List<ExpectedEntityStorage> expectedEntityStorageList = new ArrayList<>();

        initEntityStorage(entityClassStorageList, expectedEntityStorageList);

        String expectedAppId = "testEntityQuery";
        int expectedVersion = Integer.MAX_VALUE;

        //  set storage
        if (!cacheExecutor.save(expectedAppId, expectedVersion, entityClassStorageList, new ArrayList<>())) {
            throw new RuntimeException("save error.");
        }

        check(expectedVersion, expectedEntityStorageList, entityClassStorageList);

        invalid(Long.MAX_VALUE - 1, String.format("invalid entityClassId : [%s], no version pair", Long.MAX_VALUE - 1));
    }

    @Test
    public void cleanTest() throws JsonProcessingException {
        List<EntityClassStorage> entityClassStorageList = new ArrayList<>();
        List<ExpectedEntityStorage> expectedEntityStorageList = new ArrayList<>();

        initEntityStorage(entityClassStorageList, expectedEntityStorageList);

        String expectedAppId = "testCleanEntity";
        int expectedVersion = Integer.MAX_VALUE - 1;

        //  set storage
        if (!cacheExecutor.save(expectedAppId, expectedVersion, entityClassStorageList, new ArrayList<>())) {
            throw new RuntimeException("save error.");
        }

        check(expectedVersion, expectedEntityStorageList, null);

        boolean ret = cacheExecutor.clean(expectedAppId, expectedVersion, true);
        Assertions.assertTrue(ret);

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            invalid(e.getSelf(), "entityClassStorage is null, may be delete.");
        }
    }

    private void invalid(Long id, String message) {
        try {
            cacheExecutor.read(id);
        } catch (Exception e) {
            Assertions.assertEquals(message, e.getMessage());
        }
    }

    /**
     * test & check.
     */
    private void check(int expectedVersion,
                       List<ExpectedEntityStorage> expectedEntityStorageList,
                       List<EntityClassStorage> entityClassStorageList) throws JsonProcessingException {
        Map<Long, EntityClassStorage> fullCheckMaps = null;
        if (null != entityClassStorageList && entityClassStorageList.size() > 0) {
            fullCheckMaps = entityClassStorageList.stream()
                .collect(Collectors.toMap(EntityClassStorage::getId, f1 -> f1, (f1, f2) -> f1));
        }

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            Assertions.assertEquals(expectedVersion, cacheExecutor.version(e.getSelf()));
            Map<Long, EntityClassStorage> results = cacheExecutor.read(e.getSelf());

            Assertions.assertNotNull(results.remove(e.getSelf()));

            if (null != e.getAncestors()) {
                for (Long id : e.getAncestors()) {
                    EntityClassStorage r = results.remove(id);
                    Assertions.assertNotNull(r);

                    if (null != fullCheckMaps) {
                        checkEntity(fullCheckMaps.get(id), r);
                    }
                }
            }

            Assertions.assertEquals(0, results.size());
        }
    }

    private void checkEntity(EntityClassStorage expected, EntityClassStorage actual) {
        //  basic
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getCode(), actual.getCode());
        Assertions.assertEquals(expected.getVersion(), actual.getVersion());
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getFatherId(), actual.getFatherId());
        Assertions.assertEquals(expected.getLevel(), actual.getLevel());

        //  ancestors
        if (null != expected.getAncestors()) {
            Assertions.assertNotNull(actual.getAncestors());
            Assertions.assertEquals(expected.getAncestors().size(), actual.getAncestors().size());
            for (int i = 0; i < expected.getAncestors().size(); i++) {
                Assertions.assertEquals(expected.getAncestors().get(i), actual.getAncestors().get(i));
            }
        }

        //  relations
        if (null != expected.getRelations()) {
            for (int i = 0; i < expected.getRelations().size(); i++) {
                Assertions.assertEquals(expected.getRelations().get(i), actual.getRelations().get(i));
            }
        }

        //  entityFields
        if (null != expected.getFields()) {
            Map<Long, IEntityField> entityFieldMap =
                actual.getFields().stream().collect(Collectors.toMap(IEntityField::id, f1 -> f1, (f1, f2) -> f1));

            for (int i = 0; i < expected.getFields().size(); i++) {
                IEntityField exp = expected.getFields().get(i);
                IEntityField act = entityFieldMap.remove(exp.id());
                Assertions.assertNotNull(act);
                Assertions.assertEquals(exp, act);
            }

            Assertions.assertEquals(0, entityFieldMap.size());
        }
    }

    private void initEntityStorage(List<EntityClassStorage> entityClassStorageList,
                                   List<ExpectedEntityStorage> expectedEntityStorageList) {
        /*
         * set self
         */
        ExpectedEntityStorage self =
            new ExpectedEntityStorage(5L, 10L, Arrays.asList(10L, 20L), Arrays.asList(10L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(self));
        expectedEntityStorageList.add(self);

        /*
         * set father
         */
        ExpectedEntityStorage father =
            new ExpectedEntityStorage(10L, 20L, Collections.singletonList(20L),
                Arrays.asList(20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(father));
        expectedEntityStorageList.add(father);

        /*
         * set ancestor
         */
        ExpectedEntityStorage ancestor =
            new ExpectedEntityStorage(20L, null, null, null);
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(ancestor));
        expectedEntityStorageList.add(ancestor);

        /*
         * set son
         */
        ExpectedEntityStorage son =
            new ExpectedEntityStorage(4L, 5L, Arrays.asList(5L, 10L, 20L),
                Arrays.asList(5L, 20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(son));
        expectedEntityStorageList.add(son);

        /*
         * set brother
         */
        ExpectedEntityStorage brother =
            new ExpectedEntityStorage(6L, 10L, Arrays.asList(10L, 20L),
                Arrays.asList(4L, 20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(brother));
        expectedEntityStorageList.add(brother);
    }
}
