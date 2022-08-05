package com.xforceplus.ultraman.oqsengine.metadata.cache;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ACTIVE_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_CODE;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_ENTITY;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_ENV;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_PREPARE;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_VERSIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_ENTITY_APP_REL;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.DEFAULT_METADATA_UPGRADE_LOG;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ENTITY_CLASS_STORAGE_INFO;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.ENTITY_CLASS_STORAGE_INFO_LIST;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.PREPARE_VERSION_SCRIPT;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.REST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.NOT_INIT_INTEGER_PARAMETER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ANCESTORS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_APPCODE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_CODE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FATHER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_LEVEL;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_NAME;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_TYPE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateEntityCacheInternalKey;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateEntityCacheKey;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateProfileEntity;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.generateProfileRelations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.xforceplus.ultraman.oqsengine.common.thread.PollingThreadExecutor;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import com.xforceplus.ultraman.oqsengine.event.payload.meta.MetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.log.UpGradeLog;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.AppSimpleInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils;
import com.xforceplus.ultraman.oqsengine.metadata.utils.storage.CacheToStorageGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Resource
    private RedisLuaScriptWatchDog redisLuaScriptWatchDog;

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.USE_LONG_FOR_INTS)
        .build();


    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    private CacheContext cacheContext;

    private int maxWait = 10;
    private int versionCacheRefreshDuration = 10;
    private PollingThreadExecutor<Void> lifeCycleThread;

    /*
     * prepare的超时时间
     */
    private int prepareExpire = 60;

    /*
     * 默认7天过期
     */
    private int logExpire = 7;


    /*
     * 默认存放在cache中的最大数量为1024
     */
    private int maxCacheSize = 1024;

    /*
     * fixed固定1天
     */
    private int fixedDayExpire = 24 * 60 * 60;

    /*
     * 默认一年过期
     */
    private int cacheExpire = 12 * 30 * fixedDayExpire;

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
     * version key (AppID-Code信息)
     * [redis hash key]
     * key-appCodeKeys
     * field-appId
     * value-code
     */
    private final String appCodeKeys;


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


    /*
     * upGradeLogKey key prefix (当前的entityStorage key前缀)
     * [redis hash key]
     * key - upGradeLogKey
     * field - appId + env
     * value - upGradeValue
     */
    private final String upGradeLogKey;

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
            DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS,
            DEFAULT_METADATA_APP_CODE,
            DEFAULT_METADATA_UPGRADE_LOG);
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
            DEFAULT_METADATA_APP_VERSIONS_ENTITY_IDS,
            DEFAULT_METADATA_APP_CODE,
            DEFAULT_METADATA_UPGRADE_LOG);
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
     * @param appCodeKeys             应用CODE的KEY.
     */
    public DefaultCacheExecutor(int maxCacheSize, int prepareExpireSeconds, int cacheExpireSeconds,
                                String appEnvKeys, String appVersionKeys, String appPrepareKeyPrefix,
                                String entityStorageKeys,
                                String appEntityMappingKey, String appEntityCollectionsKey, String appCodeKeys, String upGradeLogKey) {

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

        this.appCodeKeys = appCodeKeys;
        if (this.appCodeKeys == null || this.appCodeKeys.isEmpty()) {
            throw new IllegalArgumentException("The appCodeKeys keys is invalid.");
        }

        this.upGradeLogKey = upGradeLogKey;
        if (this.upGradeLogKey == null || this.upGradeLogKey.isEmpty()) {
            throw new IllegalArgumentException("The upGradeLogKey keys is invalid.");
        }

        cacheContext = new CacheContext(this.maxCacheSize,  this.cacheExpire);
    }

    @PostConstruct
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.sync.metadata");

        if (redisLuaScriptWatchDog != null) {

            prepareVersionScriptSha = redisLuaScriptWatchDog.watch(PREPARE_VERSION_SCRIPT);
            versionGetByEntityScriptSha = redisLuaScriptWatchDog.watch(ACTIVE_VERSION);
            versionResetScriptSha = redisLuaScriptWatchDog.watch(REST_VERSION);
            entityClassStorageScriptSha = redisLuaScriptWatchDog.watch(ENTITY_CLASS_STORAGE_INFO);
            entityClassStorageListScriptSha = redisLuaScriptWatchDog.watch(ENTITY_CLASS_STORAGE_INFO_LIST);

        } else {

            prepareVersionScriptSha = syncCommands.scriptLoad(PREPARE_VERSION_SCRIPT);
            versionGetByEntityScriptSha = syncCommands.scriptLoad(ACTIVE_VERSION);
            versionResetScriptSha = syncCommands.scriptLoad(REST_VERSION);
            entityClassStorageScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO);
            entityClassStorageListScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO_LIST);
        }

        lifeCycleThread = new PollingThreadExecutor(
            "metaVersionCached",
            versionCacheRefreshDuration,
            TimeUnit.SECONDS, maxWait,
            (n) -> cachedVersion(),
            null);

        lifeCycleThread.start();
    }

    @PreDestroy
    public void destroy() {
        syncConnect.close();
        lifeCycleThread.stop();
    }

    /**
     * 存储appId级别的所有EntityClassStorage对象.
     */
    @Override
    public MetaChangePayLoad save(String appId, String env, int version, List<EntityClassStorage> storageList)
        throws JsonProcessingException {

        MetaChangePayLoad appMetaChangePayLoad = new MetaChangePayLoad(appId, version);

        //  获取旧版本.
        int oldVersion = version(appId);
        Map<Long, EntityClassStorage> oldMetas = null;
        //  当存在旧版本时，获取旧版本信息.
        if (oldVersion > NOT_EXIST_VERSION) {
            oldMetas = CacheToStorageGenerator
                .toEntityClassStorages(version,
                    remoteMultiplyLoading(appEntityIdList(appId, oldVersion), oldVersion));
        }

        String appCode = "";

        //  set data
        for (EntityClassStorage newStorage : storageList) {

            appCode = newStorage.getAppCode();

            //  存入到cache中，并获得entityClass的变更事件
            EntityClassStorage old = null != oldMetas ? oldMetas.remove(newStorage.getId()) : null;
            MetaChangePayLoad.EntityChange entityChange =
                saveToCache(toCacheSetKey(version, newStorage.getId()), old, newStorage);

            if (null != entityChange) {
                appMetaChangePayLoad.getEntityChanges().add(entityChange);
            }
        }

        //  还剩余的oldMetas表明有删除的EntityClass,需要反查一次oldMeta确定删除事件
        if (null != oldMetas) {
            oldMetas.forEach(
                (k, v) -> {
                    MetaChangePayLoad.EntityChange entityChange = new MetaChangePayLoad.EntityChange(k);
                    reverseEventCheck(v, null, null, entityChange);

                    appMetaChangePayLoad.getEntityChanges().add(entityChange);
                }
            );
        }

        //  reset version
        if (!resetVersion(appId, version,
            storageList.stream().map(EntityClassStorage::getId).collect(Collectors.toList()))) {
            throw new RuntimeException(String.format("reset version failed, appId : %s, %d", appId, version));
        }

        if (!appCode.isEmpty()) {
            saveAppCode(appId, appCode);
        }

        addUpGradeLog(appId, env, version);

        return appMetaChangePayLoad;
    }

    /**
     * 读取当前版本entityClassId所对应的EntityClass及所有父对象、子对象.
     */
    @Override
    public Map<String, String> remoteRead(long entityClassId) throws JsonProcessingException {
        //  这里是一次IO操作REDIS获取当前的版本, 并组装结构
        int version = version(entityClassId, false);

        return remoteRead(entityClassId, version);
    }

    /**
     * 读取当前版本entityClassId, version所对应的EntityClass及所有父对象、子对象.
     */
    @Override
    public Map<String, String> remoteRead(long entityClassId, int version) throws JsonProcessingException {
        String[] keys = {
            entityStorageKeys
        };

        //  get from redis
        String redisValue = syncCommands.evalsha(
            entityClassStorageScriptSha,
            ScriptOutputType.VALUE,
            keys, version + "", Long.toString(entityClassId));

        //  get self
        return OBJECT_MAPPER.readValue(redisValue, Map.class);
    }


    /**
     * 根据Ids读取EntityStorage列表.
     */
    @Override
    public Map<String, Map<String, String>> multiRemoteRead(Collection<Long> ids, int version)
        throws JsonProcessingException {
        if (null != ids && ids.size() > 0) {
            return remoteMultiplyLoading(ids, version);
        }
        return null;
    }

    @Override
    public String remoteFieldLoad(long entityClassId, long entityFieldId, String profile, int version)
        throws JsonProcessingException {
        String[] keys = {
            entityStorageKeys
        };

        //  get from redis
        String redisValue = syncCommands.evalsha(
            entityClassStorageScriptSha,
            ScriptOutputType.VALUE,
            keys, version + "", Long.toString(entityClassId));

        //  读取entityClass信息
        Map<String, String> storageValues = OBJECT_MAPPER.readValue(redisValue, Map.class);
        if (null == storageValues) {
            return null;
        }

        //  从field中找.
        String value = storageValues.get(ELEMENT_FIELDS + "." + entityFieldId);
        if (null != value && !value.isEmpty()) {
            return value;
        }

        //  从profile中找.
        value = storageValues.get(generateProfileEntity(profile, entityFieldId));
        if (null != value && !value.isEmpty()) {
            return value;
        }

        //  递归从父类中找.
        String fatherIdString = storageValues.get(ELEMENT_FATHER);
        if (null != value && !value.isEmpty()) {
            entityClassId = Long.parseLong(fatherIdString);

            return remoteFieldLoad(entityClassId, entityFieldId, profile, version);
        }
        return null;
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
    public int version(Long entityClassId, boolean withCache) {

        if (null == entityClassId || entityClassId <= 0) {
            return -1;
        }

        if (withCache) {
            Integer v = cacheContext.versionCache().get(entityClassId);
            if (null != v) {
                return v;
            }
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

    @Override
    public Map<Long, Integer> versions(List<Long> entityClassIds, boolean withCache, boolean errorContinue) {

        Map<Long, Integer> vs = new HashMap<>();

        List<Long> notFoundIds;
        if (withCache) {
            notFoundIds = new ArrayList<>();
            //  从缓存读取.
            entityClassIds.forEach(
                id -> {
                    Integer v = cacheContext.versionCache().get(id);
                    if (null != v && v > NOT_EXIST_VERSION) {
                        vs.put(id, v);
                    } else {
                        notFoundIds.add(id);
                    }
                }
            );
        } else {
            notFoundIds = entityClassIds;
        }

        //  如果都读到则直接退出.
        if (notFoundIds.isEmpty()) {
            return vs;
        }

        //  获取所有entity->app mapping
        Map<String, String> entityAppRelations = syncCommands.hgetall(appEntityMappingKey);
        //  获取所有的app->version mapping
        Map<String, String> appVersionRelations = syncCommands.hgetall(appVersionKeys);

        String error = "";

        if (null != appVersionRelations && !appVersionRelations.isEmpty()
                && null != entityAppRelations && !entityAppRelations.isEmpty()) {

            for (Long entityClassId : notFoundIds) {
                String appId = entityAppRelations.get(String.valueOf(entityClassId));

                if (null != appId) {
                    String version = appVersionRelations.get(appId);
                    if (null != version) {
                        vs.put(entityClassId, Integer.parseInt(version));
                    } else {
                        error = String.format("version not found, appId : %s failed, entityClassId : %s", appId, entityClassId);
                    }
                } else {
                    error = String.format("appId not found, entityClassId : %s", entityClassId);
                }

                if (!error.isEmpty()) {
                    //  存在错误且继续标志为false
                    if (!errorContinue) {
                        break;
                    }
                    logger.warn(error);
                    error = "";
                }
            }
        } else {
            error = "query entityClassIds->versions failed, no mapping in cache.";
            logger.warn(error);
        }

        if (!error.isEmpty() && !errorContinue) {
            throw new RuntimeException(error);
        }

        return vs;
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

    @Override
    public void invalidateLocal() {
        cacheContext.invalidate();
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
    public Optional<IEntityClass> localRead(long entityClassId, int version, String profile) {
        Map<String, IEntityClass> entityClassMap =
            cacheContext.entityClassStorageCache().getIfPresent(generateEntityCacheKey(entityClassId, version));

        if (null != entityClassMap) {
            return Optional.ofNullable(entityClassMap.get(generateEntityCacheInternalKey(profile)));
        }
        return Optional.empty();
    }

    @Override
    public List<String> readProfileCodes(long entityClassId, int version) {
        String key = generateEntityCacheKey(entityClassId, version);
        //  从本地cache读取
        List<String> profiles = cacheContext.profileCache().getIfPresent(key);
        if (null == profiles) {
            try {
                profiles = CacheUtils.parseProfileCodes(remoteRead(entityClassId, version));
                //  从remoteCache读取,并写入本地cache
                cacheContext.profileCache().put(key, profiles);
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format("entityId : %d, version : %d, read profiles failed, message : %s",
                        entityClassId, version, e.getMessage())
                );
            }
        }
        return profiles;
    }

    @Override
    public void localStorage(long entityClassId, int version, String profile, IEntityClass entityClass) {
        String key = generateEntityCacheKey(entityClassId, version);
        Map<String, IEntityClass> e = cacheContext.entityClassStorageCache().getIfPresent(key);
        if (null == e) {
            Map<String, IEntityClass> internals = new HashMap<>();
            internals.put(generateEntityCacheInternalKey(profile), entityClass);
            cacheContext.entityClassStorageCache().put(key, internals);
        } else {
            e.putIfAbsent(generateEntityCacheInternalKey(profile), entityClass);
        }
    }

    @Override
    public List<AppSimpleInfo> showAppInfo() {
        List<AppSimpleInfo> infoList = new ArrayList<>();
        Map<String, String> envs =  syncCommands.hgetall(appEnvKeys);
        if (null != envs && !envs.isEmpty()) {
            Map<String, String> versions = syncCommands.hgetall(appVersionKeys);
            Map<String, String> codes = syncCommands.hgetall(appCodeKeys);
            envs.forEach(
                (appId, env) -> {
                    String version = null;
                    if (null != versions && !versions.isEmpty()) {
                        version = versions.remove(appId);
                    }

                    String code = null;
                    if (null != codes && !codes.isEmpty()) {
                        code = codes.remove(appId);
                    }

                    infoList.add(new AppSimpleInfo(appId, env, null != code ? code : "",
                        null != version ? Integer.parseInt(version) : NOT_EXIST_VERSION));
                }
            );
        }
        return infoList;
    }

    @Override
    public Collection<UpGradeLog> showUpgradeLogs(String appId, String env) throws JsonProcessingException {
        List<UpGradeLog> upGradeLogs = new ArrayList<>();
        if (null != appId && !appId.isEmpty() && null != env && !env.isEmpty()) {
            String fieldKey = String.format("%s.%s", appId, env);
            UpGradeLog upGradeLog = getUpgradeLog(fieldKey);
            if (null != upGradeLog) {
                upGradeLogs.add(upGradeLog);
            }
        } else {
            Map<String, String> vs = syncCommands.hgetall(upGradeLogKey);
            if (null != vs) {
                for (String v : vs.values()) {
                    upGradeLogs.add(OBJECT_MAPPER.readValue(v, UpGradeLog.class));
                }
            }
        }
        return upGradeLogs;
    }


    @Override
    public List<String> appEntityClassIds(String appId) {
        List<String> entityClassIds = new ArrayList<>();

        Map<String, String> entityApps = syncCommands.hgetall(appEntityMappingKey);
        if (null != entityApps) {
            entityApps.forEach(
                (entityClassId, app) -> {
                    if (null == appId || appId.equals(app)) {
                        entityClassIds.add(entityClassId);
                    }
                }
            );
        }

        return entityClassIds;
    }

    private UpGradeLog getUpgradeLog(String fieldKey) throws JsonProcessingException {
        String v = syncCommands.hget(upGradeLogKey, fieldKey);
        if (null != v) {
            return OBJECT_MAPPER.readValue(v, UpGradeLog.class);
        }
        return null;
    }

    private void addUpGradeLog(String appId, String env, int currentVersion) {
        try {
            String fieldKey = String.format("%s.%s", appId, env);
            UpGradeLog upGradeLog = getUpgradeLog(fieldKey);
            if (null == upGradeLog) {
                long time = System.currentTimeMillis();
                upGradeLog = new UpGradeLog(appId, env, currentVersion, time, currentVersion, time);
            } else {
                upGradeLog.setCurrentVersion(currentVersion);
                upGradeLog.setCurrentTimeStamp(System.currentTimeMillis());
            }

            String finalValue = OBJECT_MAPPER.writeValueAsString(upGradeLog);
            syncCommands.hset(upGradeLogKey, fieldKey, finalValue);
        } catch (Exception e) {
            logger.warn("add upgrade log failed, appId : {}, env : {}, version : {}, message : {}", appId, env, currentVersion, e.getMessage());
        }
    }

    private void saveAppCode(String appId, String appCode) {
        syncCommands.hset(appCodeKeys, appId, appCode);
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

        localInvalidate(entityId, version);

        //  删除redis
        boolean isDelete = remoteInvalidate(entityId, version);
        if (!isDelete) {
            logger.warn("delete remote failed, entityId:[{}], version:[{}]", entityId, version);
        }
        return true;
    }

    private boolean remoteInvalidate(Long entityId, int version) {
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

    private Map<String, Map<String, String>> remoteMultiplyLoading(Collection<Long> ids, int version)
        throws JsonProcessingException {

        int extraSize = ids.size();
        String[] extraEntityIds = new String[extraSize + 1];
        extraEntityIds[0] = version + "";
        int j = 1;
        /*
         * set ancestors
         */
        for (Long id : ids) {
            extraEntityIds[j++] = Long.toString(id);
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

        return valuePairs;
    }

    private void localInvalidate(long entityId, int version) {
        try {
            cacheContext.entityClassStorageCache().invalidate(generateEntityCacheKey(entityId, version));
            cacheContext.entityClassStorageCache().invalidate(generateEntityCacheKey(entityId, version));
            cacheContext.versionCache().remove(entityId);
        } catch (Exception e) {
            logger.warn("delete local failed, entityId:[{}], version:[{}], message:[{}]", entityId, version,
                e.getMessage());
        }
    }

    private MetaChangePayLoad.FieldChange toFieldChange(IEntityField oldOne, IEntityField newOne, String profile) {
        if (null == oldOne && null == newOne) {
            throw new RuntimeException("add event could not handle IEntityField oldOne & newOne all empty.");
        }

        if (null == oldOne) {
            return new MetaChangePayLoad.FieldChange(newOne.id(), OperationType.CREATE, profile);
        } else if (null == newOne) {
            return new MetaChangePayLoad.FieldChange(oldOne.id(), OperationType.DELETE, profile);
        } else if (!oldOne.toString().equals(newOne.toString())) {
            //  entityField变更的标准为toString后两边不一致
            return new MetaChangePayLoad.FieldChange(newOne.id(), OperationType.UPDATE, profile);
        }
        return null;
    }

    private MetaChangePayLoad.EntityChange saveToCache(String key, EntityClassStorage oldStorage,
                                                          EntityClassStorage newStorage) {

        MetaChangePayLoad.EntityChange entityChange = null;

        //  basic elements
        syncCommands.hset(key, ELEMENT_ID, Long.toString(newStorage.getId()));
        syncCommands.hset(key, ELEMENT_APPCODE, newStorage.getAppCode());
        syncCommands.hset(key, ELEMENT_TYPE, Integer.toString(newStorage.getType()));
        syncCommands.hset(key, ELEMENT_CODE, newStorage.getCode());
        syncCommands.hset(key, ELEMENT_NAME, newStorage.getName());
        syncCommands.hset(key, ELEMENT_LEVEL, Integer.toString(newStorage.getLevel()));
        syncCommands.hset(key, ELEMENT_VERSION, Integer.toString(newStorage.getVersion()));

        //  father & ancestors
        if (null != newStorage.getFatherId() && newStorage.getFatherId() > 0) {
            syncCommands.hset(key, ELEMENT_FATHER, Long.toString(newStorage.getFatherId()));
            if (null != newStorage.getAncestors() && newStorage.getAncestors().size() > 0) {
                try {
                    String ancestorStr = OBJECT_MAPPER.writeValueAsString(newStorage.getAncestors());
                    syncCommands.hset(key, ELEMENT_ANCESTORS, ancestorStr);
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse ancestors failed.", false);
                }
            }
        }

        //  relations
        if (null != newStorage.getRelations() && newStorage.getRelations().size() > 0) {
            try {
                String relationStr = OBJECT_MAPPER.writeValueAsString(newStorage.getRelations());
                syncCommands.hset(key, ELEMENT_RELATIONS, relationStr);
            } catch (JsonProcessingException e) {
                throw new MetaSyncClientException("parse relations failed.", false);
            }
        }

        //  fields
        if (null != newStorage.getFields()) {
            for (IEntityField entityField : newStorage.getFields()) {
                try {
                    IEntityField oldField = (null == oldStorage) ? null : oldStorage.find(entityField.id(), null);
                    MetaChangePayLoad.FieldChange change = toFieldChange(oldField, entityField, null);
                    if (null != change) {
                        if (null == entityChange) {
                            entityChange = new MetaChangePayLoad.EntityChange(newStorage.getId());
                        }
                        entityChange.getFieldChanges().add(change);
                    }

                    String entityFieldStr = OBJECT_MAPPER.writeValueAsString(entityField);
                    syncCommands.hset(key, ELEMENT_FIELDS + "." + entityField.id(), entityFieldStr);
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse entityField failed.", false);
                }
            }
        }

        //  profiles
        if (null != newStorage.getProfileStorageMap() && !newStorage.getProfileStorageMap().isEmpty()) {
            for (ProfileStorage ps : newStorage.getProfileStorageMap().values()) {
                if (null != ps.getEntityFieldList() && !ps.getEntityFieldList().isEmpty()) {
                    for (IEntityField entityField : ps.getEntityFieldList()) {
                        try {
                            String entityFieldStr = OBJECT_MAPPER.writeValueAsString(entityField);
                            syncCommands
                                .hset(key, generateProfileEntity(ps.getCode(), entityField.id()), entityFieldStr);

                            IEntityField oldField =
                                (null == oldStorage) ? null : oldStorage.find(entityField.id(), ps.getCode());
                            MetaChangePayLoad.FieldChange change = toFieldChange(oldField, entityField, ps.getCode());

                            if (null != change) {
                                if (null == entityChange) {
                                    entityChange = new MetaChangePayLoad.EntityChange(newStorage.getId());
                                }
                                entityChange.getFieldChanges().add(change);
                            }
                        } catch (JsonProcessingException e) {
                            throw new MetaSyncClientException("parse profile-entityFields failed.", false);
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

        //  反向检查 DELETE事件
        if (null != oldStorage) {
            if (null == entityChange) {
                entityChange = new MetaChangePayLoad.EntityChange(oldStorage.getId());
            }
            reverseEventCheck(oldStorage, newStorage.getFields(), newStorage.getProfileStorageMap(), entityChange);
        }

        return entityChange;
    }

    /**
     * 反向寻找已被删除的字段, 查找范围包括EntityField, Profile->EntityField.
     */
    private void reverseEventCheck(EntityClassStorage oldStorage, List<EntityField> newFields,
                                   Map<String, ProfileStorage> newProfiles,
                                   MetaChangePayLoad.EntityChange entityChange) {
        //  普通field删除事件
        oldStorage.getFields().forEach(
            f -> {
                findAndAddEvent(f, newFields, null, entityChange);
            }
        );

        //  profile-field删除事件
        oldStorage.getProfileStorageMap().forEach(
            (k, v) -> {
                ProfileStorage newProfile = null != newProfiles ? newProfiles.get(k) : null;

                v.getEntityFieldList().forEach(
                    e -> {
                        findAndAddEvent(e, null != newProfile ? newProfile.getEntityFieldList() : null, k,
                            entityChange);
                    }
                );
            }
        );
    }

    /**
     * 寻找并加入event.
     */
    private void findAndAddEvent(IEntityField origin, List<EntityField> newFields, String profile,
                                 MetaChangePayLoad.EntityChange entityChange) {
        IEntityField findEntityField = null;

        if (null != newFields) {
            findEntityField = newFields.stream().filter(n -> n.id() == origin.id()).findFirst().orElse(null);
        }

        if (null == findEntityField) {
            MetaChangePayLoad.FieldChange change =
                toFieldChange(origin, null, profile);
            entityChange.getFieldChanges().add(change);
        }
    }

    private String toCacheSetKey(int version, long id) {
        return entityStorageKeys + "." + version + "." + id;
    }

    private void cachedVersion() {
        Map<String, String> appEnvMaps = syncCommands.hgetall(appEntityCollectionsKey);
        appEnvMaps.forEach(
            (key, value) -> {
                int version = splitAppVersion(key);
                if (version > NOT_EXIST_VERSION) {
                    try {
                        List<Long> ids = OBJECT_MAPPER.readValue(value,
                            OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, Long.class));

                        if (null != ids) {
                            ids.forEach(
                                id -> {
                                    cacheContext.versionCache().put(id, version);
                                }
                            );
                        }

                    } catch (JsonProcessingException e) {
                        logger.warn("cache version json error, message : {}", e.getMessage());
                    }
                }
            }
        );
    }

    private int splitAppVersion(String key) {
        try {
            String[] datas = key.split("\\.");
            if (datas.length == 2) {
                return Integer.parseInt(datas[1]);
            }
        } catch (Exception e) {
            logger.warn("key {}, split appVersion failed, message : {}", key, e.getMessage());
        }
        return NOT_EXIST_VERSION;
    }

    private String toNowDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return formatter.format(date);
    }
}
