package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.MockRequestHandler.EXIST_MIN_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_PROFILE_FOUR_GEN;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.storage.EntityClassStorageBuilderUtils.toFieldTypeValue;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.ProfileInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityUtils;
import com.xforceplus.ultraman.oqsengine.metadata.utils.offline.OffLineMetaHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class StorageMetaManagerTest extends AbstractMetaTestHelper {

    final Logger logger = LoggerFactory.getLogger(StorageMetaManagerTest.class);

    private static final String NEED_CONCURRENT_APP_ID = "test";
    private static final String[] NEED_CONCURRENT_APP_ENV_LIST = {"1", "2", "3"};

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
    }

    @Test
    public void needChangeEnvTest() throws IllegalAccessException {
        MetaManager metaManager = MetaInitialization.getInstance().getMetaManager();
        for (int j = 0; j < NEED_CONCURRENT_APP_ENV_LIST.length; j++) {
            String env = NEED_CONCURRENT_APP_ENV_LIST[j];

            metaManager.reset(NEED_CONCURRENT_APP_ID, env);
            Assertions.assertEquals(env, MetaInitialization.getInstance().getCacheExecutor().appEnvGet(NEED_CONCURRENT_APP_ID));
        }

        Assertions.assertThrows(RuntimeException.class, () -> {
            metaManager.need(NEED_CONCURRENT_APP_ID, NEED_CONCURRENT_APP_ENV_LIST[0]);
        });

        Assertions.assertThrows(RuntimeException.class, () -> {
            metaManager.need(NEED_CONCURRENT_APP_ID, NEED_CONCURRENT_APP_ENV_LIST[1]);
        });
    }

    @Test
    public void needConcurrentTest() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch startCountDown = new CountDownLatch(1);
        CountDownLatch endCountDown = new CountDownLatch(NEED_CONCURRENT_APP_ENV_LIST.length);
        List<Future> futures = Lists.newArrayList();
        for (int j = 0; j < NEED_CONCURRENT_APP_ENV_LIST.length; j++) {
            final int pos = j;
            Future future = executorService.submit(() -> {
                try {
                    startCountDown.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    String env = NEED_CONCURRENT_APP_ENV_LIST[pos];
                    MetaInitialization.getInstance().getMetaManager().need(NEED_CONCURRENT_APP_ID, env);
                    if (MetaInitialization.getInstance().getCacheExecutor().appEnvGet(NEED_CONCURRENT_APP_ID).equals(env)) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    logger.warn(e.getMessage());
                } finally {
                    endCountDown.countDown();
                }
            });
            futures.add(future);
        }

        startCountDown.countDown();

        endCountDown.await();

        Assertions.assertEquals(1, successCount.get());
        Assertions.assertEquals(NEED_CONCURRENT_APP_ENV_LIST.length - 1, failCount.get());
    }

    String defaultTestAppId = "5";
    String env = "0";
    int defaultTestVersion = 2;

    @Test
    public void showMeta() throws Exception {

        metaImportTest();

        Optional<MetaMetrics> result =
            MetaInitialization.getInstance().getMetaManager().showMeta(defaultTestAppId);

        Assertions.assertTrue(result.isPresent());

        MetaMetrics metaMetrics = result.get();
        Assertions.assertEquals(env, metaMetrics.getEnv());
        Assertions.assertEquals(defaultTestVersion, metaMetrics.getVersion());
        Assertions.assertTrue(metaMetrics.getMetas().size() > 0);
    }

    @Test
    public void metaImportTest() throws IOException, IllegalAccessException {

        InputStream in = null;
        try {
            StorageMetaManager storageMetaManager = (StorageMetaManager) MetaInitialization.getInstance().getMetaManager();
            storageMetaManager.init();
            in = initInputStreamByResource(defaultTestAppId, defaultTestVersion, env);

            boolean result = MetaInitialization.getInstance()
                .getMetaManager()
                .metaImport(defaultTestAppId, env, defaultTestVersion,
                    OffLineMetaHelper.initDataFromInputStream(defaultTestAppId, env, defaultTestVersion, in));
            Assertions.assertTrue(result);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        } finally {
            if (null != in) {
                in.close();
            }
        }

        Optional<IEntityClass> op =
            MetaInitialization.getInstance().getMetaManager().load(1251658380868685825L, "");

        Assertions.assertTrue(op.isPresent());

        //  重新导入老版本，结果为失败
        try {
            in = initInputStreamByResource(defaultTestAppId, defaultTestVersion, env);

            int failTestVersion = 1;

            boolean result = MetaInitialization.getInstance().getMetaManager().metaImport(defaultTestAppId, env, failTestVersion,
                OffLineMetaHelper.initDataFromInputStream(defaultTestAppId, env, failTestVersion, in));
            Assertions.assertFalse(result);
        } catch (Exception e) {
            Assertions.fail();
        } finally {
            if (null != in) {
                in.close();
            }
        }
    }

    @Test
    public void needTest() throws IllegalAccessException {
        String appId = "testNeed";
        String env = "test";
        int expectedVersion = EXIST_MIN_VERSION + 1;
        int version = MetaInitialization.getInstance().getMetaManager().need(appId, env);
        Assertions.assertEquals(expectedVersion, version);
    }

    @Test
    public void resetTest() throws IllegalAccessException {
        String appId = "testNeed";
        String env = "test";
        int expectedVersion = EXIST_MIN_VERSION + 1;
        int version = MetaInitialization.getInstance().getMetaManager().need(appId, env);
        Assertions.assertEquals(expectedVersion, version);

        env = "fat";
        version = MetaInitialization.getInstance().getMetaManager().reset(appId, env);
        Assertions.assertEquals(expectedVersion, version);
    }

    @Test
    public void loadByEntityRefTest() throws IllegalAccessException {
        String expectedAppId = "testLoad";
        String expectedAppCode = "loadByEntityRefTest";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;
        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        Optional<IEntityClass> entityClassOp = Optional.empty();
        try {
            entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, "");
        } catch (Exception e) {
            //  ignore
        }
        Assertions.assertFalse(entityClassOp.isPresent());

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedAppCode, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        //  测试替身1
        entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        Optional<IEntityField> fieldOp = entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true));
        Assertions.assertTrue(fieldOp.isPresent());

        //  不包含替身2
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true))
            .isPresent()
        );

        //  测试替身2
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, GeneralConstant.PROFILE_CODE_2.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        fieldOp = entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true)
        );
        Assertions.assertTrue(fieldOp.isPresent());
        //  不包含替身1
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true))
            .isPresent()
        );

        //  测试不带替身
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, null);
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true))
            .isPresent()
        );
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_GEN.getA(), true))
            .isPresent()
        );
    }

    @Test
    public void loadTest() throws IllegalAccessException {
        String expectedAppId = "testLoad";
        String expectedAppCode = "loadTest";
        int expectedVersion = 1;
        long expectedId = 1 + 3600;
        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        Optional<IEntityClass> entityClassOp = Optional.empty();
        try {
            entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, "");
        } catch (Exception e) {
            //  ignore
        }
        Assertions.assertFalse(entityClassOp.isPresent());

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedAppCode, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, "");
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        List<EntityClassInfo> entityClassInfo =
            entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assertions.assertNotNull(entityClassInfo);

        check(expectedVersion + 1, entityClassOp.get(), entityClassInfo);

        Collection<Relationship> re = entityClassOp.get().relationship();
        if (null != re) {
            re.forEach(
                s -> {
                    IEntityClass e = s.getRightEntityClass("");
                    Assertions.assertNotNull(e);
                    Assertions.assertEquals(s.getRightEntityClassId(), e.id());
                }
            );
        }

        /*
            check 自循环
         */
        long expectedAnc = expectedEntityStorageList.get(expectedEntityStorageList.size() - 1).getSelf();
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedAnc, "");
        Assertions.assertTrue(entityClassOp.isPresent());

        entityClassInfo =
            entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assertions.assertNotNull(entityClassInfo);
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        re = entityClassOp.get().relationship();
        if (null != re) {
            re.forEach(
                s -> {
                    IEntityClass e = s.getRightEntityClass("");
                    Assertions.assertNotNull(e);
                    Assertions.assertEquals(s.getRightEntityClassId(), e.id());
                }

            );
        }
    }

    @Test
    public void multiReadTest() throws IllegalAccessException {
        String appId = "testLoad";
        String expectedAppCode = "multiReadTest";
        int expectedVersion = 1;
        long expectedId = 1 + 7200;

        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(appId, expectedAppCode, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        Collection<IEntityClass> entityClasses =
            MetaInitialization.getInstance().getMetaManager().appLoad(appId);

        Assertions.assertEquals(9, entityClasses.size());
        for (IEntityClass entityClass : entityClasses) {
            Assertions.assertEquals(expectedAppCode, entityClass.appCode());
        }

        //  本层
        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == expectedId &&
                k.ref().getProfile().isEmpty()));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == expectedId &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_1.getKey())));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == expectedId &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_2.getKey())));

        // 父亲
        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.fatherId(expectedId)  &&
                k.ref().getProfile().isEmpty()));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.fatherId(expectedId) &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_1.getKey())));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.fatherId(expectedId) &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_2.getKey())));

        // 祖先
        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.ancId(expectedId)  &&
                k.ref().getProfile().isEmpty()));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.ancId(expectedId) &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_1.getKey())));

        Assertions.assertTrue(entityClasses.stream().
            anyMatch(k -> k.id() == GeneralEntityUtils.EntityClassHelper.ancId(expectedId) &&
                k.ref().getProfile().equals(GeneralConstant.PROFILE_CODE_2.getKey())));
    }

    /**
     * test & check.
     */
    private void check(int expectedVersion, IEntityClass entityClass, List<EntityClassInfo> entityClassInfos)
        throws IllegalAccessException {

        Assertions.assertTrue(entityClassInfos.size() > 0);
        Map<Long, EntityClassInfo> fullCheckMaps =
            entityClassInfos.stream().collect(Collectors.toMap(EntityClassInfo::getId, f1 -> f1, (f1, f2) -> f1));

        //  check current appId version
        Assertions.assertEquals(expectedVersion, MetaInitialization.getInstance().getCacheExecutor().version(entityClass.id(), false));

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
        Assertions.assertNotNull(entityClass);
        if (entityClass.id() == expectedId) {
            return entityClass;
        }

        if (entityClass.father().isPresent()) {
            return getEntityClass(expectedId, entityClass.father().get());
        }
        return null;
    }

    private void checkEntity(EntityClassInfo expected, IEntityClass actual,
                             Map<Long, List<EntityFieldInfo>> fieldMaps) {
        Assertions.assertNotNull(actual);
        //  basic
        Assertions.assertEquals(expected.getId(), actual.id());
        Assertions.assertEquals(expected.getCode(), actual.code());
        Assertions.assertEquals(expected.getVersion(), actual.version());
        Assertions.assertEquals(expected.getName(), actual.name());
        if (expected.getFather() >= MIN_ID) {
            Assertions.assertNotNull(actual.father());
            Assertions.assertEquals(expected.getFather(), actual.father().get().id());
        }

        Assertions.assertTrue(actual.ref().getProfile().equals(OqsProfile.UN_DEFINE_PROFILE) || expected.getProfilesList().stream().map(
            ProfileInfo::getCode).anyMatch(c -> {
                return c.equals(actual.ref().getProfile());
        }));

        Assertions.assertEquals(expected.getLevel(), actual.level());

        //  relations
        if (!expected.getRelationsList().isEmpty()) {
            Assertions.assertNotNull(actual.relationship());
            Map<Long, Relationship> actualRelations = new ArrayList<>(actual.relationship()).stream()
                .collect(Collectors.toMap(Relationship::getId, f1 -> f1, (f1, f2) -> f1));

            for (int i = 0; i < expected.getRelationsList().size(); i++) {

                RelationInfo expectedRelation = expected.getRelationsList().get(i);
                Relationship actualRelation = actualRelations.get(expectedRelation.getId());
                Assertions.assertNotNull(actualRelation);
                Assertions.assertEquals(expectedRelation.getCode(), actualRelation.getCode());
                Assertions.assertEquals(expectedRelation.getRightEntityClassId(), actualRelation.getRightEntityClassId());
                Assertions.assertEquals(expectedRelation.getLeftEntityClassId(), actualRelation.getLeftEntityClassId());
                Assertions.assertEquals(expectedRelation.getLeftEntityClassCode(), actualRelation.getLeftEntityClassCode());
                Assertions.assertEquals(expectedRelation.getRelationType(), actualRelation.getRelationType().ordinal());
                Assertions.assertEquals(expectedRelation.getIdentity(), actualRelation.isIdentity());
                Assertions.assertEquals(expectedRelation.getBelongToOwner(), actualRelation.isBelongToOwner());

                assertEntityField(expectedRelation.getEntityField(), actualRelation.getEntityField());
            }
        }

        //  entityFields
        List<EntityFieldInfo> expectedList = fieldMaps.get(expected.getId());
        if (null != expectedList) {

            Map<Long, IEntityField> entityFieldMap =
                actual.fields().stream().collect(Collectors.toMap(IEntityField::id, f1 -> f1, (f1, f2) -> f1));

            for (int i = 0; i < expectedList.size(); i++) {
                EntityFieldInfo exp = expectedList.get(i);
                IEntityField act = entityFieldMap.remove(exp.getId());
                Assertions.assertNotNull(act);

                assertEntityField(exp, act);
            }
        }
    }

    private void assertEntityField(EntityFieldInfo exp, IEntityField act) {
        Assertions.assertEquals(exp.getName(), act.name());
        Assertions.assertEquals(exp.getCname(), act.cnName());
        Assertions.assertEquals(exp.getFieldType().name().toUpperCase(), act.type().getType().toUpperCase());
        Assertions.assertEquals(exp.getDictId(), act.dictId());

        Assertions.assertTrue(act.defaultValue().isPresent());
        Assertions.assertEquals(exp.getDefaultValue(), act.defaultValue().get());

        if (act.calculationType().equals(CalculationType.FORMULA)) {
            Assertions.assertEquals(exp.getCalculator().getCalculateType(), CalculationType.FORMULA.getSymbol());
            Assertions.assertEquals(exp.getCalculator().getExpression(), ((Formula) act.config().getCalculation()).getExpression());
            Assertions.assertEquals(exp.getCalculator().getLevel(), ((Formula) act.config().getCalculation()).getLevel());
            Optional<?> opObject;
            try {
                opObject = toFieldTypeValue(act.type(), exp.getCalculator().getFailedDefaultValue());
            } catch (Exception e) {
                throw new RuntimeException(String.format("toFieldTypeValue failed, message : %s", e.getMessage()));
            }
            Assertions.assertTrue(opObject.isPresent());
            Assertions.assertEquals(opObject.get(), ((Formula) act.config().getCalculation()).getFailedDefaultValue());
            Assertions.assertEquals(exp.getCalculator().getArgsList().size(), ((Formula) act.config().getCalculation()).getArgs().size());
            Assertions.assertEquals(exp.getCalculator().getFailedPolicy(), ((Formula) act.config().getCalculation()).getFailedPolicy().getPolicy());
        } else if (act.calculationType().equals(CalculationType.AUTO_FILL)) {
            Assertions.assertEquals(exp.getCalculator().getCalculateType(), CalculationType.AUTO_FILL.getSymbol());
            Assertions.assertEquals(exp.getCalculator().getPatten(), ((AutoFill) act.config().getCalculation()).getPatten());
            Assertions.assertEquals(exp.getCalculator().getModel(), ((AutoFill) act.config().getCalculation()).getModel());
            Assertions.assertEquals(exp.getCalculator().getStep(), ((AutoFill) act.config().getCalculation()).getStep());

            Assertions.assertEquals(exp.getCalculator().getArgsList().size(), ((AutoFill) act.config().getCalculation()).getArgs().size());
            for (int i = 0; i < exp.getCalculator().getArgsList().size(); i++) {
                Assertions.assertEquals(exp.getCalculator().getArgsList().get(i), ((AutoFill) act.config().getCalculation()).getArgs().get(i));
            }

            Assertions.assertEquals(exp.getCalculator().getExpression(), ((AutoFill) act.config().getCalculation()).getExpression());
            Assertions.assertEquals(exp.getCalculator().getDomainNoSenior(), ((AutoFill) act.config().getCalculation()).getDomainNoType().getType());
            if (exp.getCalculator().getLevel() > MIN_ID) {
                Assertions.assertEquals(exp.getCalculator().getLevel(),
                    ((AutoFill) act.config().getCalculation()).getLevel());
            }
        }

        //  check field Config
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig efc = exp.getFieldConfig();
        if (efc.isInitialized()) {
            FieldConfig afc = act.config();
            Assertions.assertNotNull(afc);

            Assertions.assertEquals(efc.getSearchable(), afc.isSearchable());
            Assertions.assertEquals(efc.getLength(), afc.getLen());
            Assertions.assertEquals(efc.getPrecision(), afc.precision());
            Assertions.assertEquals(efc.getIdentifier(), afc.isIdentifie());
            Assertions.assertEquals(efc.getIsRequired(), afc.isRequired());
            Assertions.assertEquals(efc.getMetaFieldSenseValue(), afc.getFieldSense().ordinal());
            Assertions.assertEquals(efc.getValidateRegexString(), afc.getValidateRegexString());
            Assertions.assertEquals(efc.getDisplayType(), afc.getDisplayType());
        }
    }

    /**
     * 从resource目录中生成InputStream.
     */
    private InputStream initInputStreamByResource(String appId, Integer version, String env) {
        String path = String.format("/%s_%d_%s.json", appId, version, env);
        return OffLineMetaHelper.class.getResourceAsStream(path);
    }
}
