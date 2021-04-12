package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.UpdateCachePayload;

import java.util.Collection;

/**
 * desc :
 * name : CacheEventService
 *
 * @author : xujia
 * date : 2021/4/9
 * @since : 1.8
 */
public interface CacheEventService {

    /**
     * 查询当前txId的列表信息
     * 过滤条件为id, version, eventType，如需要过滤则必须传以上3值，否则将返回txId的列表信息
     * @param txId
     * @param id
     * @param version
     * @param eventType
     * @return
     */
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);

    public boolean create(long txId, long number, IEntity entity);

    public boolean replace(long txId, long number, IEntity entity, IEntity old);

    public boolean delete(long txId, long number, IEntity entity);

    public boolean begin(long txId);

    public boolean commit(long txId, long maxOpNumber);

    public boolean rollback(long txId);

}
