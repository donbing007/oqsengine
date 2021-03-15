package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.SnapshotEvent;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;

public class SnapshotEventHandler implements ChangelogEventHandler<SnapshotEvent> {

    @Override
    public boolean required(ChangelogEvent changelogEvent) {
        return changelogEvent instanceof SnapshotEvent;
    }

    @Override
    public void onEvent(SnapshotEvent changelogEvent) {

    }
}
