package com.xforceplus.ultraman.oqsengine.metadata.utils;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor.OBJECT_MAPPER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_PROFILES;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;
import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation.DEFAULT_LEVEL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存帮助工具.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class CacheUtils {
    private static final String ENTITY_STORAGE_LOCAL_CACHE_KEY = "entityStorageLocal";
    private static final String ENTITY_STORAGE_LOCAL_CACHE_INTERNAL_KEY = ".";

    private static final int PROFILE_ENTITY_KEY_PARTS = 4;
    private static final int PROFILE_RELATION_KEY_PARTS = 3;
    private static final int PROFILE_CODE_POS = 2;

    /**
     * 检查业务ID是否合法(EntityClassId, FieldId, RelationId, FatherId)等.
     */
    public static boolean validBusinessId(String id) {
        try {
            return null != id && !id.isEmpty() && Long.parseLong(id) >= MIN_ID;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查业务ID是否合法(EntityClassId, FieldId, RelationId, FatherId)等.
     */
    public static boolean validBusinessId(Long id) {
        return null != id && id >= MIN_ID;
    }

    /**
     * 生成Internal-KEY.
     */
    public static String generateEntityCacheInternalKey(String profile) {
        return ENTITY_STORAGE_LOCAL_CACHE_INTERNAL_KEY + (null == profile ? "" : profile);
    }

    /**
     * 生成KEY.
     */
    public static String generateEntityCacheKey(long entityId, int version) {
        return ENTITY_STORAGE_LOCAL_CACHE_KEY + "." + entityId + "." + version;
    }

    /**
     * 生成ProfileEntity.
     */
    public static String generateProfileEntity(String code, long id) {
        return ELEMENT_PROFILES + "." + ELEMENT_FIELDS + "." + code + "." + id;
    }

    /**
     * parseOneKey.
     */
    public static String parseOneKeyFromProfileEntity(String key) {
        String[] parts = key.split("\\.");
        if (parts.length != PROFILE_ENTITY_KEY_PARTS) {
            throw new MetaSyncClientException(
                String.format("profileEntity key's length should be %d", PROFILE_ENTITY_KEY_PARTS), false);
        }

        return parts[PROFILE_CODE_POS];
    }

    /**
     * 生成ProfileRelations.
     */
    public static String generateProfileRelations(String code) {
        return ELEMENT_PROFILES + "." + ELEMENT_RELATIONS + "." + code;
    }

    /**
     * parseOneKey.
     */
    public static String parseOneKeyFromProfileRelations(String key) {
        String[] parts = key.split("\\.");
        if (parts.length != PROFILE_RELATION_KEY_PARTS) {
            throw new MetaSyncClientException(
                String.format("profileRelations key's length should be %d", PROFILE_RELATION_KEY_PARTS), false);
        }

        return parts[PROFILE_CODE_POS];
    }

    /**
     * 为了兼容目前redis中的结构不抛NullPointException，需要对某些自增编号字段设默认值.
     */
    public static EntityField resetCalculation(EntityField entityField, int version, CacheExecutor cacheExecutor)
        throws JsonProcessingException {
        if (null != entityField.calculationType()) {
            if (entityField.calculationType().equals(CalculationType.AUTO_FILL)) {
                AutoFill autoFill = (AutoFill) entityField.config().getCalculation();
                if (autoFill.getDomainNoType() == null) {
                    autoFill.setDomainNoType(AutoFill.DomainNoType.NORMAL);
                }

                if (autoFill.getLevel() == 0) {
                    autoFill.setLevel(DEFAULT_LEVEL);
                }
            } else if (entityField.calculationType().equals(CalculationType.AGGREGATION)) {
                Aggregation aggregation = (Aggregation) entityField.config().getCalculation();
                if (null != cacheExecutor && version != NOT_EXIST_VERSION) {
                    aggregationConditionsToConditions(aggregation, version, cacheExecutor);
                }
            }
        } else {
            entityField.config().resetCalculation(StaticCalculation.Builder.anStaticCalculation().build());
        }

        return entityField;
    }

    /**
     * 解析profileCode.
     */
    public static List<String> parseProfileCodes(Map<String, String> keyValues) {

        if (null != keyValues && !keyValues.isEmpty()) {
            Set<String> profiles = new HashSet<>();
            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                if (entry.getKey().startsWith(ELEMENT_PROFILES + "." + ELEMENT_FIELDS)) {
                    profiles.add(parseOneKeyFromProfileEntity(entry.getKey()));
                } else if (entry.getKey().startsWith(ELEMENT_PROFILES + "." + ELEMENT_RELATIONS)) {
                    profiles.add(parseOneKeyFromProfileRelations(entry.getKey()));
                }
            }

            return new ArrayList<>(profiles);
        }

        return Collections.emptyList();
    }



    private static void aggregationConditionsToConditions(Aggregation aggregation
                                                , int version, CacheExecutor cacheExecutor)
        throws JsonProcessingException {
        if (null != aggregation.getAggregationConditions() && !aggregation.getAggregationConditions().isEmpty()) {
            Conditions conditions = Conditions.buildEmtpyConditions();

            for (Aggregation.AggregationCondition aggregationCondition : aggregation.getAggregationConditions()) {


                String fieldStr = cacheExecutor.remoteFieldLoad(aggregationCondition.getEntityClassId(),
                                        aggregationCondition.getEntityFieldId(), aggregationCondition.getProfile(), version) ;

                if (null == fieldStr) {
                    return;
                }

                IEntityField entityField =
                    OBJECT_MAPPER.readValue(fieldStr, EntityField.class);

                conditions.addAnd(
                    new Condition(entityField, aggregationCondition.getConditionOperator(),
                        IValueUtils.deserialize(aggregationCondition.getStringValue(), entityField))
                );
            }
            aggregation.setConditions(conditions);
        }
    }
}
