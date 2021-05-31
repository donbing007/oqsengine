package com.xforceplus.ultraman.oqsengine.metadata.mock.generator;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.ProfileInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/14
 * @since 1.8
 */
public class EntityClassSyncProtoBufMocker {
    public static List<GeneralConstant.FourTa<Integer, String, com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type, Boolean>>
        EXPECTED_ENTITY_INFO_LIST =
        Arrays.asList(
            new GeneralConstant.FourTa<>(1, FieldType.LONG.name(),
                com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.NORMAL, false),
            new GeneralConstant.FourTa<>(2, FieldType.STRING.name(),
                com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.NORMAL, false),
            new GeneralConstant.FourTa<>(3, FieldType.LONG.name(),
                com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.FORMULA, false),
            new GeneralConstant.FourTa<>(4, FieldType.STRING.name(),
                com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.AUTO_FILL, false)
        );

    public static GeneralConstant.FourTa<Integer, String, com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type, Boolean>
        EXPECTED_PROFILE_FOUR_TA =
        new GeneralConstant.FourTa<>(10, FieldType.LONG.name(),
            com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.FORMULA, true);

    public static class Response {
        /**
         * 将生成随机的3层父子类结构[爷爷、父亲、儿子].
         * 每层有2个随机的EntityField [String、Long] 各一
         * 每层存在2条关系
         */
        public static EntityClassSyncResponse entityClassSyncResponseGenerator(String appId, int version,
                                                                               List<ExpectedEntityStorage> expectedEntityStorages) {
            return EntityClassSyncResponse.newBuilder()
                .setAppId(appId)
                .setVersion(version + 1)
                .setEntityClassSyncRspProto(entityClassSyncRspProtoGenerator(expectedEntityStorages))
                .build();
        }

        /**
         * 生成同步响应.
         */
        public static EntityClassSyncRspProto entityClassSyncRspProtoGenerator(
            List<ExpectedEntityStorage> expectedEntityStorages) {
            /*
             * 生成爷爷
             */
            List<EntityClassInfo> entityClassInfos = new ArrayList<>();
            expectedEntityStorages.forEach(
                e -> {
                    entityClassInfos.add(entityClassInfo(e.getSelf(), e.getFather(), e.getRelationIds(),
                        null != e.getAncestors() ? e.getAncestors().size() : 0));
                }
            );

            return EntityClassSyncRspProto.newBuilder()
                .addAllEntityClasses(entityClassInfos)
                .build();
        }
    }


    /**
     * 生成.
     */
    public static EntityClassInfo entityClassInfo(long id, long father, List<Long> relationEntityIds, int level) {
        List<RelationInfo> relationInfos = new ArrayList<>();
        List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();

        entityFieldInfos.add(entityFieldInfo(id, EXPECTED_ENTITY_INFO_LIST.get(0)));
        entityFieldInfos.add(entityFieldInfo(id, EXPECTED_ENTITY_INFO_LIST.get(1)));
        entityFieldInfos.add(entityFieldInfo(id, EXPECTED_ENTITY_INFO_LIST.get(2)));
        entityFieldInfos.add(entityFieldInfo(id, EXPECTED_ENTITY_INFO_LIST.get(3)));

        if (null != relationEntityIds) {
            for (int i = 0; i < relationEntityIds.size(); i++) {
                RelationInfo relationInfo = relationInfo(id + i, relationEntityIds.get(i),
                    id, GeneralConstant.DEFAULT_RELATION_TYPE, id + i);
                relationInfos.add(relationInfo);
            }
        }

        return EntityClassInfo.newBuilder()
            .setId(id)
            .setVersion(GeneralConstant.DEFAULT_VERSION)
            .setCode(id + GeneralConstant.LEVEL + level + GeneralConstant.CODE_SUFFIX)
            .setName(id + GeneralConstant.LEVEL + level + GeneralConstant.NAME_SUFFIX)
            .setFather(father)
            .setLevel(level)
            .addAllEntityFields(entityFieldInfos)
            .addAllRelations(relationInfos)
            .addProfiles(
                profileInfo(GeneralConstant.PROFILE_CODE_1.getValue() * id, GeneralConstant.PROFILE_CODE_1.getKey(), EXPECTED_PROFILE_FOUR_TA)
            )
            .addProfiles(
                profileInfo(GeneralConstant.PROFILE_CODE_2.getValue() * id, GeneralConstant.PROFILE_CODE_2.getKey(), EXPECTED_PROFILE_FOUR_TA))
            .build();
    }

    /**
     * 生成profileInfo.
     */
    public static ProfileInfo profileInfo(long id, String code, GeneralConstant.FourTa<Integer, String,
        com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type, Boolean> fourTa) {
        return ProfileInfo.newBuilder().setCode(code)
            .addRelationInfo(relationInfo(id, id + GeneralConstant.MOCK_PROFILE_R_DISTANCE, id,
                GeneralConstant.DEFAULT_RELATION_TYPE, id))
            .addEntityFieldInfo(
                entityFieldInfo(id, fourTa)
            ).build();
    }

