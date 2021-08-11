package com.xforceplus.ultraman.oqsengine.metadata.utils;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.ProfileInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

                /*
                if (p.getEntityFieldInfoList().isEmpty() && p.getRelationInfoList().isEmpty()) {
                    throw new MetaSyncClientException(
                        String.format("profile [%d-%s] must have at least one element, fieldList/relationList", id,
                            p.getCode()), false);
                }
                */

                List<EntityField> fieldList = new ArrayList<>();
                if (!p.getEntityFieldInfoList().isEmpty()) {
                    fieldList = toEntityFieldList(p.getEntityFieldInfoList());
                }
                List<RelationStorage> relationStorageList = new ArrayList<>();
                if (!p.getRelationInfoList().isEmpty()) {
                    relationStorageList = toRelationStorageList(p.getRelationInfoList());
                }

                profileStorageMap.put(p.getCode(), new ProfileStorage(p.getCode(), fieldList, relationStorageList));
            }
        }
        storage.setProfileStorageMap(profileStorageMap);

        return storage;
    }

    private static List<EntityField> toEntityFieldList(List<EntityFieldInfo> entityFieldInfoList) {
        List<EntityField> fields = new ArrayList<>();
        if (null != entityFieldInfoList) {
            for (EntityFieldInfo e : entityFieldInfoList) {
                EntityField entityField = toEntityField(false, e);

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
                    relation.setEntityField(toEntityField(true, r.getEntityField()));
                }
                relation.setBelongToOwner(r.getBelongToOwner());
                relation.setStrong(r.getStrong());

                relations.add(relation);
            }
        }

        return relations;
    }

    private static EntityField toEntityField(boolean isRelationEntity, EntityFieldInfo e) {
        long eid = e.getId();
        if (eid < MIN_ID) {
            throw new MetaSyncClientException("entityFieldId is invalid.", false);
        }
        FieldType fieldType = FieldType.fromRawType(e.getFieldType().name());

        FieldConfig fieldConfig =
            toFieldConfig(isRelationEntity, fieldType, e.getId(), e.getFieldConfig(), e.getCalculator());

        return EntityField.Builder.anEntityField()
            .withId(eid)
            .withName(e.getName())
            .withCnName(e.getCname())
            .withFieldType(fieldType)
            .withDictId(e.getDictId())
            .withDefaultValue(e.getDefaultValue())
            .withConfig(fieldConfig)
            .build();
    }

    private static AbstractCalculation toCalculator(long fieldId, FieldType fieldType,
                                                      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        CalculationType calculationType = toCalculationType(calculator);

        switch (calculationType) {
            case AUTO_FILL: {
                return toAutoFill(fieldId, calculator);
            }
            case FORMULA: {
                return toFormula(fieldType, calculator);
            }
            case LOOKUP: {
                return toLookup(calculator);
            }
            default: {
                return StaticCalculation.Builder.anStaticCalculation().build();
            }
        }
    }

    private static Lookup toLookup(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        if (calculator.getLookupEntityClassId() < MIN_ID || calculator.getLookupEntityFieldId() < MIN_ID) {
            throw new MetaSyncClientException("lookup [classId] or [fieldId] could not be 0.",
                false);
        }

        return Lookup.Builder.anLookup()
                    .withClassId(calculator.getLookupEntityClassId())
                    .withFieldId(calculator.getLookupEntityFieldId())
                    .build();
    }

    private static AutoFill toAutoFill(long fieldId, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {

        AutoFill.DomainNoType domainNoType = AutoFill.DomainNoType.instance(calculator.getDomainNoSenior());

        //  校验
        //  判断表达式层级逻辑是否允许
        if (calculator.getLevel() < MIN_FORMULA_LEVEL) {
            throw new MetaSyncClientException(
                String.format("fieldId [%d], autoFill [level] could not be less than %d.",
                    fieldId, MIN_FORMULA_LEVEL), false);
        }

        switch (domainNoType) {
            //  普通自增编号
            case NORMAL: {
                if (calculator.getPatten().isEmpty()) {
                    throw new MetaSyncClientException(String.format("fieldId [%d], autoFill [patten] could not be null.", fieldId),
                        false);
                }

                break;
            }
            //  高级自增编号
            case SENIOR: {
                //  判断表达式不能为空
                if (calculator.getExpression().isEmpty()) {
                    throw new MetaSyncClientException(String.format("fieldId [%d], autoFill [expression] could not be null.", fieldId),
                        false);
                }

                break;
            }
            default: {
                throw new MetaSyncClientException(String.format("fieldId [%d], autoFill [domainNoType] should not be null.", fieldId),
                    false);
            }
        }

        return AutoFill.Builder.anAutoFill()
            .withModel(calculator.getModel())
            .withStep(calculator.getStep())
            .withMax(calculator.getMax())
            .withMin(calculator.getMin())
            .withLevel(calculator.getLevel())
            .withPatten(calculator.getPatten())
            .withResetType(calculator.getResetType())
            .withExpression(calculator.getExpression())
            .withArgs(calculator.getArgsList())
            .withDomainNoType(domainNoType)
            .build();
    }

    private static Formula toFormula(FieldType fieldType,
                                     com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        //  判断表达式不能为空
        if (calculator.getExpression().isEmpty()) {
            throw new MetaSyncClientException(
                "formula-[expression] could not be null.",
                false);
        }

        //  判断表达式层级逻辑是否允许
        if (calculator.getLevel() < MIN_FORMULA_LEVEL) {
            throw new MetaSyncClientException(
                String.format("formula [level] could not be less than %d.",
                    MIN_FORMULA_LEVEL), false);
        }

        Optional<?> failedValueOp = Optional.empty();
        List<String> args = new ArrayList<>();

        //  判断失败策略是否为空
        Formula.FailedPolicy policy = Formula.FailedPolicy.instance(calculator.getFailedPolicy());
        if (policy.equals(Formula.FailedPolicy.UNKNOWN)) {
            throw new MetaSyncClientException(
                "calculator [failedPolicy] could not be unknown with type [formula].", false);
        } else if (policy.equals((Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE))) {
            //  当失败策略为RECORD_ERROR_RESUME时,失败默认值不能为空
            if (!calculator.getFailedDefaultValue().isInitialized()) {
                throw new MetaSyncClientException(
                    "calculator [failedDefaultValueCould] could not be null when policy [USE_FAILED_DEFAULT_VALUE] with type [formula].",
                    false);
            }

            //  当失败策略为RECORD_ERROR_RESUME时,需要对默认值进行计算
            try {
                failedValueOp =
                    toFieldTypeValue(fieldType, calculator.getFailedDefaultValue());
            } catch (Exception e) {
                throw new MetaSyncClientException(
                    String.format(
                        "calculator convert failedDefaultValue failed with type [formula], message : %s",
                        e.getMessage()), false);
            }
        }

        //  判断公式所使用的参数
        if (!calculator.getArgsList().isEmpty()) {
            args.addAll(calculator.getArgsList());
        }

        Formula.Builder builder = Formula.Builder.anFormula()
            .withExpression(calculator.getExpression())
            .withLevel(calculator.getLevel())
            .withArgs(args)
            .withFailedPolicy(policy);

        failedValueOp.ifPresent(builder::withFailedDefaultValue);

        return builder.build();
    }


    /**
     * 转换FieldConfig.
     */
    private static FieldConfig toFieldConfig(boolean isRelationEntity, FieldType fieldType,
                                             long fieldId,
                                             com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig fieldConfig,
                                             com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        FieldConfig.Builder builder = FieldConfig.Builder.anFieldConfig()
            .withSearchable(fieldConfig.getSearchable())
            .withMax(fieldConfig.getMax())
            .withMin(fieldConfig.getMin())
            .withPrecision(fieldConfig.getPrecision())
            .withScale(fieldConfig.getValueFloatScale())
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
            .withCrossSearch(fieldConfig.getCrossSearch())
            .withLen(fieldConfig.getLength());

        if (!isRelationEntity) {
            builder.withCalculation(toCalculator(fieldId, fieldType, calculator));
        } else {
            builder.withCalculation(StaticCalculation.Builder.anStaticCalculation().build());
        }

        return builder.build();
    }

    private static CalculationType toCalculationType(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator calculator) {
        try {
            Integer type = calculator.getCalculateType();
            return CalculationType.getInstance(type.byteValue());
        } catch (Exception e) {
            throw new MetaSyncClientException("to calculationType instance failed.",
                false);
        }
    }


    /**
     * 按照FieldType转换成实际的Value值.
     */
    public static Optional<?> toFieldTypeValue(FieldType fieldType, Any any) throws Exception {
        Object value = null;
        if (any.isInitialized()) {
            switch (fieldType) {
                case DATETIME: {
                    value = DateTimeValue.toLocalDateTime(any.unpack(Int64Value.class).getValue());
                    break;
                }
                case LONG: {
                    value = any.unpack(Int64Value.class).getValue();
                    break;
                }
                case DECIMAL: {
                    value = BigDecimal.valueOf(any.unpack(DoubleValue.class).getValue());
                    break;
                }
                case BOOLEAN: {
                    value = any.unpack(BoolValue.class).getValue();
                    break;
                }
                case STRING:
                case ENUM: {
                    value = any.unpack(StringValue.class).getValue();
                    break;
                }
                case STRINGS: {
                    value = StringsValue.toStrings(any.unpack(StringValue.class).getValue());
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                        String.format("un-support type, fieldType : %s, protoTypeUrl : %s", fieldType.getType(),
                            any.getTypeUrl())
                    );
                }
            }
        }

        return Optional.ofNullable(value);
    }
}
