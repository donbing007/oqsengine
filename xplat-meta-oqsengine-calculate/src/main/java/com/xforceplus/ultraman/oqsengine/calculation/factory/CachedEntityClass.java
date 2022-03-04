package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CachedEntityClass {
    private Map<String, IEntityClass> cache = new ConcurrentHashMap<>();

    /**
     * 获取一个EntityClass.(从缓存获取)
     */
    public IEntityClass findEntityClassWithCache(MetaManager metaManager, long id, String profile, int version) {
        String cacheKey = id + "__" + profile;
        IEntityClass entityClass = cache.get(cacheKey);
        if (null == entityClass) {
            //  获取当前version的entityClass
            entityClass = metaManager.load(id, version, profile).orElse(null);
            if (null == entityClass) {
                //  获取当前任意版本的entityClass
                entityClass = metaManager.load(id, profile).orElse(null);
            }

            if (null != entityClass) {
                cache.put(cacheKey, entityClass);
            }
        }

        return entityClass;
    }
}
