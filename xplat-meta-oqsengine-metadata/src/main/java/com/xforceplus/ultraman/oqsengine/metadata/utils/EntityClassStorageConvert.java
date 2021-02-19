package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.GeneratedMessageV3;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * ProtoBuffer object to POJO
     */
    public static <T> T fromProtoBuffer(GeneratedMessageV3 pbObject, Class<T> modelClass) {
        T model = null;

        try {
            model = modelClass.newInstance();
            Field[] modelFields = modelClass.getDeclaredFields();
            if (modelFields != null && modelFields.length > 0) {
                for (Field modelField : modelFields) {
                    String fieldName = modelField.getName();
                    String name = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    Class<?> fieldType = modelField.getType();
                    try {
                        Method pbGetMethod = pbObject.getClass().getMethod("get" + name);
                        Object value = pbGetMethod.invoke(pbObject);

                        Method modelSetMethod = modelClass.getMethod("set" + name, fieldType);
                        modelSetMethod.invoke(model, value);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    public static EntityClassStorage valuesToStorage(ObjectMapper objectMapper, Map<String, String> keyValues) throws JsonProcessingException {

        if (0 == keyValues.size()) {
            throw new MetaSyncClientException("entityClassStorage is null, may be delete.", false);
        }

        EntityClassStorage entityClassStorage = new EntityClassStorage();

        //  id
        String id = keyValues.remove(ELEMENT_ID);
        if (null == id || id.isEmpty()) {
            throw new MetaSyncClientException("id is null from cache.", false);
        }
        entityClassStorage.setId(Long.parseLong(id));

        //  code
        String code = keyValues.remove(ELEMENT_CODE);
        if (null == code || code.isEmpty()) {
            throw new MetaSyncClientException("code is null from cache.", false);
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
            throw new MetaSyncClientException("level is null from cache.", false);
        }
        entityClassStorage.setLevel(Integer.parseInt(level));

        //  version
        String version = keyValues.remove(ELEMENT_VERSION);
        if (null == version || version.isEmpty()) {
            throw new MetaSyncClientException("version is null from cache.", false);
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
       for(Map.Entry<String, String> entry : keyValues.entrySet()) {
           if (entry.getKey().startsWith(ELEMENT_FIELDS + ".")) {
               fields.add(objectMapper.readValue(entry.getValue(), EntityField.class));
           }
       }
       entityClassStorage.setFields(fields);

       return entityClassStorage;
    }
}
