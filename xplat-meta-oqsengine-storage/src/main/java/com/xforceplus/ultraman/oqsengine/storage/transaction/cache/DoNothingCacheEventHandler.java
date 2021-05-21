package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.util.ArrayList;
import java.util.Collection;

/**
 * desc :.
 * name : DoNothingCacheEventHandler
 *
 * @author : xujia 2021/4/13
 * @since : 1.8
 */
public class DoNothingCacheEventHandler implements CacheEventHandler {
    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {
        return new ArrayList<>();
    }

    @Override
    public boolean create(long txId, long number, IEntity entity) {
        return true;
    }

    @Override
    public boolean replace(long txId, long number, IEntity entity, IEntity old) {
        return true;
    }

    @Override
    public boolean delete(long txId, long number, IEntity entity) {
        return true;
    }

    @Override
    public boolean begin(long txId) {
        return true;
    }

    @Override
    public boolean commit(long txId, long maxOpNumber) {
        return true;
    }

    @Override
    public boolean rollback(long txId) {
        return true;
    }
}
