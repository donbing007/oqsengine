package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.payload.cache.CachePayload;

import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;

import java.util.Collection;

/**
 * desc :
 * name : CacheEventHandler
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public interface ICacheEventHandler {

    public boolean onEventCreate(Event<CachePayload> event);

    public boolean onEventUpdate(Event<CachePayload> event);

    public boolean onEventDelete(Event<CachePayload> event);

    public boolean onEventBegin(Event<BeginPayload> event);

    public boolean onEventCommit(Event<CommitPayload> event);

    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);

    public long queueSize();

    public int eventCleanByRange(long start, long end);

    public void eventCleanByTxId(long txId);

    public long expiredDuration();
}
