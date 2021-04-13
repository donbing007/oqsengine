package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.event.Event;

import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;

import java.util.Collection;

/**
 * desc :
 * name : CacheEventHandler
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public interface CacheEventHandler {

    public boolean onEventCreate(Event<CachePayload> event);

    public boolean onEventUpdate(Event<CachePayload> event);

    public boolean onEventDelete(Event<CachePayload> event);

    public boolean onEventBegin(long txId);

    public boolean onEventCommit(long txId, long maxOpNumber);

    public boolean onEventRollback(long txId);

    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);
}
