package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.RelationStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;

/**
 * desc :
 * name : EntityClassStorageBuilderUtils
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
public class EntityClassStorageBuilderUtils {

    /**
     * 将protoBuf转为EntityClassStorage列表
     * @param entityClassSyncRspProto
     * @return
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
                                    String.format("entityClass id [%d], father entityClass : [%d] missed.", v.getId(), fatherId)
                                                                            , BUSINESS_HANDLER_ERROR.ordinal());
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

    private static void relationCheck(long id, Map<Long, EntityClassStorage> entityClassStorageMap, RelationStorage relationStorage) {
        if (!(relationStorage.getEntityClassId() > 0)) {
            throw new MetaSyncClientException(
                    String.format("entityClass id [%d], relation entityClassId [%d] should not less than 0."
                                            , id, relationStorage.getEntityClassId()), BUSINESS_HANDLER_ERROR.ordinal());
        }

        if (null == entityClassStorageMap.get(relationStorage.getEntityClassId())) {
            throw new MetaSyncClientException(
                    String.format("entityClass id [%d], relation entityClass [%d] missed."
                            , id, relationStorage.getEntityClassId()), BUSINESS_HANDLER_ERROR.ordinal());
        }
    }

    /**
     * 转换单个EntityClassStorage
     * @param entityClassInfo
     * @return
     */
    private static EntityClassStorage protoValuesToLocalStorage(EntityClassInfo entityClassInfo) {
        if (null == entityClassInfo) {
            throw new MetaSyncClientException("entityClassInfo should not be null.", false);
        }

        /**
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
        List<RelationStorage> relations = new ArrayList<>();
        if (entityClassInfo.getRelationsList() != null) {
            for (RelationInfo r : entityClassInfo.getRelationsList()) {
                RelationStorage relation = new RelationStorage();
                relation.setId(r.getId());
                relation.setName(r.getName());
                relation.setEntityClassId(r.getEntityClassId());
                relation.setRelOwnerClassId(r.getRelOwnerClassId());
                relation.setRelOwnerClassName(r.getRelOwnerClassName());
                relation.setRelationType(r.getRelationType());
                relation.setIdentity(r.getIdentity());
                if (r.hasEntityField()) {
                    relation.setEntityField(toEntityField(r.getEntityField()));
                }
                relation.setBelongToOwner(r.getBelongToOwner());

                relations.add(relation);
            }
        }
        storage.setRelations(relations);

        //  entityFields
        List<IEntityField> fields = new ArrayList<>();
        if (entityClassInfo.getEntityFieldsList() != null) {
            for (EntityFieldInfo e : entityClassInfo.getEntityFieldsList()) {
                EntityField entityField = toEntityField(e);

                fields.add(entityField);
            }
        }
        storage.setFields(fields);

        return storage;
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

        return builder.build();
    }

    /**
     * 转换FieldConfig
     * @param fieldConfig
     * @return
     */
    private static FieldConfig toFieldConfig(com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig fieldConfig) {
        return FieldConfig.Builder.aFieldConfig()
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
                .build();
    }
}