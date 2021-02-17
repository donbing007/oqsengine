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
                "if (redis.call('setnx', prePareKey, '1') == 1) then " +
                    "redis.call('expire', prePareKey, 600);" +
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
     * 删除对应AppId，Version版本的过期信息
     * 一个EntityClass将被存入2个Hash中，分别对应为（Basement）基本信息和EntityField信息
     * 所以需要删除2次
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
     *
     */
    public static String ACTIVE_VERSION =
            "local entityAppMapping = KEYS[1]..ARGV[1]" +
            "local value = redis.call('get', entityAppMapping);" +
            "if value ~= false then " +
                "return redis.call('hget', KEYS[2], value);" +
            "end;" +
            "return -1;";


//
//    /**
//     * 一次性获取EntityClass的信息
//     */
//    public static String ENTITY_CLASS_INFO =
//            "function string:split(sep)" +
//            "    local sep, fields = sep or ',', {};" +
//            "    local pattern = string.format('([^%s]+)', sep);" +
//            "    self:gsub(pattern, function(c) fields[#fields+1] = c end);" +
//            "    return fields;" +
//            "end;" +
//
//            "function get_entity(baseKey, fieldKey)" +
//                    "local result = {}; " +
//                    "result['id'] = redis.call('hget', baseKey, 'id');" +
//                    "result['name'] = redis.call('hget', baseKey, 'name');" +
//                    "result['code'] = redis.call('hget', baseKey, 'code');" +
//                    "result['version'] = redis.call('hget', baseKey, 'version');" +
//                    "result['level'] = redis.call('hget', baseKey, 'level');" +
//                    "result['relations'] = redis.call('hget', baseKey, 'relations');" +
//                    "result['fatherId'] = redis.call('hget', baseKey, 'fatherId');" +
//                    "result['childIds'] = redis.call('hget', baseKey, 'childIds');" +
//                    "result['ancestors'] = redis.call('hget', baseKey, 'ancestors');" +
//                    //  handle EntityFields
//                    "local entityFieldKeys = redis.call('hgetall', fieldKey)" +
//                    "if (idStr ~= nil) then" +
//                    "  result['field'] = redis.call('hmget', currentEntity, unpack(entityFieldKeys));" +
//                    "end;" +
//                    "return result;" +
//            "end;" +
//
//            "local ret = {}; " +
//            "local j = 1;" +
//            "local baseKey = KEYS[1]..ARGV[1]..ARGV[2]" +
//            "local fieldKey = KEYS[1]..ARGV[1]..ARGV[2]..ARGV[3]" +
//            "ret[j] = get_entity(baseKey, fieldKey);" +
//            "j = j + 1;" +
//            //  handle ancestors
//            "if (ret[1].ancestors and ret[1].ancestors ~= '') then " +
//                "local ancss = ret[1].ancestors:split();" +
//                "for i, v in ipairs(ancss) do " +
//                    "local fBaseKey = KEYS[1]..ARGV[v]..ARGV[2]" +
//                    "local fFieldKey = KEYS[1]..ARGV[v]..ARGV[2]..ARGV[3]" +
//                    "ret[j] = get_entity(fBaseKey, fFieldKey);" +
//                "end;" +
//            "end;" +
//            //  handle childs
//            "if (ret[1].childIds and ret[1].childIds ~= '') then " +
//                "local childs = ret[1].childIds:split();" +
//                "for i, v in ipairs(childs) do " +
//                    "local fBaseKey = KEYS[1]..ARGV[v]..ARGV[2]" +
//                    "local fFieldKey = KEYS[1]..ARGV[v]..ARGV[2]..ARGV[3]" +
//                    "ret[j] = get_entity(fBaseKey, fFieldKey);" +
//                "end;" +
//            "end;" +
//            "return cjson.encode(ret);";

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
