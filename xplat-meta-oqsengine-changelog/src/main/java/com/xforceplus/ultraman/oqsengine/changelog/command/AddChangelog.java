package com.xforceplus.ultraman.oqsengine.changelog.command;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;

/**
 * a command wrap a changelogedEvent
 */
public class AddChangelog implements ChangelogCommand{

    private ChangedEvent changedEvent;

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }

    public void setChangedEvent(ChangedEvent changedEvent) {
        this.changedEvent = changedEvent;
    }
}
