package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;

import java.util.List;
import java.util.Map;

/**
 * desc :
 * name : ICacheExecutor
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public interface ICacheExecutor {

    /**
     * 保存storageList
     * @param appId
     * @param version
     * @param storageList
     * @return
     */
    boolean save(String appId, int version, List<EntityClassStorage> storageList);

    /**
     * 读取storageList原始信息，由外部进行EntityClass拼装
     * @param entityClassId
     * @return
     * @throws JsonProcessingException
     */
    Map<Long, EntityClassStorage> read(long entityClassId) throws JsonProcessingException;

    /**
     * 批量读取
     * @param ids
     * @param version
     * @return
     * @throws JsonProcessingException
     */
    Map<Long, EntityClassStorage> multiplyRead(List<Long> ids, int version) throws JsonProcessingException;

    /**
     * 清除AppId + version对应的存储记录
     * @param appId
     * @param version
     * @param force 是否强制
     * @return
     */
    boolean clean(String appId, int version, boolean force);

    /**
     * 获取appId对应的版本信息
     * @param appId
     * @return
     */
    int version(String appId);

    /**
     * 获取entityClassId对应的版本信息
     * @param entityClassId
     * @return
     */
    int version(Long entityClassId);

    /**
     * 重置appId对应的版本信息
     * @param appId
     * @param version
     * @param ids
     * @return
     */
    boolean resetVersion(String appId, int version, List<Long> ids);

    /**
     * 执行版本更新前的准备动作
     * @param appId
     * @param version
     * @return
     */
    boolean prepare(String appId, int version);

    /**
     * 结束准备
     * @param appId
     * @return
     */
    boolean endPrepare(String appId);

    /**
     * 通过appId获取当前的Env信息
     * @param appId
     * @return
     */
    String appEnvGet(String appId);

    /**
     * 设置当前appId所使用的Env
     * @param appId
     * @param env
     * @return
     */
    boolean appEnvSet(String appId, String env);

    /**
     * 移除当前appId所使用的Env
     * @param appId
     * @return
     */
    boolean appEnvRemove(String appId);

    /**
     * 设置本地缓存无效
     */
    void invalidateLocal();
}
