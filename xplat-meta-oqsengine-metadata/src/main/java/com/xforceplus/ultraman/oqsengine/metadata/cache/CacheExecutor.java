package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.*;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.*;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.*;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateEntityCacheKey;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.redisValuesToLocalStorage;

/**
 * desc :
 * name : CacheExecutor
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class CacheExecutor implements ICacheExecutor {

    final Logger logger = LoggerFactory.getLogger(CacheExecutor.class);

    @Resource
    private RedisClient redisClient;

    @Resource
    private ObjectMapper objectMapper;

    private Cache<String, EntityClassStorage> entityClassStorageCache;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    /**
     * 默认存放在cache中的最大数量为1024
     */
    private int maxCacheSize = 1024;

    /**
     * prepare的超时时间
     */
    private int prepareExpire = 60;

    /**
     * 默认一天过期
     */
    private int cacheExpire = 24 * 60 * 60;

    /**
     * script
     */
    /**
     * 写入准备检查
     */
    private String prepareVersionScriptSha;

    /**
     * 写入当前版本、获取当前版本(通过entityClassId获取)
     */
    private String versionResetScriptSha;
    private String versionGetByEntityScriptSha;

    /**
     * 获取当前的EntityClassStorage
     */
    private String entityClassStorageScriptSha;
    private String entityClassStorageListScriptSha;

    /**
     * keys
     */
    /**
     * version key (版本信息)
     * [redis hash key]
     * key-appVersionKeys
     * field-appId
     * value-version
     */
    private String appVersionKeys;

    /**
     * prepare key prefix with appId (当前正在进行更新中的Key)
     * [redis key]
     * key-appPrepareKeyPrefix..appId
     * value-version
     */
    private String appPrepareKeyPrefix;

    /**
     * entityId-appId mapping key (当前的entityId与appId的mapping Key)
     * [redis hash key]
     * key-appEntityMappingKey
     * field-entityId
     * value-appId
     */
    private String appEntityMappingKey;

    /**
     * all entityIds in one appId with version key (当前的appId + version下所有的entityId列表)
     * [redis hash key]
     * key-appEntityCollectionsKey
     * field-(appId + version)
     * value-appIds
     */
    private String appEntityCollectionsKey;


    /**
     * entityStorage key prefix (当前的entityStorage key前缀)
     * [redis hash key]
     * key - entityStorageKeys + version + storage.getId()
     * field - entityStroage elements name
     * value - entityStroage elements value
     */
    private String entityStorageKeys;

    public CacheExecutor() {
        this(NOT_INIT_INTEGER_PARAMETER, NOT_INIT_INTEGER_PARAMETER, NOT_INIT_INTEGER_PARAMETER, DEFAULT_METADATA_APP_VERSIONS,
                DEFAULT_METADATA_APP_PREPARE,
                DEFAULT_METADATA_APP_ENTITY,
                DEFAULT_METADATA_ENTITY_APP_REL,
                DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS);
    }

    public CacheExecutor(int maxCacheSize, int prepareExpireSeconds, int cacheExpireSeconds) {
        this(maxCacheSize, prepareExpireSeconds, cacheExpireSeconds,
                DEFAULT_METADATA_APP_VERSIONS,
                DEFAULT_METADATA_APP_PREPARE,
                DEFAULT_METADATA_APP_ENTITY,
                DEFAULT_METADATA_ENTITY_APP_REL,
                DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS);
    }

    public CacheExecutor(int maxCacheSize, int prepareExpireSeconds, int cacheExpireSeconds,
                         String appVersionKeys, String appPrepareKeyPrefix, String entityStorageKeys,
                                                    String appEntityMappingKey, String appEntityCollectionsKey) {

        if (maxCacheSize > NOT_INIT_INTEGER_PARAMETER) {
            this.maxCacheSize = maxCacheSize;
        }

        if (prepareExpireSeconds > NOT_INIT_INTEGER_PARAMETER) {
            this.prepareExpire = prepareExpireSeconds;
        }

        if (cacheExpireSeconds > NOT_INIT_INTEGER_PARAMETER) {
            this.cacheExpire = cacheExpireSeconds;
        }

        this.appVersionKeys = appVersionKeys;
        if (this.appVersionKeys == null || this.appVersionKeys.isEmpty()) {
            throw new IllegalArgumentException("The metadataVersion keys is invalid.");
        }

        this.appPrepareKeyPrefix = appPrepareKeyPrefix;
        if (this.appPrepareKeyPrefix == null || this.appPrepareKeyPrefix.isEmpty()) {
            throw new IllegalArgumentException("The metadataPrepare key is invalid.");
        }

        this.entityStorageKeys = entityStorageKeys;
        if (this.entityStorageKeys == null || this.entityStorageKeys.isEmpty()) {
            throw new IllegalArgumentException("The metadataAppEntity key is invalid.");
        }

        this.appEntityMappingKey = appEntityMappingKey;
        if (this.appEntityMappingKey == null || this.appEntityMappingKey.isEmpty()) {
            throw new IllegalArgumentException("The metadataAppRel key is invalid.");
        }

        this.appEntityCollectionsKey = appEntityCollectionsKey;
        if (this.appEntityCollectionsKey == null || this.appEntityCollectionsKey.isEmpty()) {
            throw new IllegalArgumentException("The appEntityCollections key is invalid.");
        }

        entityClassStorageCache = initCache();
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public void setCacheExpire(int cacheExpire) {
        this.cacheExpire = cacheExpire;
    }

    public void setPrepareExpire(int prepareExpire) {
        this.prepareExpire = prepareExpire;
    }

    private <V> Cache<String, V> initCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterAccess(cacheExpire, TimeUnit.SECONDS)
                .build();
    }

    @PostConstruct
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.sync.metadata");

        /**
         * prepare
         */
        prepareVersionScriptSha = syncCommands.scriptLoad(PREPARE_VERSION_SCRIPT);

        /**
         * version get/set
         */
        versionGetByEntityScriptSha = syncCommands.scriptLoad(ACTIVE_VERSION);
        versionResetScriptSha = syncCommands.scriptLoad(REST_VERSION);

        /**
         * entityClassStorage(s) get
         */
        entityClassStorageScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO);
        entityClassStorageListScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO_LIST);
    }

    @PreDestroy
    public void destroy() {
        syncConnect.close();
    }

    /**
     * 存储appId级别的所有EntityClassStorage对象
     * @param appId
     * @param version
     * @param storageList
     * @return
     */
    @Override
    public boolean save(String appId, int version, List<EntityClassStorage> storageList) {
        //  set data
        for (EntityClassStorage storage : storageList) {
            String key = entityStorageKeys + "." + version + "." + storage.getId();

            //  basic elements
            syncCommands.hset(key, ELEMENT_ID, Long.toString(storage.getId()));
            syncCommands.hset(key, ELEMENT_CODE, storage.getCode());
            syncCommands.hset(key, ELEMENT_NAME, storage.getName());
            syncCommands.hset(key, ELEMENT_LEVEL, Integer.toString(storage.getLevel()));
            syncCommands.hset(key, ELEMENT_VERSION, Integer.toString(storage.getVersion()));

            //  father & ancestors
            if (null != storage.getFatherId()) {
                syncCommands.hset(key, ELEMENT_FATHER, Long.toString(storage.getFatherId()));
                if (null != storage.getAncestors() && storage.getAncestors().size() > 0) {
                    try {
                        String ancestorStr = objectMapper.writeValueAsString(storage.getAncestors());
                        syncCommands.hset(key, ELEMENT_ANCESTORS, ancestorStr);
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse ancestors failed.", false);
                    }
                }
            }

            //  relations
            if (null != storage.getRelations() && storage.getRelations().size() > 0) {
                try {
                    String relationStr = objectMapper.writeValueAsString(storage.getRelations());
                    syncCommands.hset(key, ELEMENT_RELATIONS, relationStr);
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse children failed.", false);
                }
            }

            //  fields
            if (null != storage.getFields()) {
                for (IEntityField entityField : storage.getFields()) {
                    try {
                        String entityFieldStr = objectMapper.writeValueAsString(entityField);
                        syncCommands.hset(key, ELEMENT_FIELDS + "." + entityField.id(), entityFieldStr);
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse entityField failed.", false);
                    }
                }
            }
        }

        //  reset version
        return resetVersion(appId, version, storageList.stream().map(EntityClassStorage::getId).collect(Collectors.toList()));
    }

    /**
     * 读取当前版本entityClassId所对应的EntityClass及所有父对象、子对象
     * @param entityClassId
     * @return
     */
    @Override
    public Map<Long, EntityClassStorage> read(long entityClassId) throws JsonProcessingException {
        int version = version(entityClassId);
        /**
         * 不存在时抛出异常
         */
        if (NOT_EXIST_VERSION == version) {
            throw new RuntimeException(String.format("invalid entityClassId : [%s], no version pair", entityClassId));
        }

        EntityClassStorage entityClassStorage = getFromLocal(entityClassId, version);
        Map<Long, EntityClassStorage> entityClassStorageMap = null;
        if (null == entityClassStorage) {
            entityClassStorageMap = getFromRemote(entityClassId, version);
            entityClassStorageMap.forEach(
                    (k, v) -> {
                        addToLocal(generateEntityCacheKey(k, version), v);
                    }
            );
        } else {
            entityClassStorageMap = new HashMap<>();
            entityClassStorageMap.put(entityClassStorage.getId(), entityClassStorage);
            List<Long> ids = new ArrayList<>();

            if (null != entityClassStorage.getAncestors()) {
                ids.addAll(entityClassStorage.getAncestors());
            }

            for (Long r : ids) {
                EntityClassStorage e = getFromLocal(r, version);
                /**
                 * 当缓存中没有该EntityClass时,从Redis中获取并写入缓存中
                 */
                if (null == e) {
                    e = getOneFromRemote(r, version);

                    addToLocal(generateEntityCacheKey(r, version), e);
                }

                entityClassStorageMap.put(r, e);
            }
        }

        return entityClassStorageMap;
    }


    /**
     * 获取当前appId对应的版本信息
     * @param appId
     * @return
     */
    @Override
    public int version(String appId) {
        String versionStr = syncCommands.hget(appVersionKeys, appId);
        if (null != versionStr) {
            return Integer.parseInt(versionStr);
        }
        return -1;
    }


    /**
     * 通过entityClassId获取当前活动版本
     * @param entityClassId
     * @return
     */
    @Override
    public int version(Long entityClassId) {
        if (null == entityClassId || entityClassId <= 0) {
            return -1;
        }

        String[] keys = {
                appEntityMappingKey,
                appVersionKeys
        };

        String v = syncCommands.evalsha(
                versionGetByEntityScriptSha,
                ScriptOutputType.VALUE,
                keys, Long.toString(entityClassId));

        return null != v ? Integer.parseInt(v) : NOT_EXIST_VERSION;
    }

    /**
     * 重置appId对应的版本信息
     * @param appId
     * @return
     */
    @Override
    public boolean resetVersion(String appId, int version, List<Long> ids) {
        if (null == appId || appId.isEmpty()) {
            return false;
        }

        if (version < 0) {
            return false;
        }

        String[] keys = {
                appEntityMappingKey,
                appVersionKeys
        };

        /**
         * 这里多出2个位置分别为appId、version
         * 当
         */
        String[] values = null;

        if (null != ids && ids.size() > 0) {
            values = new String[ids.size() + 2];
            int j = 2;
            for (Long id : ids) {
                values[j] = Long.toString(id);
                j++;
            }
        } else {
            values = new String[2];
        }

        values[0] = appId;
        values[1] = Integer.toString(version);
        /**
         * 设置版本、entity-appId mapping关系
         */
        boolean ret = syncCommands.evalsha(
                versionResetScriptSha,
                ScriptOutputType.BOOLEAN,
                keys, values);

        /**
         * 记录当前appId+version下的所有entityIds，用于过期清理
         */
        if (ret) {
            try {
                String fieldName = String.format("%s.%s", appId, version);
                syncCommands.hset(appEntityCollectionsKey,
                        fieldName, objectMapper.writeValueAsString(ids));
            } catch (Exception e) {
                //  ignore
            }
        }

        return ret;
    }

    /**
     * 锁定当前AppId一分钟，在此期间其他对于该AppId的更新操作将被拒绝
     * @param appId
     * @param version
     * @return
     */
    @Override
    public boolean prepare(String appId, int version) {
        if (null == appId || appId.isEmpty()) {
            return false;
        }

        if (version < 0) {
            return false;
        }

        String[] keys = {
                appVersionKeys,
                appPrepareKeyPrefix,
        };

        return syncCommands.evalsha(
                    prepareVersionScriptSha,
                    ScriptOutputType.BOOLEAN,
                    keys, Integer.toString(version), appId, Integer.toString(prepareExpire));
    }

    /**
     * 删除当前锁定更新的appId
     * @param appId
     * @return
     */
    @Override
    public boolean endPrepare(String appId) {
        if (null == appId || appId.isEmpty()) {
            return false;
        }

        return syncCommands.del(String.format("%s.%s", appPrepareKeyPrefix, appId)) > 0;
    }

    /**
     * 删除已经过期AppId版本
     * 删除只能进行一次，当删除进行中由于某些原因导致redis删除失败时，需要手动进入redis清理过期信息
     * @param appId
     * @param version
     * @param force
     * @return
     */
    @Override
    public boolean clean(String appId, int version, boolean force) {
        //check version
        if (!force) {
            int activeVersion = version(appId);
            if (version >= activeVersion) {
                return false;
            }
        }

        String fieldName = String.format("%s.%s", appId, version);

        //  获取 appId + version 映射的 entityIds列表
        String v = syncCommands.hget(appEntityCollectionsKey,
                fieldName);

        if (null != v && !v.isEmpty()) {
            try {
                List<Long> ids = objectMapper.readValue(v,
                        objectMapper.getTypeFactory().constructParametricType(List.class, Long.class));
                for (Long id : ids) {
                    clean(id, version);
                }
                //  删除 appId + version 映射的 entityIds列表
                syncCommands.hdel(appEntityCollectionsKey, fieldName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    /**
     * 删除过期版本的EntityClass信息
     * @param entityId
     * @param version
     * @return
     */
    private boolean clean(Long entityId, int version) {
        if (null == entityId) {
            return false;
        }

        if (version < 0) {
            return false;
        }

        //  删除本地
        delFromLocal(entityId, version);

        //  删除redis
        boolean isDelete = delFromRemote(entityId, version);
        if (!isDelete) {
            logger.warn("delete remote failed, entityId:[{}], version:[{}]", entityId, version);
        }
        return true;
    }

    private boolean delFromRemote(Long entityId, int version) {

        /**
         * 获取当前的Key
         */
        String keys = String.format("%s.%s.%s", entityStorageKeys, Integer.toString(version), Long.toString(entityId));

        try {
            List<String> hKeys = syncCommands.hkeys(keys);
            if (null != hKeys && hKeys.size() > 0) {
                return syncCommands.hdel(keys, hKeys.toArray(new String[hKeys.size()])) == hKeys.size();
            }
            return true;
        } catch (Exception e) {
            logger.warn("delete remote failed, entityId:[{}], version:[{}], message:[{}]", entityId, version, e.getMessage());
            return false;
        }
    }

    private Map<Long, EntityClassStorage> getFromRemote(long entityClassId, int version) {
        try {
            Map<Long, EntityClassStorage> entityClassStorageMap = new HashMap<>();
            //  self
            EntityClassStorage entityClassStorage = getOneFromRemote(entityClassId, version);

            entityClassStorageMap.put(entityClassStorage.getId(), entityClassStorage);

            //  get ancestors
            if (entityClassStorage.getAncestors().size() > 0) {
                remoteAncestorsLoading(entityClassStorage, version, entityClassStorageMap);
            }

            return entityClassStorageMap;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private EntityClassStorage getOneFromRemote(long entityClassId, int version) throws JsonProcessingException {
        String[] keys = {
                entityStorageKeys
        };

        //  get from redis
        String redisValue = syncCommands.evalsha(
                entityClassStorageScriptSha,
                ScriptOutputType.VALUE,
                keys, version + "" , Long.toString(entityClassId));

        //  get self
        Map<String, String> keyValues = objectMapper.readValue(redisValue, Map.class);

        return redisValuesToLocalStorage(objectMapper, keyValues);
    }

    private void remoteAncestorsLoading(EntityClassStorage entityClassStorage, int version, Map<Long, EntityClassStorage> entityClassStorageMap) throws JsonProcessingException {

        int extraSize = entityClassStorage.getAncestors().size();
        String[] extraEntityIds = new String[extraSize + 1];
        extraEntityIds[0] = version + "";
        int j = 1;

        /**
         * set ancestors
         */
        for (int i = 0; i < entityClassStorage.getAncestors().size(); i++) {
            extraEntityIds[j] = Long.toString(entityClassStorage.getAncestors().get(i));
            j++;
        }

        String[] keys = {
                entityStorageKeys
        };

        String redisValue = syncCommands.evalsha(
                entityClassStorageListScriptSha,
                ScriptOutputType.VALUE,
                keys, extraEntityIds);

        Map<String, Map<String, String>> valuePairs = objectMapper.readValue(redisValue, Map.class);
        /**
         * check size
         */
        if (extraSize != valuePairs.size()) {
            throw new RuntimeException(
                    String.format("missed some extend or children entityClassStorage, should be [%d], actual [%d] ", extraSize, valuePairs.size()));
        }

        for (Map.Entry<String, Map<String, String>> value : valuePairs.entrySet()) {
            EntityClassStorage storage =
                    redisValuesToLocalStorage(objectMapper, value.getValue());

            entityClassStorageMap.put(storage.getId(), storage);
        }
    }

    private EntityClassStorage getFromLocal(long entityClassId, int version) {
        return entityClassStorageCache.getIfPresent(generateEntityCacheKey(entityClassId, version));
    }

    private synchronized void addToLocal(String key, EntityClassStorage entityClassStorage) {
        EntityClassStorage e = entityClassStorageCache.getIfPresent(key);
        if (null == e) {
            entityClassStorageCache.put(key, entityClassStorage);
        }
    }

    private void delFromLocal(long entityId, int version) {
        try {
            entityClassStorageCache.invalidate(generateEntityCacheKey(entityId, version));
        } catch (Exception e) {
            logger.warn("delete local failed, entityId:[{}], version:[{}], message:[{}]", entityId, version, e.getMessage());
        }
    }
}
