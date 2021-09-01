package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存执行器接口.
 *
 * @author xujia 2021/2/9
 * @since 1.8
 */
public interface CacheExecutor {

    /**
     * 保存storageList.
     *
     * @param appId       应用标识.
     * @param version     版本号.
     * @param storageList 需要保存的元信息.
     * @param payLoads 需要发布的事件.
     * @return true成功, false失败.
     */
    boolean save(String appId, int version, List<EntityClassStorage> storageList, List<Event<?>> payLoads);

    /**
     * 读取storageList原始信息，由外部进行EntityClass拼装.
     *
     * @param entityClassId 元信息标识.
     * @return 元信息.
     * @throws JsonProcessingException JSON异常.
     */
    Map<Long, EntityClassStorage> read(long entityClassId) throws JsonProcessingException;

    /**
     * 批量读取.
     *
     * @param ids     元信息列表.
     * @param version 版本号.
     * @return 元信息结果.
     * @throws JsonProcessingException JSON异常.
     */
    Map<Long, EntityClassStorage> multiplyRead(Collection<Long> ids, int version, boolean useLocalCache) throws JsonProcessingException;

    /**
     * 清除AppId + version对应的存储记录.
     *
     * @param appId   应用标识.
     * @param version 版本号.
     * @param force   是否强制
     * @return true 成功, false 失败.
     */
    boolean clean(String appId, int version, boolean force);

    /**
     * 获取当前appId的entityId列表.
     *
     * @param appId  应用标识.
     * @param version 版本号.
     * @return entityId列表.
     */
    public Collection<Long> appEntityIdList(String appId, Integer version);

    /**
     * 获取appId对应的版本信息.
     * 小于0表示没有相应的应用.
     *
     * @param appId 应用标识.
     * @return 版本号.
     */
    int version(String appId);

    /**
     * 获取entityClassId对应的版本信息.
     * 小于0表示没有相应的元信息.
     *
     * @param entityClassId 元信息版本号标识.
     * @return 版本号.
     */
    int version(Long entityClassId);

    /**
     * 重置appId对应的版本信息.
     *
     * @param appId   应用标识.
     * @param version 版本号.
     * @param ids     目标元信息ID列表.
     * @return true成功, false失败.
     */
    boolean resetVersion(String appId, int version, List<Long> ids);

    /**
     * 执行版本更新前的准备动作.
     *
     * @param appId   应用标识.
     * @param version 版本.
     * @return true 成功,false失败.
     */
    boolean prepare(String appId, int version);

    /**
     * 结束准备.
     *
     * @param appId 应用标识.
     * @return true成功, false失败.
     */
    boolean endPrepare(String appId);

    /**
     * 通过appId获取当前的Env信息.
     *
     * @param appId 应用标识.
     * @return 环境编码.
     */
    String appEnvGet(String appId);

    /**
     * 设置当前appId所使用的Env.
     *
     * @param appId 应用标识.
     * @param env   环境编码.
     * @return true成功, false失败.
     */
    boolean appEnvSet(String appId, String env);

    /**
     * 移除当前appId所使用的Env.
     *
     * @param appId 应用标识.
     * @return true成功, false失败.
     */
    boolean appEnvRemove(String appId);

    /**
     * 设置本地缓存无效.
     */
    void invalidateLocal();

    /**
     * 读取内存中应用下的配置信息.
     * @param appId 应用标识.
     * @return
     */
    List<EntityClassStorage> read(String appId);
}
