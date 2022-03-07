package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CacheContext {
    /**
     * cache key entityClass.version.
     * value key profile.
     * is not have profile, value key will be defaultKey '-'getFromLocal.
     */
    private final Cache<String, Map<String, IEntityClass>> entityClassStorageCache;

    /**
     * cache key entityClass.version.
     * value profile list.
     */
    private final Cache<String, List<String>> profileCache;

    /**
     * version cache.
     */
    private Map<Long, Integer> versions;

    public CacheContext(int maxCacheSize, int cacheExpire) {
        entityClassStorageCache = initCache(maxCacheSize, cacheExpire);
        profileCache = initCache(maxCacheSize, cacheExpire);

        versions = new ConcurrentHashMap<>();
    }

    /**
     * 使缓存失效.
     */
    public void invalidate() {
        entityClassStorageCache.invalidateAll();
        profileCache.invalidateAll();
        versions.clear();
    }

    /**
     * 初始化cache.
     */
    public <V> Cache<String, V> initCache(int maxCacheSize, int cacheExpire) {
        return CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize)
            .expireAfterAccess(cacheExpire, TimeUnit.SECONDS)
            .build();
    }

    public Cache<String, Map<String, IEntityClass>> entityClassStorageCache() {
        return entityClassStorageCache;
    }

    public Cache<String, List<String>> profileCache() {
        return profileCache;
    }

    public Map<Long, Integer> versionCache() {
        return versions;
    }
}
