package com.xforceplus.ultraman.oqsengine.metadata.cache;

/**
 * desc :
 * name : RedisLuaScript
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class RedisLuaScript {

    public static final String DEFAULT_METADATA_APP_VERSIONS = "com.xforceplus.ultraman.oqsengine.metadata.versions";
    public static final String DEFAULT_METADATA_APP_PREPARE = "com.xforceplus.ultraman.oqsengine.metadata.prepare";

    public static final String DEFAULT_METADATA_APP_ENTITY = "com.xforceplus.ultraman.oqsengine.metadata.entity";


    public static final String DEFAULT_METADATA_ENTITY_APP_REL = "com.xforceplus.ultraman.oqsengine.metadata.entity.app.rel";

    /***
     * 传入 AppId, Version
     * 1。检查传入AppId，Version是否大于系统版本，如不满足则直接返回false
     * 2。检查当前AppId，Version是否已不存在于准备列表中，如果不满足则直接false
     * 3。写入准备的AppId
     */
    public static String PREPARE_VERSION_SCRIPT =
            "local appVersion = redis.call('hget', KEYS[1], ARGV[2]);" +
            "if appVersion == false or appVersion < ARGV[1] then " +
                "local prePareKey = KEYS[2]..ARGV[2]" +
                "if (redis.call('setnx', prePareKey, ARGV[1]) == 1) then " +
                    "redis.call('expire', prePareKey, 60);" +
                    "return 1;" +
                "else " +
                    "return 0;" +
                "end;" +
            "else " +
                "return 0;" +
            "end;";


    public static String END_PREPARE_VERSION_SCRIPT =
            "return redis.call('del', KEYS[1]..ARGV[1])";

    /**
     * 删除过期信息
     */
    public static String EXPIRED_VERSION_ENTITY_CLASS =
            "local currentEntity = KEYS[1]..ARGV[1]..ARGV[2]" +
            "local keys = redis.call('hkeys', currentEntity);" +
            "for i, v in ipairs(keys) do " +
                "redis.call('hdel', currentEntity, v);" +
            "end;" +
            "redis.call('hdel', currentEntity);" +
            "return 1";


    /**
     * 使用entityClassId获取活动版本信息
     * 首先从mapping的关系中找出entity对应的appId信息
     * 再使用appId获取当前版本
     * KEYS[1]-app-entity mapping key
     * KEYS[2]-app-version mapping key
     * ARGV[1]-appId
     */
    public static String ACTIVE_VERSION =
            "local value = redis.call('hget', KEYS[1], ARGV[1]);" +
            "if value ~= false then " +
                "return redis.call('hget', KEYS[2], value);" +
            "end;" +
            "return -1;";

    /**
     * 使用entityClassId获取活动版本信息
     * KEYS[1]-mapping key
     * KEYS[2]
     * ARGV[1]-appId
     * ARGV[2]-version
     * ARGV[3-N]-entityId
     * 首先设置当前的MAPPING关系，设置完毕后再更新当前版本
     */
    public static String REST_VERSION =
            "for i=3, #ARGV, 1 do " +
                "if (redis.call('hexists', KEYS[1], ARGV[i]) == 0) then " +
                    "redis.call('hset', KEYS[1], ARGV[i], ARGV[1])" +
                "end;" +
            "end; " +
            "redis.call('hset', KEYS[2], ARGV[1], ARGV[2]);" +
            "return 1;";

    /**
     * 获取单个EntityClassStorage
     * KEYS[1]-前缀
     * ARGV[1]-version
     * ARGV[2]-entityClassId
     */
    public static String ENTITY_CLASS_STORAGE_INFO =
            "local result = {}; " +
            "local baseKey = KEYS[1]..ARGV[1]..ARGV[2]; " +
            "local flat_map = redis.call('HGETALL', baseKey); " +
            "if flat_map ~= false then " +
                "for i = 1, #flat_map, 2 do " +
                    "result[flat_map[i]] = flat_map[i + 1];" +
                "end; " +
            "end;" +
            "return cjson.encode(result);";

    /**
     * 获取EntityClassStorage列表
     * KEYS[1]-前缀
     * ARGV[1]-version
     * ARGV[2-N]-entityClassIds
     */
    public static String ENTITY_CLASS_STORAGE_INFO_LIST =
            "local empty = {}; " +
            "local result = {}; " +
            "for i=2, #ARGV, 1 do " +
                "local baseKey = KEYS[1]..ARGV[1]..ARGV[i]; " +
                "local flat_map = redis.call('HGETALL', baseKey); " +
                "local ret = {}; " +
                "if flat_map == false then " +
                    "return empty;" +
                "end;" +
                "for i = 1, #flat_map, 2 do " +
                    "ret[flat_map[i]] = flat_map[i + 1];" +
                "end; " +
                "result[ARGV[i]] = ret;" +
            "end;" +
            "return cjson.encode(result);";
}
