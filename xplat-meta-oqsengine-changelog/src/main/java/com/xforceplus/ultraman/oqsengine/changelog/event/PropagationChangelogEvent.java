package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;

/**
 * propagation event
 */
public class PropagationChangelogEvent implements ChangelogEvent {

    private Long destinationObjId;

    private ChangedEvent changedEvent;

    public PropagationChangelogEvent(Long destinationObjId, ChangedEvent changedEvent) {
        this.destinationObjId = destinationObjId;
        this.changedEvent = changedEvent;
    }

    public Long getDestinationObjId() {
        return destinationObjId;
    }

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }
}
