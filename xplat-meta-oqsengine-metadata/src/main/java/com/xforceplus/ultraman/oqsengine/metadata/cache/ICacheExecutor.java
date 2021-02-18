package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;

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
     * 存储appId级别的所有EntityClassStorage对象
     * @param appId
     * @param version
     * @param storageList
     * @return
     */
    boolean save(String appId, int version, List<EntityClassStorage> storageList);

    /**
     * 读取当前版本entityClassId所对应的EntityClass及所有父对象、子对象
     * @param entityClassId
     * @return
     */
    Map<Long, EntityClassStorage> read(long entityClassId);

    /**
     * 删除已经过期EntityClass版本
     * @param entityId
     * @param version
     * @return
     */
    boolean clean(Long entityId, int version);

    int version(String appId);

    boolean resetVersion(String appId, int version, List<Long> ids);

    boolean prepare(String appId, int version);

    boolean endPrepare(String appId);
}
