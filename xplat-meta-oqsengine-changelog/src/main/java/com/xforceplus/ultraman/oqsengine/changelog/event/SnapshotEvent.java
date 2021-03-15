package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;

import java.util.Map;

/**
 * snapshot event
 */
public class SnapshotEvent implements ChangelogEvent{

    private ChangeSnapshot changeSnapshot;

    public SnapshotEvent(ChangeSnapshot changeSnapshot) {
        this.changeSnapshot = changeSnapshot;
    }

    public ChangeSnapshot getChangeSnapshot() {
        return changeSnapshot;
    }

    @Override
    public Map<String, Object> getContext() {
        return null;
    }
}
