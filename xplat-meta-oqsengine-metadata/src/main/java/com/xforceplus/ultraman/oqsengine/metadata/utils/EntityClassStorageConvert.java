package com.xforceplus.ultraman.oqsengine.metadata.utils;

import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ANCESTORS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_CODE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FATHER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_LEVEL;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_NAME;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_PROFILES;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.parseOneKeyFromProfileEntity;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.parseOneKeyFromProfileRelations;
import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation.DEFAULT_LEVEL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.RelationStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 元信息序列化为储存JSON格式.
 *
 * @author xujia 2021/2/9
 * @since 1.8
 */
public class EntityClassStorageConvert {

    /**
     * 将redis存储结构转为EntityClassStorage.
     */
    public static EntityClassStorage redisValuesToLocalStorage(ObjectMapper objectMapper, Map<String, String> keyValues)
        throws JsonProcessingException {

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
            List<RelationStorage> relationStorageList = objectMapper.readValue(relations,
                objectMapper.getTypeFactory().constructParametricType(List.class, RelationStorage.class));
            entityClassStorage.setRelations(relationStorageList);
        } else {
            entityClassStorage.setRelations(new ArrayList<>());
        }

        //  entityFields
        List<EntityField> fields = new ArrayList<>();
        //profile
        Map<String, ProfileStorage> profileStorageMap = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = keyValues.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().startsWith(ELEMENT_FIELDS + ".")) {
                fields.add(resetAutoFill(objectMapper.readValue(entry.getValue(), EntityField.class)));
            } else if (entry.getKey().startsWith(ELEMENT_PROFILES + "." +  ELEMENT_FIELDS)) {
                String key = parseOneKeyFromProfileEntity(entry.getKey());
                profileStorageMap.computeIfAbsent(key, ProfileStorage::new)
                    .addField(resetAutoFill(objectMapper.readValue(entry.getValue(), EntityField.class)));
            } else if (entry.getKey().startsWith(ELEMENT_PROFILES + "." +  ELEMENT_RELATIONS)) {
                String key = parseOneKeyFromProfileRelations(entry.getKey());
                String profileRelations = keyValues.get(entry.getKey());
                profileStorageMap.computeIfAbsent(key, ProfileStorage::new)
                    .setRelationStorageList(objectMapper.readValue(profileRelations,
                        objectMapper.getTypeFactory().constructParametricType(List.class, RelationStorage.class)));
            }
        }

        entityClassStorage.setFields(fields);
        entityClassStorage.setProfileStorageMap(profileStorageMap);

        return entityClassStorage;
    }

    /**
     * 为了兼容目前redis中的结构不抛NullPointException，需要对某些自增编号字段设默认值.
     */
    private static EntityField resetAutoFill(EntityField entityField) {
        if (entityField.calculationType().equals(CalculationType.AUTO_FILL)) {
            AutoFill autoFill = (AutoFill) entityField.config().getCalculation();
            if (autoFill.getDomainNoType() == null) {
                autoFill.setDomainNoType(AutoFill.DomainNoType.NORMAL);
            }

            if (autoFill.getLevel() == 0) {
                autoFill.setLevel(DEFAULT_LEVEL);
            }
        }

        return entityField;
    }
}
