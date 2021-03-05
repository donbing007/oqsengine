package com.xforceplus.ulraman.oqsengine.metadata.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ulraman.oqsengine.metadata.mock.MockRequestHandler;
import com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassManagerExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xforceplus.ulraman.oqsengine.metadata.mock.MockRequestHandler.EXIST_MIN_VERSION;
import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.entityClassSyncResponseGenerator;
import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.mockSelfFatherAncestorsGenerate;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;

/**
 * desc :
 * name : EntityClassManagerExecutorTest
 *
 * @author : xujia
 * date : 2021/2/20
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class EntityClassManagerExecutorTest {

    private RedisClient redisClient;

    private CacheExecutor cacheExecutor;

    private EntityClassSyncExecutor entityClassSyncExecutor;

    private MockRequestHandler mockRequestHandler;

    private EntityClassManagerExecutor entityClassManagerExecutor;

    private ExecutorService executorService;


    @Before
    public void before() throws Exception {
        /**
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        /**
         * init cacheExecutor
         */
        cacheExecutor = new CacheExecutor();
        ObjectMapper objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        ReflectionTestUtils.setField(cacheExecutor, "objectMapper", objectMapper);
        cacheExecutor.init();

        /**
         * init entityClassExecutor
         */
        entityClassSyncExecutor = new EntityClassSyncExecutor();
        ReflectionTestUtils.setField(entityClassSyncExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassSyncExecutor, "expireExecutor", new ExpireExecutor());

        entityClassSyncExecutor.start();

        /**
         * init mockRequestHandler
         */
        mockRequestHandler = new MockRequestHandler();
        ReflectionTestUtils.setField(mockRequestHandler, "syncExecutor", entityClassSyncExecutor);

        /**
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));
        entityClassManagerExecutor = new EntityClassManagerExecutor();
        ReflectionTestUtils.setField(entityClassManagerExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassManagerExecutor, "requestHandler", mockRequestHandler);
        ReflectionTestUtils.setField(entityClassManagerExecutor, "asyncDispatcher", executorService);
    }

    @After
    public void after() throws Exception {
        entityClassSyncExecutor.stop();
        executorService.shutdown();

        cacheExecutor.destroy();
        cacheExecutor = null;

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    @Test
    public void needTest() {
        String appId = "testNeed";
        String env = "test";
        int expectedVersion = EXIST_MIN_VERSION + 1;
        int version = entityClassManagerExecutor.need(appId, env);
        Assert.assertEquals(expectedVersion, version);
    }

    @Test
    public void loadTest() throws InterruptedException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;
        List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorageList = mockSelfFatherAncestorsGenerate(expectedId);
        long expectedAnc = expectedEntityStorageList.get(expectedEntityStorageList.size() - 1).getSelf();
        try {
            entityClassManagerExecutor.load(expectedId);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().startsWith(String.format("load entityClass [%d] error, message", expectedId)));
        }

        EntityClassSyncResponse entityClassSyncResponse =
                entityClassSyncResponseGenerator(expectedAppId, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.onNext(entityClassSyncResponse);

        Optional<IEntityClass> entityClassOp = entityClassManagerExecutor.load(expectedId);
        Assert.assertTrue(entityClassOp.isPresent());

        List<EntityClassInfo> entityClassInfo =
                entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assert.assertNotNull(entityClassInfo);

        check(expectedVersion + 1, entityClassOp.get(), entityClassInfo);

        Collection<OqsRelation> re = entityClassOp.get().oqsRelations();
        if (null != re) {
            re.forEach(
                    s -> {
                        IEntityClass e = s.getEntityClass();
                        Assert.assertNotNull(e);
                        Assert.assertEquals(s.getEntityClassId(), e.id());
                    }

            );
        }

        /*
            check 自循环
         */
        entityClassOp = entityClassManagerExecutor.load(expectedAnc);
        Assert.assertTrue(entityClassOp.isPresent());

        entityClassInfo =
                entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assert.assertNotNull(entityClassInfo);

        re = entityClassOp.get().oqsRelations();
        if (null != re) {
            re.forEach(
                    s -> {
                        IEntityClass e = s.getEntityClass();
                        Assert.assertNotNull(e);
                        Assert.assertEquals(s.getEntityClassId(), e.id());
                    }

            );
        }
    }

    /**
     * test & check
     */
    private void check(int expectedVersion, IEntityClass entityClass, List<EntityClassInfo> entityClassInfos) {

        Assert.assertTrue(entityClassInfos.size() > 0);
        Map<Long, EntityClassInfo> fullCheckMaps =
                entityClassInfos.stream().collect(Collectors.toMap(EntityClassInfo::getId, f1 -> f1, (f1, f2) -> f1));

        //  check current appId version
        Assert.assertEquals(expectedVersion, cacheExecutor.version(entityClass.id()));

        Map<Long, List<EntityFieldInfo>> expectedFields = new HashMap<>();
        for (EntityClassInfo e : entityClassInfos) {
            List<EntityFieldInfo> fieldList = new ArrayList<>();
            EntityClassInfo element = e;
            while (null != element) {
                fieldList.addAll(element.getEntityFieldsList());
                if (element.getFather() < MIN_ID) {
                    element = null;
                } else {
                    element = fullCheckMaps.get(element.getFather());
                }
            }

            expectedFields.put(e.getId(), fieldList);
        }

        IEntityClass current = entityClass;
        for (EntityClassInfo e : entityClassInfos) {
            current = getEntityClass(e.getId(), current);
            checkEntity(e, current, expectedFields);
        }
    }

    private IEntityClass getEntityClass(long expectedId, IEntityClass entityClass) {
        Assert.assertNotNull(entityClass);
        if (entityClass.id() == expectedId) {
            return entityClass;
        }

        if (null != entityClass.father()) {
            return getEntityClass(expectedId, entityClass.father().get());
        }
        return null;
    }

    private void checkEntity(EntityClassInfo expected, IEntityClass actual, Map<Long, List<EntityFieldInfo>> fieldMaps) {
        Assert.assertNotNull(actual);
        //  basic
        Assert.assertEquals(expected.getId(), actual.id());
        Assert.assertEquals(expected.getCode(), actual.code());
        Assert.assertEquals(expected.getVersion(), actual.version());
        Assert.assertEquals(expected.getName(), actual.name());
        if (expected.getFather() >= MIN_ID) {
            Assert.assertNotNull(actual.father());
            Assert.assertEquals(expected.getFather(), actual.father().get().id());
        }
        Assert.assertEquals(expected.getLevel(), actual.level());

        //  relations
        if (null != expected.getRelationsList()) {
            Assert.assertNotNull(actual.oqsRelations());
            List<OqsRelation> actualRelations = new ArrayList<>(actual.oqsRelations());
            Assert.assertEquals(expected.getRelationsList().size(), actualRelations.size());
            for (int i = 0; i < expected.getRelationsList().size(); i++) {
                RelationInfo expectedRelation = expected.getRelationsList().get(i);
                OqsRelation actualRelation = actualRelations.get(i);
                Assert.assertEquals(expectedRelation.getId(), (long) actualRelation.getId());
                Assert.assertEquals(expectedRelation.getName(), actualRelation.getName());
                Assert.assertEquals(expectedRelation.getEntityClassId(), actualRelation.getEntityClassId());
                Assert.assertEquals(expectedRelation.getRelOwnerClassId(), actualRelation.getRelOwnerClassId());
                Assert.assertEquals(expectedRelation.getRelOwnerClassName(), actualRelation.getRelOwnerClassName());
                Assert.assertEquals(expectedRelation.getRelationType(), actualRelation.getRelationType());
                Assert.assertEquals(expectedRelation.getIdentity(), actualRelation.isIdentity());
                Assert.assertEquals(expectedRelation.getBelongToOwner(), actualRelation.isBelongToOwner());

                assertEntityField(expectedRelation.getEntityField(), actualRelation.getEntityField());
            }
        }

        //  entityFields
        List<EntityFieldInfo> expectedList = fieldMaps.get(expected.getId());
        Collection<IEntityField> actualList = actual.fields();
        if (null != expectedList) {
            Assert.assertEquals(expectedList.size(), actualList.size());

            Map<Long, IEntityField> entityFieldMap =
                    actual.fields().stream().collect(Collectors.toMap(IEntityField::id, f1 -> f1, (f1, f2) -> f1));

            for (int i = 0; i < expectedList.size(); i++) {
                EntityFieldInfo exp = expectedList.get(i);
                IEntityField act = entityFieldMap.remove(exp.getId());
                Assert.assertNotNull(act);

                assertEntityField(exp, act);
            }

            Assert.assertEquals(0, entityFieldMap.size());
        }
    }

    private void assertEntityField(EntityFieldInfo exp, IEntityField act) {
        Assert.assertEquals(exp.getName(), act.name());
        Assert.assertEquals(exp.getCname(), act.cnName());
        Assert.assertEquals(exp.getFieldType().name(), act.type().name());
        Assert.assertEquals(exp.getDictId(), act.dictId());
        Assert.assertEquals(exp.getDefaultValue(), act.defaultValue());

        //  check field Config
        com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig efc = exp.getFieldConfig();
        if (null != efc) {
            FieldConfig afc = act.config();
            Assert.assertNotNull(afc);

            Assert.assertEquals(efc.getSearchable(), afc.isSearchable());
            Assert.assertEquals(efc.getMax(), afc.getMax());
            Assert.assertEquals(efc.getMin(), afc.getMin());
            Assert.assertEquals(efc.getPrecision(), afc.getPrecision());
            Assert.assertEquals(efc.getIdentifier(), afc.isIdentifie());
            Assert.assertEquals(efc.getIsRequired(), afc.isRequired());
            Assert.assertEquals(efc.getMetaFieldSenseValue(), afc.getFieldSense().ordinal());
            Assert.assertEquals(efc.getValidateRegexString(), afc.getValidateRegexString());
            Assert.assertEquals(efc.getDisplayType(), afc.getDisplayType());
        }
    }
}
