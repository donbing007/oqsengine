package com.xforceplus.ultraman.oqsengine.metadata.cache;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.event.payload.meta.MetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder;
import com.xforceplus.ultraman.oqsengine.metadata.utils.storage.CacheToStorageGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

    private CacheExecutor cacheExecutor;

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
    public void batchVersionsTest() {
        Map<Long, Integer> expectedVersions = new HashMap<>();
        List<Long> testEntityClassId = new ArrayList<>();

        String appId = "testAppId_1";
        int version = 1;
        long entityClassId = 10001;
        cacheExecutor.resetVersion(appId, version, Collections.singletonList(entityClassId));
        expectedVersions.put(entityClassId, version);
        testEntityClassId.add(entityClassId);

        appId = "testAppId_2";
        version = 2;
        entityClassId = 10002;
        cacheExecutor.resetVersion(appId, version, Collections.singletonList(entityClassId));
        expectedVersions.put(entityClassId, version);
        testEntityClassId.add(entityClassId);

        appId = "testAppId_3";
        version = 3;
        entityClassId = 10003;
        cacheExecutor.resetVersion(appId, version, Collections.singletonList(entityClassId));
        expectedVersions.put(entityClassId, version);
        testEntityClassId.add(entityClassId);

        Map<Long, Integer> result = cacheExecutor.versions(testEntityClassId, false);

        Assertions.assertEquals(expectedVersions.size(), result.size());

        result.forEach(
            (k, v) -> {
                Integer vExpected = expectedVersions.get(k);
                Assertions.assertEquals(vExpected, v);
            }
        );
    }

    @Test
    public void saveTestPayloadCheck() throws JsonProcessingException {

        // 纯粹测试新增.
        List<EntityClassStorage> entityClassStorageList = new ArrayList<>();
        EntityClassStorage one = MetaPayLoadHelper.toBasicPrepareEntity(1);
        one.getFields().add(MetaPayLoadHelper.genericEntityField(10001, FieldType.STRING, CalculationType.STATIC, OperationType.CREATE));
        one.getFields().add(MetaPayLoadHelper.genericEntityField(10002, FieldType.STRING, CalculationType.FORMULA, OperationType.CREATE));
        one.getFields().add(MetaPayLoadHelper.genericEntityField(10003, FieldType.STRING, CalculationType.AUTO_FILL, OperationType.CREATE));

        entityClassStorageList.add(one);
        EntityClassStorage two = MetaPayLoadHelper.toBasicPrepareEntity(2);
        two.getFields().add(MetaPayLoadHelper.genericEntityField(20001, FieldType.STRING, CalculationType.STATIC, OperationType.CREATE));
        two.getFields().add(MetaPayLoadHelper.genericEntityField(20002, FieldType.STRING, CalculationType.FORMULA, OperationType.CREATE));
        two.getFields().add(MetaPayLoadHelper.genericEntityField(20003, FieldType.STRING, CalculationType.AUTO_FILL, OperationType.CREATE));
        entityClassStorageList.add(two);

        MetaChangePayLoad metaChangePayLoad =
            cacheExecutor.save("1", 1, entityClassStorageList);

        checkMetaPayLoad(metaChangePayLoad, "1", 1, entityClassStorageList);


        //  这里是预期需要修改的Field
        List<EntityClassStorage> mixedUpdateDeletes = new ArrayList<>();
        EntityClassStorage oldOne = MetaPayLoadHelper.toBasicPrepareEntity(1);
        oldOne.getFields().add(MetaPayLoadHelper.genericEntityField(10001, FieldType.STRING, CalculationType.STATIC, OperationType.DELETE));
        oldOne.getFields().add(MetaPayLoadHelper.genericEntityField(10002, FieldType.STRING, CalculationType.FORMULA, OperationType.UPDATE));

        mixedUpdateDeletes.add(oldOne);
        EntityClassStorage oldTwo = MetaPayLoadHelper.toBasicPrepareEntity(2);
        oldTwo.getFields().add(MetaPayLoadHelper.genericEntityField(20001, FieldType.STRING, CalculationType.STATIC, OperationType.DELETE));
        oldTwo.getFields().add(MetaPayLoadHelper.genericEntityField(20002, FieldType.STRING, CalculationType.FORMULA, OperationType.UPDATE));
        oldTwo.getFields().add(MetaPayLoadHelper.genericEntityField(20003, FieldType.STRING, CalculationType.AUTO_FILL, OperationType.UPDATE));
        mixedUpdateDeletes.add(oldTwo);

        //  写入更新对象
        entityClassStorageList.clear();
        EntityClassStorage doOne = MetaPayLoadHelper.toBasicPrepareEntity(1);
        doOne.getFields().add(MetaPayLoadHelper.genericEntityField(10002, FieldType.STRING, CalculationType.FORMULA, OperationType.UPDATE));
        doOne.getFields().add(MetaPayLoadHelper.genericEntityField(10003, FieldType.STRING, CalculationType.AUTO_FILL, OperationType.CREATE));
        entityClassStorageList.add(doOne);

        EntityClassStorage doTwo = MetaPayLoadHelper.toBasicPrepareEntity(2);
        doTwo.getFields().add(MetaPayLoadHelper.genericEntityField(20002, FieldType.STRING, CalculationType.FORMULA, OperationType.UPDATE));
        doTwo.getFields().add(MetaPayLoadHelper.genericEntityField(20003, FieldType.STRING, CalculationType.AUTO_FILL, OperationType.UPDATE));
        entityClassStorageList.add(doTwo);

        metaChangePayLoad =
            cacheExecutor.save("1", 3, entityClassStorageList);

        checkMetaPayLoad(metaChangePayLoad, "1", 3, mixedUpdateDeletes);
    }


    /**
     * 测试版本.
     */
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
    public void versionsTest() {
        Map<Long, Integer> expects = new HashMap<>();
        List<Long> entityClassIds = new ArrayList<>();

        int expectedVersion = 2;
        entityClassIds.addAll(addAndRetEntityClassId(expects, "testApp1", expectedVersion, Arrays.asList(1L, 2L)));
        entityClassIds.addAll(addAndRetEntityClassId(expects, "testApp2", expectedVersion + 1, Arrays.asList(3L, 4L)));
        entityClassIds.addAll(addAndRetEntityClassId(expects, "testApp3", expectedVersion + 2, Arrays.asList(5L, 6L)));

        Map<Long, Integer> res = cacheExecutor.versions(entityClassIds, false);

        Assertions.assertEquals(expects.size(), res.size());

        res.forEach(
            (k,expected) -> {
                Integer value = expects.get(k);
                Assertions.assertEquals(expected, value);
            }
        );

    }
    private List<Long> addAndRetEntityClassId(Map<Long, Integer> expects, String appId, int version, List<Long> entityClassIds) {
        boolean ret = cacheExecutor.resetVersion(appId, version, entityClassIds);
        if (!ret) {
            throw new RuntimeException("reset version failed.");
        }

        entityClassIds.forEach(
            e -> {
                expects.put(e, version);
            }
        );

        return entityClassIds;
    }

    @Test
    public void entityClassStorageQueryTest() throws JsonProcessingException {

        List<EntityClassStorage> entityClassStorageList = new ArrayList<>();
        List<ExpectedEntityStorage> expectedEntityStorageList = new ArrayList<>();

        initEntityStorage(entityClassStorageList, expectedEntityStorageList);

        String expectedAppId = "testEntityQuery";
        int expectedVersion = Integer.MAX_VALUE;

        //  set storage
        cacheExecutor.save(expectedAppId, expectedVersion, entityClassStorageList);

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
        cacheExecutor.save(expectedAppId, expectedVersion, entityClassStorageList);

        check(expectedVersion, expectedEntityStorageList, null);

        boolean ret = cacheExecutor.clean(expectedAppId, expectedVersion, true);
        Assertions.assertTrue(ret);

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            invalid(e.getSelf(), "entityClassStorage is null, may be delete.");
        }
    }

    private void invalid(Long id, String message) {
        try {
            cacheExecutor.remoteRead(id);
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
            Map<String, String> results = cacheExecutor.remoteRead(e.getSelf());

            Assertions.assertTrue(null != results && !results.isEmpty());

            if (null != e.getAncestors()) {
                for (Long id : e.getAncestors()) {
                    Map<String, String> r = cacheExecutor.remoteRead(id);
                    Assertions.assertNotNull(r);

                    if (null != fullCheckMaps) {
                        checkEntity(fullCheckMaps.get(id), CacheToStorageGenerator.toEntityClassStorage(DefaultCacheExecutor.OBJECT_MAPPER, r));
                    }
                }
            }
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
            new ExpectedEntityStorage(5L, 10L, Arrays.asList(10L, 20L), Collections.singletonList(10L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(self));
        expectedEntityStorageList.add(self);

        /*
         * set father
         */
        ExpectedEntityStorage father =
            new ExpectedEntityStorage(10L, 20L, Collections.singletonList(20L),
                Collections.singletonList(20L));
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


    private void checkMetaPayLoad(MetaChangePayLoad metaChangePayLoad, String appId, int version, List<EntityClassStorage> entityClassStorageList)  {
        Assertions.assertEquals(appId, metaChangePayLoad.getAppId());

        Assertions.assertEquals(version, metaChangePayLoad.getVersion());

        Assertions.assertEquals(entityClassStorageList.size(), metaChangePayLoad.getEntityChanges().size());

        metaChangePayLoad.getEntityChanges().forEach(
            entityChange -> {
                EntityClassStorage entityClass = entityClassStorageList.stream().filter(k -> {
                    return k.getId() == entityChange.getEntityClassId();
                }).findFirst().orElse(null);

                Assertions.assertNotNull(entityClass);

                entityChange.getFieldChanges().forEach(
                    fieldChange -> {
                        EntityField entityField = null;
                        if (null == fieldChange.getProfile()) {
                            entityField =
                                entityClass.getFields().stream().filter(s -> {
                                    return s.id() == fieldChange.getFieldId();
                                }).findFirst().orElse(null);
                        } else {
                            ProfileStorage profileStorage =
                                entityClass.getProfileStorageMap().get(fieldChange.getProfile());
                            Assertions.assertNotNull(profileStorage);
                            entityField = profileStorage.getEntityFieldList().stream().filter(s -> {
                                return s.id() == fieldChange.getFieldId();
                            }).findFirst().orElse(null);
                        }

                        Assertions.assertNotNull(entityField);
                    }
                );
            }
        );
    }
}
