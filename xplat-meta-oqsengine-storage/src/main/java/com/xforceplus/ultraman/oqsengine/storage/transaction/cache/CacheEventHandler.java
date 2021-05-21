package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.util.Collection;

/**
 * desc :.
 * name : CacheEventHandler
 *
 * @author : xujia 2021/4/8
 * @since : 1.8
 */
public interface CacheEventHandler {

    /**
     * 查询当前txId的列表信息.
     * 过滤条件为id, version, eventType，如需要过滤则必须传以上3值，否则将返回txId的列表信息
     */
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);

    /**
     * 创建一条build event.
     */
    public boolean create(long txId, long number, IEntity entity);

    /**
     * 创建一条replace event.
     */
    public boolean replace(long txId, long number, IEntity entity, IEntity old);

    /**
     * 创建一条delete event.
     */
    public boolean delete(long txId, long number, IEntity entity);

    /**
     * 开始事务 调用.
     */
    public boolean begin(long txId);

    /**
     * 提交事务 调用.
     */
    public boolean commit(long txId, long maxOpNumber);

    /**
     * 回滚事务 调用.
     */
    public boolean rollback(long txId);
}
