package com.xforceplus.ultraman.oqsengine.metadata.utils;

import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_PROFILES;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;

/**
 * 缓存帮助工具.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class CacheUtils {
    private static final String ENTITY_STORAGE_LOCAL_CACHE_KEY = "entityStorageLocal";

    private static final int PROFILE_ENTITY_KEY_PARTS = 4;
    private static final int PROFILE_RELATION_KEY_PARTS = 3;
    private static final int PROFILE_CODE_POS = 2;

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
}
