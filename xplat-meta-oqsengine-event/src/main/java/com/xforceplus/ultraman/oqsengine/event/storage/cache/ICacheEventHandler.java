package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
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

    public boolean onEventCreate(Event<BuildPayload> event);

    public boolean onEventUpdate(Event<ReplacePayload> event);

    public boolean onEventDelete(Event<DeletePayload> event);

    public boolean onEventBegin(Event<BeginPayload> event);

    public boolean onEventCommit(Event<CommitPayload> event);

    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);

    public long queueSize();

    public int eventCleanByRange(long start, long end);

    public void eventCleanByTxId(long txId);
}
