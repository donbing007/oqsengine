package com.xforceplus.ultraman.oqsengine.metadata.utils;

/**
 * desc :
 * name : CacheUtils
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class CacheUtils {
    private static final String ENTITY_STORAGE_LOCAL_CACHE_KEY = "entityStorageLocal";
    public static String generateEntityCacheKey(long entityId, int version) {
        return ENTITY_STORAGE_LOCAL_CACHE_KEY + "." + entityId + "." + version;
    }
}
