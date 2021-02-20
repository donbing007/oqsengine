package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    boolean save(String appId, int version, List<EntityClassStorage> storageList);

    Map<Long, EntityClassStorage> read(long entityClassId) throws JsonProcessingException;

    boolean clean(String appId, int version, boolean force);

    int version(String appId);

    int version(Long entityClassId);

    boolean resetVersion(String appId, int version, List<Long> ids);

    boolean prepare(String appId, int version);

    boolean endPrepare(String appId);
}
