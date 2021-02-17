package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.metadata.cache.RedisLuaScript.*;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.*;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.valuesToStorage;

/**
 * desc :
 * name : CacheExecutor
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class CacheExecutor implements ICacheExecutor {

    @Resource
    private RedisClient redisClient;

    @Resource
    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;


    /**
     * script
     */
    private String prepareVersionScriptSha;
    private String endPrepareVersionScriptSha;

    private String cleanExpiredEntityClassScriptSha;
    private String metadataAppRelScriptSha;
    private String entityClassStorageScriptSha;
    private String entityClassStorageListScriptSha;

    /**
     * keys
     */
    private String metadataVersionKeys;
    private String metadataPrepareKey;
    private String metadataAppRelKey;
    private String metadataAppEntityKeys;

    public CacheExecutor() {
        this(DEFAULT_METADATA_APP_VERSIONS, DEFAULT_METADATA_APP_PREPARE, DEFAULT_METADATA_APP_ENTITY, DEFAULT_METADATA_ENTITY_APP_REL);
    }

    public CacheExecutor(String metadataVersionKeys, String metadataPrepareKey, String metadataAppEntityKeys, String metadataAppRelKey) {
        this.metadataVersionKeys = metadataVersionKeys;
        if (this.metadataVersionKeys == null || this.metadataVersionKeys.isEmpty()) {
            throw new IllegalArgumentException("The metadataVersion keys is invalid.");
        }

        this.metadataPrepareKey = metadataPrepareKey;
        if (this.metadataPrepareKey == null || this.metadataPrepareKey.isEmpty()) {
            throw new IllegalArgumentException("The metadataPrepare key is invalid.");
        }

        this.metadataAppEntityKeys = metadataAppEntityKeys;
        if (this.metadataAppEntityKeys == null || this.metadataAppEntityKeys.isEmpty()) {
            throw new IllegalArgumentException("The metadataAppEntity key is invalid.");
        }

        this.metadataAppRelKey = metadataAppRelKey;
        if (this.metadataAppRelKey == null || this.metadataAppRelKey.isEmpty()) {
            throw new IllegalArgumentException("The metadataAppRel key is invalid.");
        }

    }

    @PostConstruct
    public void init() {

        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();

        syncCommands = syncConnect.sync();

        syncCommands.clientSetname("oqs.sync.metadata");

        prepareVersionScriptSha = syncCommands.scriptLoad(PREPARE_VERSION_SCRIPT);

        endPrepareVersionScriptSha = syncCommands.scriptLoad(END_PREPARE_VERSION_SCRIPT);

        cleanExpiredEntityClassScriptSha = syncCommands.scriptLoad(EXPIRED_VERSION_ENTITY_CLASS);

        metadataAppRelScriptSha = syncCommands.scriptLoad(ACTIVE_VERSION);

        entityClassStorageScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO);

        entityClassStorageListScriptSha = syncCommands.scriptLoad(ENTITY_CLASS_STORAGE_INFO_LIST);
    }

    @PreDestroy
    public void destroy() {
        syncConnect.close();
    }

    @Override
    public boolean save(String appId, int version, List<EntityClassStorage> storageList) {

        //  set data
        for (EntityClassStorage storage : storageList) {
            String key = metadataAppEntityKeys + version + storage.getId();

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
                        syncCommands.hset(key, ELEMENT_ANCESTORS, objectMapper.writeValueAsString(storage.getAncestors()));
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse ancestors failed.", false);
                    }
                }
            }

            //  children
            if (null != storage.getChildIds() && storage.getChildIds().size() > 0) {
                try {
                    syncCommands.hset(key, ELEMENT_CHILDREN, objectMapper.writeValueAsString(storage.getChildIds()));
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse children failed.", false);
                }
            }

            //  relations
            if (null != storage.getRelations() && storage.getRelations().size() > 0) {
                try {
                    syncCommands.hset(key, ELEMENT_RELATIONS, objectMapper.writeValueAsString(storage.getRelations()));
                } catch (JsonProcessingException e) {
                    throw new MetaSyncClientException("parse children failed.", false);
                }
            }

            //  fields
            if (null != storage.getFields()) {
                for (IEntityField entityField : storage.getFields()) {
                    try {
                        syncCommands.hset(key, ELEMENT_FIELDS + ".." + entityField.id(), objectMapper.writeValueAsString(entityField));
                    } catch (JsonProcessingException e) {
                        throw new MetaSyncClientException("parse entityField failed.", false);
                    }
                }
            }
        }

        //  set relation
        resetMapping(storageList.stream().map(EntityClassStorage::getId).collect(Collectors.toList()), appId);

        //  set version
        resetVersion(appId, version);

        return true;
    }

    @Override
    public Map<Long, EntityClassStorage> read(long entityClassId) {
        int version = version(entityClassId);
        if (-1 != version) {
            String[] keys = {
                    metadataAppEntityKeys
            };

            //  todo get from local cache

            try {
                //  get from redis
                String redisValue = syncCommands.evalsha(
                        entityClassStorageScriptSha,
                        ScriptOutputType.VALUE,
                        keys, version + "" , Long.toString(entityClassId));

                Map<Long, EntityClassStorage> entityClassStorageMap = new HashMap<>();
                //  get self
                Map<String, String> keyValues = objectMapper.readValue(redisValue, Map.class);
                EntityClassStorage entityClassStorage =
                        valuesToStorage(objectMapper, keyValues);

                entityClassStorageMap.put(entityClassStorage.getId(), entityClassStorage);

                //  get father, Children
                int extraSize = entityClassStorage.getAncestors().size() + entityClassStorage.getChildIds().size();
                if (extraSize > 0) {
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

                    /**
                     * set children
                     */
                    for (int i = 0; i < entityClassStorage.getChildIds().size(); i ++) {
                        extraEntityIds[j] = Long.toString(entityClassStorage.getChildIds().get(i));
                        j++;
                    }

                    redisValue = syncCommands.evalsha(
                            entityClassStorageListScriptSha,
                            ScriptOutputType.VALUE,
                            keys, extraEntityIds);

                    Map<String, Map<String, String>> valuePairs = objectMapper.readValue(redisValue, Map.class);
                    for (Map.Entry<String, Map<String, String>> value : valuePairs.entrySet()) {
                        EntityClassStorage storage =
                                valuesToStorage(objectMapper, value.getValue());

                        entityClassStorageMap.put(storage.getId(), storage);
                    }
                }
                //   todo rewrite into local cache and set 1 day expire time

                return entityClassStorageMap;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new MetaSyncClientException(e.getMessage(), false);
            }
        }

        throw new MetaSyncClientException("invalid version -1", false);
    }

    /**
     * 获取当前appId对应的版本信息
     * @param appId
     * @return
     */
    @Override
    public int version(String appId) {
        String versionStr = syncCommands.hget(metadataVersionKeys, appId);
        if (null != versionStr) {
            return Integer.parseInt(versionStr);
        }
        return -1;
    }

    /**
     * 重置appId对应的版本信息
     * @param appId
     * @return
     */
    @Override
    public boolean resetVersion(String appId, int version) {
        syncCommands.hset(metadataVersionKeys, appId, Integer.toString(version));
        return true;
    }

    /**
     * 删除过期版本的EntityClass信息
     * @param entityId
     * @param version
     * @return
     */
    @Override
    public boolean clean(Long entityId, int version) {
        if (null == entityId) {
            return false;
        }

        if (version <= 0) {
            return false;
        }

        String[] keys = {
                metadataAppEntityKeys
        };

        return syncCommands.evalsha(
                    cleanExpiredEntityClassScriptSha,
                    ScriptOutputType.BOOLEAN,
                    keys, Long.toString(entityId), Integer.toString(version), "FIELD");
    }

    @Override
    public boolean resetMapping(List<Long> entityId, String appId) {
        entityId.forEach(
                e -> {
                    String key = metadataAppRelKey + ".." + e;
                    syncCommands.set(key, appId);
                }
        );
        return true;
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
                metadataVersionKeys,
                metadataPrepareKey,
        };

        return syncCommands.evalsha(
                    prepareVersionScriptSha,
                    ScriptOutputType.BOOLEAN,
                    keys, Integer.toString(version), appId);
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
        String[] keys = {
                metadataPrepareKey
        };
        return syncCommands.evalsha(
                endPrepareVersionScriptSha,
                ScriptOutputType.BOOLEAN,
                keys, appId);
//
////        String key = metadataPrepareKey + ".." + appId;
////        return syncCommands.del(key) == 1;
    }

    /**
     * 通过entityClassId获取当前活动版本
     * @param entityClassId
     * @return
     */
    private int version(Long entityClassId) {
        if (null == entityClassId || entityClassId <= 0) {
            return -1;
        }
        String key = metadataAppRelKey + ".." + entityClassId;
        String appId = syncCommands.get(key);

        return version(appId);

//
//        String[] keys = {
//                metadataAppRelKey,
//                metadataVersionKeys
//        };
//
//        return syncCommands.evalsha(
//                metadataAppRelScriptSha,
//                ScriptOutputType.INTEGER,
//                keys, Long.toString(entityClassId));
    }
}
