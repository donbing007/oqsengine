package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;

/**
 * version event to return
 */
public class VersionEvent implements ChangelogEvent{

    private ChangeVersion changeVersion;

    public VersionEvent(ChangeVersion changeVersion) {
        this.changeVersion = changeVersion;
    }

    public ChangeVersion getChangeVersion() {
        return changeVersion;
    }
}
