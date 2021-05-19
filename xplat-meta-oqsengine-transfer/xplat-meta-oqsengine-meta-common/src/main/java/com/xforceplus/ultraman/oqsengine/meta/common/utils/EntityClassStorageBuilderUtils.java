package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.RelationStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.ProfileInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculateType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * EntityClassStorageBuilderUtils.
 *
 * @author : xujia
 * @since : 1.8
 */
public class EntityClassStorageBuilderUtils {

    private static final int MIN_FORMULA_LEVEL = 1;

    /**
     * 将protoBuf转为EntityClassStorage列表.
     */
    public static List<EntityClassStorage> protoToStorageList(EntityClassSyncRspProto entityClassSyncRspProto) {
        Map<Long, EntityClassStorage> temp = entityClassSyncRspProto.getEntityClassesList().stream().map(
            ecs -> {
                EntityClassStorage e = protoValuesToLocalStorage(ecs);
                return e;
            }
        ).collect(Collectors.toMap(EntityClassStorage::getId, s1 -> s1, (s1, s2) -> s1));

        return temp.values().stream().peek(
            v -> {
                Long fatherId = v.getFatherId();
                while (null != fatherId && fatherId >= MIN_ID) {
                    EntityClassStorage entityClassStorage = temp.get(fatherId);
                    if (null == entityClassStorage) {
                        throw new MetaSyncClientException(
                            String
                                .format("entityClass id [%d], father entityClass : [%d] missed.", v.getId(), fatherId),
                            BUSINESS_HANDLER_ERROR.ordinal());
                    }
                    v.addAncestors(fatherId);
                    fatherId = entityClassStorage.getFatherId();
                }
                v.getRelations().forEach(
                    relationStorage -> {
                        relationCheck(v.getId(), temp, relationStorage);
                    }
                );
            }
        ).collect(Collectors.toList());
    }

    private static void relationCheck(long id, Map<Long, EntityClassStorage> entityClassStorageMap,
                                      RelationStorage relationStorage) {
        if (relationStorage.getRightEntityClassId() <= 0) {
            throw new MetaSyncClientException(
                String.format("entityClass id [%d], relation entityClassId [%d] should not less than 0.",
                    id, relationStorage.getRightEntityClassId()), BUSINESS_HANDLER_ERROR.ordinal());
        }

        if (null == entityClassStorageMap.get(relationStorage.getRightEntityClassId())) {
            throw new MetaSyncClientException(
                String.format("entityClass id [%d], relation entityClass [%d] missed.",
                    id, relationStorage.getRightEntityClassId()), BUSINESS_HANDLER_ERROR.ordinal());
        }
    }

    /**
     * 转换单个EntityClassStorage.
     */
    private static EntityClassStorage protoValuesToLocalStorage(EntityClassInfo entityClassInfo) {
        if (null == entityClassInfo) {
            throw new MetaSyncClientException("entityClassInfo should not be null.", false);
        }

        /*
         * convert
         */
        EntityClassStorage storage = new EntityClassStorage();

        //  id
        long id = entityClassInfo.getId();
        if (id < MIN_ID) {
            throw new MetaSyncClientException("id is invalid.", false);
        }
        storage.setId(id);
        //  code
        storage.setCode(entityClassInfo.getCode());
        //  name
        storage.setName(entityClassInfo.getName());
        //  level
        storage.setLevel(entityClassInfo.getLevel());
        //  version
        int version = entityClassInfo.getVersion();
        if (version <= NOT_EXIST_VERSION) {
            throw new MetaSyncClientException("version is invalid.", false);
        }
        storage.setVersion(version);
        //  father
        storage.setFatherId(entityClassInfo.getFather());

        //  relations
        storage.setRelations(toRelationStorageList(entityClassInfo.getRelationsList()));

        //  entityFields
        storage.setFields(toEntityFieldList(entityClassInfo.getEntityFieldsList()));

        //  profiles
        Map<String, ProfileStorage> profileStorageMap = new HashMap<>();
        if (!entityClassInfo.getProfilesList().isEmpty()) {
            for (ProfileInfo p : entityClassInfo.getProfilesList()) {
                if (p.getCode().isEmpty()) {
                    throw new MetaSyncClientException("profile code is invalid.", false);
                }

                if (p.getEntityFieldInfoList().isEmpty() && p.getRelationInfoList().isEmpty()) {
                    throw new MetaSyncClientException(
                        String.format("profile [%d-%s] must have at least one element, fieldList/relationList", id,
                            p.getCode()), false);
                }

                List<IEntityField> fieldList = toEntityFieldList(p.getEntityFieldInfoList());
                List<RelationStorage> relationStorageList = toRelationStorageList(p.getRelationInfoList());
                profileStorageMap.put(p.getCode(), new ProfileStorage(p.getCode(), fieldList, relationStorageList));
            }
        }
        storage.setProfileStorageMap(profileStorageMap);

        return storage;
    }

