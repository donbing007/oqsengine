package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;

/**
 * a event to store in some where
 */
public class PersistentEvent implements ChangelogEvent{

    private Changelog changelog;

    public PersistentEvent(Changelog changelog) {
        this.changelog = changelog;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public void setChangelog(Changelog changelog) {
        this.changelog = changelog;
    }
}
