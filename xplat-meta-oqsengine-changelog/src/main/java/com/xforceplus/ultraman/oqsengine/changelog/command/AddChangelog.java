package com.xforceplus.ultraman.oqsengine.changelog.command;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;

/**
 * a command wrap a changelogedEvent
 */
public class AddChangelog implements ChangelogCommand{

    private long objId;

    private long entityclassId;

    private ChangedEvent changedEvent;

    public AddChangelog(long objId, long entityclassId, ChangedEvent changedEvent) {
        this.objId = objId;
        this.changedEvent = changedEvent;
        this.entityclassId = entityclassId;
    }

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }

    public long getObjId() {
        return objId;
    }

    public long getEntityclassId() {
        return entityclassId;
    }
}
