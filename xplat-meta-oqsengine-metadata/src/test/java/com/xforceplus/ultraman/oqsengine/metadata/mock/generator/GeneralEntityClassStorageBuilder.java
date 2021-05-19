package com.xforceplus.ultraman.oqsengine.metadata.mock.generator;

import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_ENTITY_INFO_LIST;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker.EXPECTED_PROFILE_FOUR_TA;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.PROFILE_CODE_1;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.PROFILE_CODE_2;

import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.RelationStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculateType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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
            .withConfig(defaultFieldConfig())
            .build());
        r.setBelongToOwner(GeneralEntityUtils.RelationHelper.belongTo(id));
        return r;
    }

    public static FieldConfig defaultFieldConfig() {
        return FieldConfig.Builder.anFieldConfig()
            .withFieldSense(FieldConfig.FieldSense.NORMAL)
            .withSearchable(true)
            .withRequired(true)
            .withIdentifie(false)
            .build();
    }

    public static Calculator defaultCalculator() {
        return Calculator.Builder.anCalculator().withCalculateType(CalculateType.NORMAL).build();
    }

    public static Calculator formulaCalculator(String expression, int level) {
        return Calculator.Builder.anCalculator()
            .withCalculateType(CalculateType.FORMULA)
            .withExpression(expression)
            .withLevel(level)
            .build();
    }

    public static Calculator autoFillCalculator(String patten, String model, String min, int step) {
        return Calculator.Builder.anCalculator()
            .withCalculateType(CalculateType.AUTO_FILL)
            .withStep(step)
            .withMin(min)
            .withPatten(patten)
            .withModel(model)
            .build();
    }

    public static EntityField genericEntityField(long id,
                                                 GeneralConstant.FourTa<Integer, FieldType, CalculateType, Boolean> fourTa) {
        EntityField.Builder builder = EntityField.Builder.anEntityField()
            .withCalculator(defaultCalculator())
            .withId(GeneralEntityUtils.EntityFieldHelper.id(id + fourTa.getA(), fourTa.getD()))
            .withName(GeneralEntityUtils.EntityFieldHelper.name(id))
            .withCnName(GeneralEntityUtils.EntityFieldHelper.cname(id))
            .withDictId(GeneralEntityUtils.EntityFieldHelper.dictId(id))
            .withFieldType(FieldType.fromRawType(fourTa.getB().getType()))
            .withConfig(defaultFieldConfig());

        switch (fourTa.getC()) {
            case NORMAL: {
                builder.withCalculator(defaultCalculator());
                break;
            }
            case FORMULA: {
                builder.withCalculator(formulaCalculator(GeneralConstant.MOCK_EXPRESSION, GeneralConstant.MOCK_LEVEL));
                break;
            }
            case AUTO_FILL: {
                builder.withCalculator(autoFillCalculator(GeneralConstant.MOCK_PATTEN, GeneralConstant.MOCK_MODEL,
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
     * 生成.
     */
    public static EntityClassStorage prepareEntity(
        ExpectedEntityStorage expectedEntityStorage) {
        IEntityField[] entityFields = new IEntityField[4];
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
        EntityField entityField = genericEntityField(id, EXPECTED_PROFILE_FOUR_TA);

        return new ProfileStorage(code, Collections.singletonList(entityField), Collections.singletonList(relationStorage));
    }


}
