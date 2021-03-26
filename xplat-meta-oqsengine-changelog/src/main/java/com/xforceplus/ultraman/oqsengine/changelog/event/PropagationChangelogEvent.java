package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;

import java.util.Map;

/**
 * propagation event
 */
public class PropagationChangelogEvent implements ChangelogEvent {

    private long destinationObjId;

    private long entityClassId;

    private ChangedEvent changedEvent;

    /**
     * event context
     */
    private Map<String, Object> context;

    public PropagationChangelogEvent(long destinationObjId, long entityClassId, ChangedEvent changedEvent, Map<String, Object> context) {
        this.destinationObjId = destinationObjId;
        this.changedEvent = changedEvent;
        this.entityClassId = entityClassId;
        this.context = context;
    }

    public long getDestinationObjId() {
        return destinationObjId;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }


    @Override
    public Map<String, Object> getContext() {
        return context;
    }
}
