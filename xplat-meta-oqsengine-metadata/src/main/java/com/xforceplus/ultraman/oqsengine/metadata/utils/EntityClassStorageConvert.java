package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.*;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.*;

/**
 * desc :
 * name : EntityClassStorageConvert
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class EntityClassStorageConvert {

    /**
     * 将redis存储结构转为EntityClassStorage
     * @param objectMapper
     * @param keyValues
     * @return
     * @throws JsonProcessingException
     */
    public static EntityClassStorage redisValuesToLocalStorage(ObjectMapper objectMapper, Map<String, String> keyValues) throws JsonProcessingException {

        if (0 == keyValues.size()) {
            throw new RuntimeException("entityClassStorage is null, may be delete.");
        }

        EntityClassStorage entityClassStorage = new EntityClassStorage();

        //  id
        String id = keyValues.remove(ELEMENT_ID);
        if (null == id || id.isEmpty()) {
            throw new RuntimeException("id is null from cache.");
        }
        entityClassStorage.setId(Long.parseLong(id));

        //  code
        String code = keyValues.remove(ELEMENT_CODE);
        if (null == code || code.isEmpty()) {
            throw new RuntimeException("code is null from cache.");
        }
        entityClassStorage.setCode(code);

        //  name
        String name = keyValues.remove(ELEMENT_NAME);
        if (null != name && !name.isEmpty()) {
            entityClassStorage.setName(name);
        }

        //  level
        String level = keyValues.remove(ELEMENT_LEVEL);
        if (null == level || level.isEmpty()) {
            throw new RuntimeException("level is null from cache.");
        }
        entityClassStorage.setLevel(Integer.parseInt(level));

        //  version
        String version = keyValues.remove(ELEMENT_VERSION);
        if (null == version || version.isEmpty()) {
            throw new RuntimeException("version is null from cache.");
        }
        entityClassStorage.setVersion(Integer.parseInt(version));

        //  father
        String father = keyValues.remove(ELEMENT_FATHER);
        if (null == father || father.isEmpty()) {
            father = "0";
        }
        entityClassStorage.setFatherId(Long.parseLong(father));

        //  ancestors
        String ancestors = keyValues.remove(ELEMENT_ANCESTORS);
        if (null != ancestors && !ancestors.isEmpty()) {
            entityClassStorage.setAncestors(objectMapper.readValue(ancestors,
                    objectMapper.getTypeFactory().constructParametricType(List.class, Long.class)));
        } else {
            entityClassStorage.setAncestors(new ArrayList<>());
        }

        //  relations
        String relations = keyValues.remove(ELEMENT_RELATIONS);
        if (null != relations && !relations.isEmpty()) {
            List<OqsRelation> relationStorageList = objectMapper.readValue(relations,
                    objectMapper.getTypeFactory().constructParametricType(List.class, OqsRelation.class));
            entityClassStorage.setRelations(relationStorageList);
        } else {
            entityClassStorage.setRelations(new ArrayList<>());
        }

        //  entityFields
        List<IEntityField> fields = new ArrayList<>();
        for (Map.Entry<String, String> entry : keyValues.entrySet()) {
            if (entry.getKey().startsWith(ELEMENT_FIELDS + ".")) {
                fields.add(objectMapper.readValue(entry.getValue(), EntityField.class));
            }
        }
        entityClassStorage.setFields(fields);

        return entityClassStorage;
    }

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
        ).collect(Collectors.toMap(EntityClassStorage::getId, s1 -> s1,  (s1, s2) -> s1));

        return temp.values().stream().peek(
                v -> {
                    Long fatherId = v.getFatherId();
                    while (null != fatherId && fatherId >= MIN_ID) {
                        EntityClassStorage entityClassStorage = temp.get(fatherId);
                        if (null == entityClassStorage) {
                            throw new MetaSyncClientException(
                                    String.format("father entityClass : [%d] missed.", fatherId), BUSINESS_HANDLER_ERROR.ordinal());
                        }
                        v.addAncestors(fatherId);
                        fatherId = entityClassStorage.getFatherId();
                    }
                }
        ).collect(Collectors.toList());
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
        List<OqsRelation> relations = new ArrayList<>();
        if (entityClassInfo.getRelationsList() != null) {
            for (RelationInfo r : entityClassInfo.getRelationsList()) {
                OqsRelation oqsRelation = new OqsRelation();
                oqsRelation.setId(r.getId());
                oqsRelation.setName(r.getName());
                oqsRelation.setEntityClassId(r.getEntityClassId());
                oqsRelation.setEntityClassName(r.getEntityClassName());
                oqsRelation.setRelOwnerClassId(r.getRelOwnerClassId());
                oqsRelation.setRelOwnerClassName(r.getRelOwnerClassName());
                oqsRelation.setRelationType(r.getRelationType());
                oqsRelation.setIdentity(r.getIdentity());
                oqsRelation.setEntityFieldId(r.getEntityFieldId());

                relations.add(oqsRelation);
            }
        }
        storage.setRelations(relations);

        //  entityFields
        List<IEntityField> fields = new ArrayList<>();
        if (entityClassInfo.getEntityFieldsList() != null) {
            for (EntityFieldInfo e : entityClassInfo.getEntityFieldsList()) {
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

                fields.add(builder.build());
            }
        }
        storage.setFields(fields);

        return storage;
    }

    /**
     * 转换FieldConfig
     * @param fieldConfig
     * @return
     */
    private static FieldConfig toFieldConfig(com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig fieldConfig) {
        return FieldConfig.Builder.anFieldConfig()
                .withSearchable(fieldConfig.getSearchable())
                .withMax(fieldConfig.getMax())
                .withMin(fieldConfig.getMin())
                .withPrecision(fieldConfig.getPrecision())
                .withIdentifie(fieldConfig.getIdentifier())
                .withRequired(fieldConfig.getIsRequired())
                .withValidateRegexString(fieldConfig.getValidateRegexString())
                .withSplittable(fieldConfig.getIsSplittable())
                .withDelimiter(fieldConfig.getDelimiter())
                .withDisplayType(fieldConfig.getDisplayType())
                .withFieldSense(FieldConfig.FieldSense.getInstance(fieldConfig.getMetaFieldSenseValue()))
                .build();
    }
}
