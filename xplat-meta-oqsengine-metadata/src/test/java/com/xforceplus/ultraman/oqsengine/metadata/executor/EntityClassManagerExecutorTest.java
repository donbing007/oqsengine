package com.xforceplus.ultraman.oqsengine.metadata.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.MockRequestHandler.EXIST_MIN_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_PROFILE_FOUR_TA;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ProtoAnyHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaTestHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试.
 *
 * @author xujia 2021/2/20
 * @since 1.8
 */
public class EntityClassManagerExecutorTest extends MetaTestHelper {

    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
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
    public void loadByEntityRefTest() throws IllegalAccessException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;
        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);
        try {
            MetaInitialization.getInstance().getMetaManager().load(expectedId);
        } catch (Exception e) {
            Assertions.assertTrue(
                e.getMessage().startsWith(String.format("load entityClass [%d] error, message", expectedId)));
        }

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        //  测试替身1
        Optional<IEntityClass> entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());

        Optional<IEntityField> fieldOp = entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true));
        Assertions.assertTrue(fieldOp.isPresent());

        //  不包含替身2
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true))
            .isPresent()
        );

        //  测试替身2
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, GeneralConstant.PROFILE_CODE_2.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());

        fieldOp = entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true)
        );
        Assertions.assertTrue(fieldOp.isPresent());
        //  不包含替身1
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true))
            .isPresent()
        );

        //  测试不带替身
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId, null);
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_1.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true))
            .isPresent()
        );
        Assertions.assertFalse(entityClassOp.get().field(
            GeneralEntityUtils.EntityFieldHelper
                .id(GeneralConstant.PROFILE_CODE_2.getValue() * expectedId + EXPECTED_PROFILE_FOUR_TA.getA(), true))
            .isPresent()
        );
    }

    @Test
    public void loadTest() throws InterruptedException, IllegalAccessException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = 1 + 3600;
        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        try {
            MetaInitialization.getInstance().getMetaManager().load(expectedId);
        } catch (Exception e) {
            Assertions.assertTrue(
                e.getMessage().startsWith(String.format("load entityClass [%d] error, message", expectedId)));
        }

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        Optional<IEntityClass> entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedId);
        Assertions.assertTrue(entityClassOp.isPresent());

        List<EntityClassInfo> entityClassInfo =
            entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assertions.assertNotNull(entityClassInfo);

        check(expectedVersion + 1, entityClassOp.get(), entityClassInfo);

        Collection<OqsRelation> re = entityClassOp.get().oqsRelations();
        if (null != re) {
            re.forEach(
                s -> {
                    IEntityClass e = s.getRightEntityClass();
                    Assertions.assertNotNull(e);
                    Assertions.assertEquals(s.getRightEntityClassId(), e.id());
                }
            );
        }

        /*
            check 自循环
         */
        long expectedAnc = expectedEntityStorageList.get(expectedEntityStorageList.size() - 1).getSelf();
        entityClassOp = MetaInitialization.getInstance().getMetaManager().load(expectedAnc);
        Assertions.assertTrue(entityClassOp.isPresent());

        entityClassInfo =
            entityClassSyncResponse.getEntityClassSyncRspProto().getEntityClassesList();

        Assertions.assertNotNull(entityClassInfo);

        re = entityClassOp.get().oqsRelations();
        if (null != re) {
            re.forEach(
                s -> {
                    IEntityClass e = s.getRightEntityClass();
                    Assertions.assertNotNull(e);
                    Assertions.assertEquals(s.getRightEntityClassId(), e.id());
                }

            );
        }
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
        Assertions.assertEquals(expectedVersion, MetaInitialization.getInstance().getCacheExecutor().version(entityClass.id()));

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
        Assertions.assertEquals(expected.getLevel(), actual.level());

        //  relations
        if (!expected.getRelationsList().isEmpty()) {
            Assertions.assertNotNull(actual.oqsRelations());
            Map<Long, OqsRelation> actualRelations = new ArrayList<>(actual.oqsRelations()).stream()
                .collect(Collectors.toMap(OqsRelation::getId, f1 -> f1, (f1, f2) -> f1));

            for (int i = 0; i < expected.getRelationsList().size(); i++) {

                RelationInfo expectedRelation = expected.getRelationsList().get(i);
                OqsRelation actualRelation = actualRelations.get(expectedRelation.getId());
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
        Assertions.assertEquals(exp.getDefaultValue(), act.defaultValue());

        if (act.calculateType().equals(Calculator.Type.FORMULA)) {
            Assertions.assertEquals(exp.getCalculator().getCalculateType(), Calculator.Type.FORMULA.getType());
            Assertions.assertEquals(exp.getCalculator().getExpression(), act.calculator().getExpression());
            Assertions.assertEquals(exp.getCalculator().getLevel(), act.calculator().getLevel());
            Optional<?> opObject;
            try {
                opObject = ProtoAnyHelper.toFieldTypeValue(act.type(), exp.getCalculator().getFailedDefaultValue());
            } catch (Exception e) {
                throw new RuntimeException(String.format("toFieldTypeValue failed, message : %s", e.getMessage()));
            }
            Assertions.assertTrue(opObject.isPresent());
            Assertions.assertEquals(opObject.get(), act.calculator().getFailedDefaultValue());
            Assertions.assertEquals(exp.getCalculator().getArgsList().size(), act.calculator().getArgs().size());
            Assertions.assertEquals(exp.getCalculator().getFailedPolicy(), act.calculator().getFailedPolicy().getPolicy());
        } else if (act.calculateType().equals(Calculator.Type.AUTO_FILL)) {
            Assertions.assertEquals(exp.getCalculator().getCalculateType(), Calculator.Type.AUTO_FILL.getType());
            Assertions.assertEquals(exp.getCalculator().getPatten(), act.calculator().getPatten());
            Assertions.assertEquals(exp.getCalculator().getModel(), act.calculator().getModel());
            Assertions.assertEquals(exp.getCalculator().getStep(), act.calculator().getStep());
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
}