    private static List<IEntityField> toEntityFieldList(List<EntityFieldInfo> entityFieldInfoList) {
        List<IEntityField> fields = new ArrayList<>();
        if (null != entityFieldInfoList) {
            for (EntityFieldInfo e : entityFieldInfoList) {
                EntityField entityField = toEntityField(e);

                fields.add(entityField);
            }
        }

        return fields;
    }

    private static List<RelationStorage> toRelationStorageList(List<RelationInfo> relationInfoList) {
        List<RelationStorage> relations = new ArrayList<>();
        if (null != relationInfoList) {
            for (RelationInfo r : relationInfoList) {
                RelationStorage relation = new RelationStorage();
                relation.setId(r.getId());
                relation.setCode(r.getCode());
                relation.setRightEntityClassId(r.getRightEntityClassId());
                relation.setLeftEntityClassId(r.getLeftEntityClassId());
                relation.setLeftEntityClassCode(r.getLeftEntityClassCode());
                relation.setRelationType(r.getRelationType());
                relation.setIdentity(r.getIdentity());
                if (r.hasEntityField()) {
                    relation.setEntityField(toEntityField(r.getEntityField()));
                }
                relation.setBelongToOwner(r.getBelongToOwner());
                relation.setStrong(r.getStrong());

                relations.add(relation);
            }
        }

        return relations;
    }

    private static EntityField toEntityField(EntityFieldInfo e) {
        long eid = e.getId();
        if (eid < MIN_ID) {
            throw new MetaSyncClientException("entityFieldId is invalid.", false);
        }

        EntityField.Builder builder = EntityField.Builder.anEntityField()
            .withId(eid)
            .withName(e.getName())
            .withCnName(e.getCname())
            .withFieldType(FieldType.fromRawType(e.getFieldType().name()))
            .withDictId(e.getDictId())
            .withDefaultValue(e.getDefaultValue())
            .withConfig(toFieldConfig(e.getFieldConfig()));

        builder.withCalculator(toCalculator(e.getCalculator()));

        return builder.build();
    }

    private static Calculator toCalculator(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        if (null == calculator || !calculator.isInitialized()) {
            throw new MetaSyncClientException("calculator is null or not init.", false);
        }

        CalculateType calculateType = CalculateType.instance(calculator.getCalculateType());
        /*
            初始化
         */

        //  check
        switch (calculateType) {
            case FORMULA: {
                if (calculator.getExpression().isEmpty()) {
                    throw new MetaSyncClientException("calculator [expression] could not be null with type [formula].", false);
                }
                if (calculator.getLevel() < MIN_FORMULA_LEVEL) {
                    throw new MetaSyncClientException(
                        String.format("calculator [level] could not be less than %d with type [formula].", MIN_FORMULA_LEVEL), false);
                }
                break;
            }
            case AUTO_FILL: {
                if (calculator.getPatten().isEmpty()) {
                    throw new MetaSyncClientException("calculator [patten] could not be null with type [autoFill].", false);
                }
                break;
            }
            default:
                break;
        }

        return Calculator.Builder.anCalculator()
            .withCalculateType(calculateType)
            .withExpression(calculator.getExpression())
            .withMax(calculator.getMax())
            .withMin(calculator.getMin())
            .withCondition(calculator.getCondition())
            .withValidator(calculator.getValidator())
            .withEmptyValueTransfer(calculator.getEmptyValueTransfer())
            .withPatten(calculator.getPatten())
            .withModel(calculator.getModel())
            .withStep(calculator.getStep())
            .withLevel(calculator.getLevel())
            .build();
    }

    /**
     * 转换FieldConfig.
     */
    private static FieldConfig toFieldConfig(
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig fieldConfig) {
        return FieldConfig.Builder.anFieldConfig()
            .withSearchable(fieldConfig.getSearchable())
            .withMax(fieldConfig.getMax())
            .withMin(fieldConfig.getMin())
            .withPrecision(fieldConfig.getPrecision())
            .withIdentifie(fieldConfig.getIdentifier())
            .withRequired(fieldConfig.getIsRequired())
            .withValidateRegexString(fieldConfig.getValidateRegexString())
            .withSplittable(false)
            .withDelimiter("")
            .withDisplayType(fieldConfig.getDisplayType())
            .withFieldSense(FieldConfig.FieldSense.getInstance(fieldConfig.getMetaFieldSenseValue()))
            .withFuzzyType(FieldConfig.FuzzyType.getInstance(fieldConfig.getFuzzyType()))
            .withWildcardMinWidth(fieldConfig.getWildcardMinWidth())
            .withWildcardMaxWidth(fieldConfig.getWildcardMaxWidth())
            .withUniqueName(fieldConfig.getUniqueName())
            .build();
    }
}
