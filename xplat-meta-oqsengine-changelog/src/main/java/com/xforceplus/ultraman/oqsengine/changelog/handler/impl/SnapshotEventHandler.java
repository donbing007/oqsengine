package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.SnapshotService;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.SnapshotEvent;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;

import javax.annotation.Resource;

public class SnapshotEventHandler implements ChangelogEventHandler<SnapshotEvent> {

    @Resource
    private SnapshotService snapshotService;

    @Override
    public boolean required(ChangelogEvent changelogEvent) {
        return changelogEvent instanceof SnapshotEvent;
    }

    @Override
    public void onEvent(SnapshotEvent changelogEvent) {
        snapshotService.saveSnapshot(changelogEvent.getChangeSnapshot());
    }
}
