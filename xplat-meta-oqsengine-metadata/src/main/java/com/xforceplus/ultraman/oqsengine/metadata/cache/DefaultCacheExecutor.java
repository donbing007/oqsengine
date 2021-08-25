package com.xforceplus.ultraman.oqsengine.metadata.cache;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ACTIVE_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_ENTITY;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_ENV;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_PREPARE;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_VERSIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_ENTITY_APP_REL;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ENTITY_CLASS_STORAGE_INFO;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ENTITY_CLASS_STORAGE_INFO_LIST;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.PREPARE_VERSION_SCRIPT;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.REST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.NOT_INIT_INTEGER_PARAMETER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ANCESTORS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_CODE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FATHER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_LEVEL;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_NAME;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateEntityCacheKey;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateProfileEntity;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateProfileRelations;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.redisValuesToLocalStorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AutoFillUpgradePayload;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存执行器.
 *
 * @author : xujia 2021/2/9
 * @since : 1.8
 */
public class DefaultCacheExecutor implements CacheExecutor {

    final Logger logger = LoggerFactory.getLogger(DefaultCacheExecutor.class);

    @Resource(name = "redisClientState")
    private RedisClient redisClient;

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.USE_LONG_FOR_INTS)
        .build();

    private final Cache<String, EntityClassStorage> entityClassStorageCache;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    /*
     * 默认存放在cache中的最大数量为1024
     */
    private int maxCacheSize = 1024;

    /*
     * prepare的超时时间
     */
    private int prepareExpire = 60;

    /*
     * 默认一年过期
     */
    private int cacheExpire = 12 * 30 * 24 * 60 * 60;

    /*
     * script
     */
    /*
     * 写入准备检查
     */
    private String prepareVersionScriptSha;

    /*
     * 写入当前版本、获取当前版本(通过entityClassId获取)
     */
    private String versionResetScriptSha;
    private String versionGetByEntityScriptSha;

    /*
     * 获取当前的EntityClassStorage
     */
    private String entityClassStorageScriptSha;
    private String entityClassStorageListScriptSha;

    /*
     * version key (AppID-Env信息)
     * [redis hash key]
     * key-appEnvKeys
     * field-appId
     * value-env
     */
    private final String appEnvKeys;

    /*
     * version key (版本信息)
     * [redis hash key]
     * key-appVersionKeys
     * field-appId
     * value-version
     */
    private final String appVersionKeys;

    /*
     * prepare key prefix with appId (当前正在进行更新中的Key)
     * [redis key]
     * key-appPrepareKeyPrefix..appId
     * value-version
     */
    private final String appPrepareKeyPrefix;

    /*
     * entityId-appId mapping key (当前的entityId与appId的mapping Key)
     * [redis hash key]
     * key-appEntityMappingKey
     * field-entityId
     * value-appId
     */
    private final String appEntityMappingKey;

    /*
     * all entityIds in one appId with version key (当前的appId + version下所有的entityId列表)
     * [redis hash key]
     * key-appEntityCollectionsKey
     * field-(appId + version)
     * value-appIds
     */
    private final String appEntityCollectionsKey;


    /*
     * entityStorage key prefix (当前的entityStorage key前缀)
     * [redis hash key]
     * key - entityStorageKeys + version + storage.getId()
     * field - entityStroage elements name
     * value - entityStroage elements value
     */
    private final String entityStorageKeys;

    /**
     * 默认实例化.
     */
    public DefaultCacheExecutor() {
        this(NOT_INIT_INTEGER_PARAMETER, NOT_INIT_INTEGER_PARAMETER, NOT_INIT_INTEGER_PARAMETER,
            DEFAULT_METADATA_APP_ENV,
            DEFAULT_METADATA_APP_VERSIONS,
            DEFAULT_METADATA_APP_PREPARE,
            DEFAULT_METADATA_APP_ENTITY,
            DEFAULT_METADATA_ENTITY_APP_REL,
            DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS);
    }

    /**
     * 实例化.
     *
     * @param maxCacheSize         缓存最多元素.
     * @param prepareExpireSeconds 预备的等待超时秒数.
     * @param cacheExpireSeconds   缓存过期的秒数.
     */
    public DefaultCacheExecutor(int maxCacheSize, int prepareExpireSeconds, int cacheExpireSeconds) {
        this(maxCacheSize, prepareExpireSeconds, cacheExpireSeconds,
            DEFAULT_METADATA_APP_ENV,
            DEFAULT_METADATA_APP_VERSIONS,
            DEFAULT_METADATA_APP_PREPARE,
            DEFAULT_METADATA_APP_ENTITY,
            DEFAULT_METADATA_ENTITY_APP_REL,
            DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS);
    }

    /**
     * 实例化.
     *
     * @param maxCacheSize            缓存的最多元素.
     * @param prepareExpireSeconds    预备的等等超时秒数.
     * @param cacheExpireSeconds      缓存过期的秒数.
     * @param appEnvKeys              应用环境KEY.
     * @param appVersionKeys          应用版本KEY.
     * @param appPrepareKeyPrefix     应用预备的KEY.
     * @param entityStorageKeys       元信息储存KEY.
     * @param appEntityMappingKey     元信息属性MAP的KEY.
     * @param appEntityCollectionsKey 应用所有元信息的列表KEY.
     */
    public DefaultCacheExecutor(int maxCacheSize, int prepareExpireSeconds, int cacheExpireSeconds,
                                String appEnvKeys, String appVersionKeys, String appPrepareKeyPrefix,
                                String entityStorageKeys,
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

        this.appEnvKeys = appEnvKeys;
        if (this.appEnvKeys == null || this.appEnvKeys.isEmpty()) {
            throw new IllegalArgumentException("The metadataAppEnv keys is invalid.");
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

        /*
         * prepare
         */
        prepareVersionScriptSha = syncCommands.scriptLoad(PREPARE_VERSION_SCRIPT);

        /*
         * version get/set
         */
        versionGetByEntityScriptSha = syncCommands.scriptLoad(ACTIVE_VERSION);
        versionResetScriptSha = syncCommands.scriptLoad(REST_VERSION);

        /*
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
     * 存储appId级别的所有EntityClassStorage对象.
     */
    @Override
    public boolean save(String appId, int version, List<EntityClassStorage> storageList,
                        List<Event<?>> payLoads) {
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
                        String ancestorStr = OBJECT_MAPPER.writeValueAsString(storage.getAncestors());
                        syncCommands.hset(key, ELEMENT_ANCESTORS, ancestorStr);
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse ancestors failed.", false);
                    }
                }
            }

            //  relations
            if (null != storage.getRelations() && storage.getRelations().size() > 0) {
                try {
                    String relationStr = OBJECT_MAPPER.writeValueAsString(storage.getRelations());
                    syncCommands.hset(key, ELEMENT_RELATIONS, relationStr);
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse relations failed.", false);
                }
            }

            //  fields
            if (null != storage.getFields()) {
                for (IEntityField entityField : storage.getFields()) {
                    try {
                        String entityFieldStr = OBJECT_MAPPER.writeValueAsString(entityField);
                        // TODO avg init.
                        syncCommands.hset(key, ELEMENT_FIELDS + "." + entityField.id(), entityFieldStr);
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse entityField failed.", false);
                    }

                    if (entityField.calculationType().equals(CalculationType.AUTO_FILL)) {
                        AutoFill.DomainNoType domainNoType =
                            ((AutoFill) entityField.config().getCalculation()).getDomainNoType();
                        if (domainNoType.equals(AutoFill.DomainNoType.NORMAL)) {
                            payLoads.add(
                                new ActualEvent<>(EventType.AUTO_FILL_UPGRADE,
                                        new AutoFillUpgradePayload(entityField))
                            );
                        }
                    }
                }
            }

            //  profiles
            if (null != storage.getProfileStorageMap() && !storage.getProfileStorageMap().isEmpty()) {
                for (ProfileStorage ps : storage.getProfileStorageMap().values()) {
                    if (null != ps.getEntityFieldList() && !ps.getEntityFieldList().isEmpty()) {
                        for (IEntityField entityField : ps.getEntityFieldList()) {
                            try {
                                String entityFieldStr = OBJECT_MAPPER.writeValueAsString(entityField);
                                syncCommands
                                    .hset(key, generateProfileEntity(ps.getCode(), entityField.id()), entityFieldStr);
                            } catch (JsonProcessingException e) {
                                throw new MetaSyncClientException("parse profile-entityFields failed.", false);
                            }

                            if (entityField.calculationType().equals(CalculationType.AUTO_FILL)) {
                                payLoads.add(
                                    new ActualEvent<>(EventType.AUTO_FILL_UPGRADE,
                                        new AutoFillUpgradePayload(entityField))
                                );
                            }
                        }
                    }

                    if (null != ps.getRelationStorageList() && !ps.getRelationStorageList().isEmpty()) {
                        try {
                            String relationStr = OBJECT_MAPPER.writeValueAsString(ps.getRelationStorageList());
                            syncCommands.hset(key, generateProfileRelations(ps.getCode()), relationStr);
                        } catch (JsonProcessingException e) {
                            throw new MetaSyncClientException("parse profile-relations failed.", false);
                        }
                    }
                }
            }
        }

        //  reset version
        return resetVersion(appId, version,
            storageList.stream().map(EntityClassStorage::getId).collect(Collectors.toList()));
    }

    /**
     * 读取当前版本entityClassId所对应的EntityClass及所有父对象、子对象.
     */
    @Override
    public Map<Long, EntityClassStorage> read(long entityClassId) throws JsonProcessingException {
        int version = version(entityClassId);
        /*
         * 不存在时抛出异常
         */
        if (NOT_EXIST_VERSION == version) {
            throw new RuntimeException(String.format("invalid entityClassId : [%d], no version pair", entityClassId));
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
                /*
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
     * 根据Ids读取EntityStorage列表.
     */
    @Override
    public Map<Long, EntityClassStorage> multiplyRead(Collection<Long> ids, int version, boolean useLocalCache)
        throws JsonProcessingException {
        Map<Long, EntityClassStorage> entityClassStorageMap = new HashMap<>();
        if (null != ids && ids.size() > 0) {
            List<Long> remoteFilters = new ArrayList<>();

            ids.forEach(
                id -> {
                    if (!useLocalCache) {
                        remoteFilters.add(id);
                    } else {
                        EntityClassStorage entityClassStorage = getFromLocal(id, version);
                        if (null == entityClassStorage) {
                            remoteFilters.add(id);
                        } else {
                            entityClassStorageMap.put(id, entityClassStorage);
                        }
                    }
                }
            );
            remoteMultiplyLoading(remoteFilters, version, entityClassStorageMap);
        }
        return entityClassStorageMap;
    }

    /**
     * 获取当前appId对应的版本信息.
     */
    @Override
    public int version(String appId) {
        String versionStr = syncCommands.hget(appVersionKeys, appId);
        if (null != versionStr) {
            return Integer.parseInt(versionStr);
        }
        return NOT_EXIST_VERSION;
    }


    /**
     * 通过entityClassId获取当前活动版本.
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
     * 重置appId对应的版本信息.
     */
    @Override
    public boolean resetVersion(String appId, int version, List<Long> ids) {
        if (null == appId || appId.isEmpty()) {
            return false;
        }

        if (version < 0) {
            return false;
        }

        /*
         * 这里多出2个位置分别为appId、version
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

        String[] keys = {
            appEntityMappingKey,
            appVersionKeys
        };

        values[0] = appId;
        values[1] = Integer.toString(version);
        /*
         * 设置版本、entity-appId mapping关系
         */
        boolean ret = syncCommands.evalsha(
            versionResetScriptSha,
            ScriptOutputType.BOOLEAN,
            keys, values);

        /*
         * 记录当前appId+version下的所有entityIds，用于过期清理
         */
        if (ret) {
            try {
                String fieldName = String.format("%s.%d", appId, version);
                syncCommands.hset(appEntityCollectionsKey,
                    fieldName, OBJECT_MAPPER.writeValueAsString(ids));
            } catch (Exception e) {
                //  ignore
            }
        }

        return ret;
    }

    /**
     * 锁定当前AppId一分钟，在此期间其他对于该AppId的更新操作将被拒绝.
     */
    @Override
    public boolean prepare(String appId, int version) {
        if (null == appId || appId.isEmpty()) {
            logger.warn("prepare appId is empty.");
            return false;
        }

        if (version < 0) {
            logger.warn("prepare [{}] failed, version [{}] is less than 0", appId, version);
            return false;
        }

        String[] keys = {
            appVersionKeys,
            appPrepareKeyPrefix,
        };
        try {
            return syncCommands.evalsha(
                prepareVersionScriptSha,
                ScriptOutputType.BOOLEAN,
                keys, Integer.toString(version), appId, Integer.toString(prepareExpire));
        } catch (Exception e) {
            logger.warn("do prepare [{}]-[{}] failed, message [{}]", appId, version, e.toString());
            throw e;
        }
    }

    /**
     * 删除当前锁定更新的appId.
     */
    @Override
    public boolean endPrepare(String appId) {
        if (null == appId || appId.isEmpty()) {
            return false;
        }
        try {
            return syncCommands.del(String.format("%s.%s", appPrepareKeyPrefix, appId)) > 0;
        } catch (Exception e) {
            logger.warn("end prepare [{}] failed, message [{}]", appId, e.toString());
            return false;
        }
    }

    @Override
    public String appEnvGet(String appId) {
        return syncCommands.hget(appEnvKeys, appId);
    }

    @Override
    public boolean appEnvSet(String appId, String env) {
        return syncCommands.hsetnx(appEnvKeys, appId, env);
    }

    @Override
    public boolean appEnvRemove(String appId) {
        return syncCommands.hdel(appEnvKeys, appId) == 1;
    }

    /**
     * 删除已经过期AppId版本.
     * 删除只能进行一次，当删除进行中由于某些原因导致redis删除失败时，需要手动进入redis清理过期信息.
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

        Collection<Long> ids = appEntityIdList(appId, version);
        try {
            for (Long id : ids) {
                doClean(id, version);
            }
            //  删除 appId + version 映射的 entityIds列表
            syncCommands.hdel(appEntityCollectionsKey, String.format("%s.%d", appId, version));
        } catch (Exception e) {
            logger.warn("{}", e.toString());
        }

        return true;
    }

    @Override
    public Collection<Long> appEntityIdList(String appId, Integer version) {
        String fieldName = String.format("%s.%d", appId, version);

        //  获取 appId + version 映射的 entityIds列表
        String v = syncCommands.hget(appEntityCollectionsKey, fieldName);

        if (null != v && !v.isEmpty()) {
            try {
                return OBJECT_MAPPER.readValue(v,
                    OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, Long.class));
            } catch (Exception e) {
                logger.warn("{}", e.toString());
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void invalidateLocal() {
        entityClassStorageCache.invalidateAll();
    }

    /**
     * 删除过期版本的EntityClass信息.
     */
    private boolean doClean(Long entityId, int version) {
        if (null == entityId) {
            return false;
        }

        if (version < 0) {
            return false;
        }

        //  删除本地
        invalidateFromLocal(entityId, version);

        //  删除redis
        boolean isDelete = invalidateFromRemote(entityId, version);
        if (!isDelete) {
            logger.warn("delete remote failed, entityId:[{}], version:[{}]", entityId, version);
        }
        return true;
    }

    private boolean invalidateFromRemote(Long entityId, int version) {

        /*
         * 获取当前的Key.
         */
        String keys = String.format("%s.%d.%d", entityStorageKeys, version, entityId);

        try {
            List<String> entityClassKeys = syncCommands.hkeys(keys);
            if (null != entityClassKeys && entityClassKeys.size() > 0) {
                return syncCommands.hdel(keys, entityClassKeys.toArray(new String[entityClassKeys.size()]))
                    == entityClassKeys.size();
            }
            return true;
        } catch (Exception e) {
            logger.warn("delete remote failed, entityId:[{}], version:[{}], message:[{}]", entityId, version,
                e.getMessage());
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
                remoteMultiplyLoading(entityClassStorage.getAncestors(), version, entityClassStorageMap);
            }

            return entityClassStorageMap;
        } catch (JsonProcessingException e) {
            logger.warn("{}", e.toString());
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
            keys, version + "", Long.toString(entityClassId));

        //  get self
        Map<String, String> keyValues = OBJECT_MAPPER.readValue(redisValue, Map.class);

        return redisValuesToLocalStorage(OBJECT_MAPPER, keyValues);
    }

    private void remoteMultiplyLoading(List<Long> ids, int version, Map<Long, EntityClassStorage> entityClassStorageMap)
        throws JsonProcessingException {

        int extraSize = ids.size();
        String[] extraEntityIds = new String[extraSize + 1];
        extraEntityIds[0] = version + "";
        int j = 1;

        /*
         * set ancestors
         */
        for (int i = 0; i < ids.size(); i++) {
            extraEntityIds[j] = Long.toString(ids.get(i));
            j++;
        }

        String[] keys = {
            entityStorageKeys
        };

        String redisValue = syncCommands.evalsha(
            entityClassStorageListScriptSha,
            ScriptOutputType.VALUE,
            keys, extraEntityIds);

        Map<String, Map<String, String>> valuePairs = OBJECT_MAPPER.readValue(redisValue, Map.class);
        /*
         * check size
         */
        if (extraSize != valuePairs.size()) {
            throw new RuntimeException(
                String.format("missed some extend or children entityClassStorage, should be [%d], actual [%d] ",
                    extraSize, valuePairs.size()));
        }

        for (Map.Entry<String, Map<String, String>> value : valuePairs.entrySet()) {
            EntityClassStorage storage =
                redisValuesToLocalStorage(OBJECT_MAPPER, value.getValue());

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


    private void invalidateFromLocal(long entityId, int version) {
        try {
            entityClassStorageCache.invalidate(generateEntityCacheKey(entityId, version));
        } catch (Exception e) {
            logger.warn("delete local failed, entityId:[{}], version:[{}], message:[{}]", entityId, version,
                e.getMessage());
        }
    }
}