    /**
     * 生成通用calculator
     */
    public static Calculator genericCalculator() {
        return Calculator.newBuilder()
            .setCalculateType(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.NORMAL.getType())
            .build();
    }

    /**
     * 生成formula-calculator
     */
    public static Calculator formulaCalculator(String expression, int level) {
        return Calculator.newBuilder()
            .setCalculateType(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.FORMULA.getType())
            .setExpression(expression).setLevel(level).build();
    }

    /**
     * 生成autoFill-calculator
     */
    public static Calculator autoFillCalculator(String patten, String model, String min, int step) {
        return Calculator.newBuilder()
            .setCalculateType(com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type.AUTO_FILL.getType())
            .setPatten(patten).setModel(model).setMin(min).setStep(step).build();
    }

    private static EntityFieldInfo.FieldType toFieldType(String type) {
        for (EntityFieldInfo.FieldType fieldType : EntityFieldInfo.FieldType.values()) {
            if (fieldType.name().equals(type)) {
                return fieldType;
            }
        }
        return EntityFieldInfo.FieldType.UNKNOWN;
    }

    /**
     * 生成entityFieldInfo.
     */
    public static EntityFieldInfo entityFieldInfo(long id,
                                                  GeneralConstant.FourTa<Integer, String,
                                                      com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.Type, Boolean> fourTa) {
        EntityFieldInfo.Builder builder = EntityFieldInfo.newBuilder()
            .setId(GeneralEntityUtils.EntityFieldHelper.id(id + fourTa.getA(), fourTa.getD()))
            .setName(GeneralEntityUtils.EntityFieldHelper.name(id))
            .setCname(GeneralEntityUtils.EntityFieldHelper.cname(id))
            .setFieldType(toFieldType(fourTa.getB()))
            .setDictId(GeneralEntityUtils.EntityFieldHelper.dictId(id))
            .setFieldConfig(fieldConfig(true, GeneralConstant.MOCK_SYSTEM_FIELD_TYPE));

        switch (fourTa.getC()) {
            case NORMAL: {
                builder.setCalculator(genericCalculator());
                break;
            }
            case FORMULA: {
                builder.setCalculator(formulaCalculator(GeneralConstant.MOCK_EXPRESSION, GeneralConstant.MOCK_LEVEL));
                break;
            }
            case AUTO_FILL: {
                builder.setCalculator(autoFillCalculator(GeneralConstant.MOCK_PATTEN, GeneralConstant.MOCK_MODEL,
                    GeneralConstant.MOCK_MIN, GeneralConstant.MOCK_STEP));
                break;
            }
            default: {
                throw new IllegalArgumentException("not support generate unknown-type calculator");
            }
        }
        return builder.build();
    }

    /**
     * 生成relationInfo.
     */
    public static RelationInfo relationInfo(long id, long entityId, long ownerId, int relationType, long fieldId) {
        return RelationInfo.newBuilder()
            .setId(id)
            .setCode(GeneralEntityUtils.RelationHelper.code(id))
            .setRightEntityClassId(entityId)
            .setLeftEntityClassId(ownerId)
            .setRelationType(relationType)
            .setEntityField(EntityFieldInfo.newBuilder()
                .setId(fieldId)
                .setFieldType(toFieldType(FieldType.LONG.name()))
                .setName(GeneralEntityUtils.EntityFieldHelper.name(fieldId))
                .setFieldConfig(fieldConfig(true, GeneralConstant.MOCK_SYSTEM_FIELD_TYPE))
                .build())
            .setBelongToOwner(GeneralEntityUtils.RelationHelper.belongTo(id))
            .build();
    }

    /**
     * 生成.
     */
    public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig fieldConfig(
        boolean searchable, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense systemFieldType) {
        return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.newBuilder()
            .setSearchable(searchable)
            .setIsRequired(true)
            .setIdentifier(false)
            .setMetaFieldSense(systemFieldType)
            .build();
    }

    /**
     * 生成.
     */
    public static List<ExpectedEntityStorage> mockSelfFatherAncestorsGenerate(long id) {
        List<ExpectedEntityStorage> expectedEntityStorages = new ArrayList<>();

        long father = GeneralEntityUtils.EntityClassHelper.fatherId(id);
        long anc = GeneralEntityUtils.EntityClassHelper.ancId(id);

        //  add self
        expectedEntityStorages.add(
            new ExpectedEntityStorage(id, father, Arrays.asList(father, anc),
                Collections.singletonList(father)));

        //  add father
        expectedEntityStorages.add(
            new ExpectedEntityStorage(father, anc, Collections.singletonList(anc),
                Collections.singletonList(id)));
        //  add anc
        expectedEntityStorages
            .add(new ExpectedEntityStorage(anc, 0L, null, Collections.singletonList(anc)));

        return expectedEntityStorages;
    }
}
