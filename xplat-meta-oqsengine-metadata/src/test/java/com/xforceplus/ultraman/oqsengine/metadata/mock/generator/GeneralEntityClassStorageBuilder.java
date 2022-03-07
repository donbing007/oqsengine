package com.xforceplus.ultraman.oqsengine.metadata.mock.generator;

import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_ENTITY_INFO_LIST;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_PROFILE_FOUR_GEN;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.DEFAULT_ARGS;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.PROFILE_CODE_1;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.PROFILE_CODE_2;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.defaultValue;

import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.RelationStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AbstractCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/14
 * @since 1.8
 */
public class GeneralEntityClassStorageBuilder {

    private static String APP_CODE = "GeneralEntityClassStorageBuilder";

    /**
     * 生成.
     */
    public static RelationStorage relationLong(long id, long entityId, long fieldId) {

        return relationStorage(id, entityId, fieldId, FieldType.STRING);
    }

    /**
     * 生成.
     */
    public static RelationStorage relationString(long id, long entityId, long fieldId) {

        return relationStorage(id, entityId, fieldId, FieldType.STRING);
    }

    /**
     * 生成.
     */
    public static RelationStorage relationStorage(long id, long entityId, long fieldId, FieldType fieldType) {
        RelationStorage r = new RelationStorage();
        r.setId(id);
        r.setCode(GeneralEntityUtils.RelationHelper.code(id));
        r.setRightEntityClassId(entityId);
        r.setLeftEntityClassId(id);
        r.setRelationType(GeneralConstant.DEFAULT_RELATION_TYPE);
        r.setEntityField(EntityField.Builder.anEntityField()
            .withId(fieldId)
            .withFieldType(fieldType)
            .withName(GeneralEntityUtils.EntityFieldHelper.name(fieldId))
            .withConfig(fieldConfig(null))
            .build());
        r.setBelongToOwner(GeneralEntityUtils.RelationHelper.belongTo(id));
        return r;
    }

    public static <T extends AbstractCalculation> FieldConfig fieldConfig(T calculation) {
        FieldConfig.Builder builder = FieldConfig.Builder.anFieldConfig()
            .withFieldSense(FieldConfig.FieldSense.NORMAL)
            .withSearchable(true)
            .withRequired(true)
            .withIdentifie(false);

        if (null != calculation) {
            builder.withCalculation(calculation);
        }

        return builder.build();
    }

    public static StaticCalculation staticCalculation() {
        return StaticCalculation.Builder.anStaticCalculation().build();
    }

    public static Formula formula(String expression, int level, FieldType fieldType) {
        return Formula.Builder.anFormula()
            .withExpression(expression)
            .withLevel(level)
            .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
            .withFailedDefaultValue(defaultValue(fieldType))
            .withArgs(DEFAULT_ARGS)
            .build();
    }

    public static AutoFill autoFill(String patten, String model, String min, int step) {
        return AutoFill.Builder.anAutoFill()
            .withStep(step)
            .withDomainNoType(AutoFill.DomainNoType.instance(GeneralConstant.MOCK_DOMAIN_NOT_TYPE))
            .withLevel(GeneralConstant.MOCK_LEVEL)
            .withExpression(GeneralConstant.MOCK_SENIOR_EXPRESSION)
            .withArgs(GeneralConstant.MOCK_SENIOR_ARGS)
            .withMin(min)
            .withPatten(patten)
            .withModel(model)
            .build();
    }

    public static EntityField genericEntityField(long id,
                                                 GeneralConstant.FourGeneric<Integer, String, CalculationType, Boolean> fourTa) {

        FieldType fieldType = FieldType.fromRawType(fourTa.getB());


        EntityField.Builder builder = EntityField.Builder.anEntityField()
            .withId(GeneralEntityUtils.EntityFieldHelper.id(id + fourTa.getA(), fourTa.getD()))
            .withName(GeneralEntityUtils.EntityFieldHelper.name(id))
            .withCnName(GeneralEntityUtils.EntityFieldHelper.cname(id))
            .withDictId(GeneralEntityUtils.EntityFieldHelper.dictId(id))
            .withFieldType(fieldType);

        switch (fourTa.getC()) {
            case STATIC: {
                builder.withConfig(fieldConfig(staticCalculation()));
                break;
            }
            case FORMULA: {
                builder.withConfig(fieldConfig(formula(GeneralConstant.MOCK_EXPRESSION, GeneralConstant.MOCK_LEVEL, fieldType)));
                break;
            }
            case AUTO_FILL: {
                builder.withConfig(fieldConfig(autoFill(GeneralConstant.MOCK_PATTEN, GeneralConstant.MOCK_MODEL,
                    GeneralConstant.MOCK_MIN, GeneralConstant.MOCK_STEP)));
                break;
            }
            default: {
                throw new IllegalArgumentException("not support generate unknown-type calculator");
            }
        }

        return builder.build();
    }

