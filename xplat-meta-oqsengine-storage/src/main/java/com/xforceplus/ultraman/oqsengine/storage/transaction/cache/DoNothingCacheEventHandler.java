package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.util.Collection;

/**
 * desc :
 * name : DoNothingCacheEventHandler
 *
 * @author : xujia
 * date : 2021/4/13
 * @since : 1.8
 */
public class DoNothingCacheEventHandler implements CacheEventHandler {
    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {
        return null;
    }

    @Override
    public boolean create(long txId, long number, IEntity entity) {
        return false;
    }

    @Override
    public boolean replace(long txId, long number, IEntity entity, IEntity old) {
        return false;
    }

    @Override
    public boolean delete(long txId, long number, IEntity entity) {
        return false;
    }

    @Override
    public boolean begin(long txId) {
        return false;
    }

    @Override
    public boolean commit(long txId, long maxOpNumber) {
        return false;
    }

    @Override
    public boolean rollback(long txId) {
        return false;
    }
}