    /**
     * 生成.
     */
    public static EntityClassStorage prepareEntity(
        ExpectedEntityStorage expectedEntityStorage) {
        EntityField[] entityFields = new EntityField[4];
        entityFields[0] = genericEntityField(expectedEntityStorage.getSelf(), EXPECTED_ENTITY_INFO_LIST.get(0));
        entityFields[1] = genericEntityField(expectedEntityStorage.getSelf(), EXPECTED_ENTITY_INFO_LIST.get(1));
        entityFields[2] = genericEntityField(expectedEntityStorage.getSelf(), EXPECTED_ENTITY_INFO_LIST.get(2));
        entityFields[3] = genericEntityField(expectedEntityStorage.getSelf(), EXPECTED_ENTITY_INFO_LIST.get(3));

        RelationStorage[] relations = new RelationStorage[2];
        if (null != expectedEntityStorage.getRelationIds() && !expectedEntityStorage.getRelationIds().isEmpty()) {
            relations[0] = relationLong(expectedEntityStorage.getSelf(), expectedEntityStorage.getRelationIds().get(0),
                expectedEntityStorage.getSelf());
            relations[1] = relationString(expectedEntityStorage.getSelf() + 1, expectedEntityStorage.getRelationIds().get(0),
                expectedEntityStorage.getSelf());
        }

        EntityClassStorage entityClassStorage = new EntityClassStorage();
        entityClassStorage.setId(expectedEntityStorage.getSelf());
        entityClassStorage.setAppCode(APP_CODE);
        entityClassStorage.setVersion(GeneralConstant.DEFAULT_VERSION);


        if (null != expectedEntityStorage.getFather()) {
            entityClassStorage.setFatherId(expectedEntityStorage.getFather());
        } else {
            entityClassStorage.setFatherId(0L);
        }

        if (null != expectedEntityStorage.getAncestors()) {
            entityClassStorage.setAncestors(expectedEntityStorage.getAncestors());
            entityClassStorage.setLevel(expectedEntityStorage.getAncestors().size());
        } else {
            entityClassStorage.setLevel(0);
        }

        entityClassStorage.setName(expectedEntityStorage.getSelf() + GeneralConstant.LEVEL
            + entityClassStorage.getLevel() + GeneralConstant.NAME_SUFFIX);

        entityClassStorage.setCode(expectedEntityStorage.getSelf() + GeneralConstant.LEVEL
            + entityClassStorage.getLevel() + GeneralConstant.CODE_SUFFIX);

        entityClassStorage.setFields(Arrays.asList(entityFields));
        entityClassStorage.setRelations(Arrays.asList(relations));
        entityClassStorage.setProfileStorageMap(profileStorage(expectedEntityStorage.getSelf()));
        return entityClassStorage;
    }

    public static Map<String, ProfileStorage> profileStorage(long id) {
        Map<String, ProfileStorage> profileStorageMap = new HashMap<>();

        profileStorageMap.put(PROFILE_CODE_1.getKey(),
                toProfile(GeneralConstant.PROFILE_CODE_1.getValue() * id, GeneralConstant.PROFILE_CODE_1.getKey()));
        profileStorageMap.put(PROFILE_CODE_2.getKey(),
            toProfile(GeneralConstant.PROFILE_CODE_2.getValue() * id, GeneralConstant.PROFILE_CODE_2.getKey()));

        return profileStorageMap;
    }

    public static ProfileStorage toProfile(long id, String code) {
        RelationStorage relationStorage = relationLong(id, id + GeneralConstant.MOCK_PROFILE_E_DISTANCE, id);
        EntityField entityField = genericEntityField(id, EXPECTED_PROFILE_FOUR_GEN);

        return new ProfileStorage(code, Collections.singletonList(entityField), Collections.singletonList(relationStorage));
    }


}
