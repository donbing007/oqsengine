package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.PersistentEvent;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * persistent event handler
 */
public class PersistentEventHandler implements ChangelogEventHandler<PersistentEvent> {

    @Resource
    private ChangelogService changelogService;

    @Override
    public boolean required(ChangelogEvent changelogEvent) {
        return changelogEvent instanceof PersistentEvent;
    }

    @Override
    public void onEvent(PersistentEvent persistentEvent) {
        Changelog changelog = persistentEvent.getChangelog();
        changelogService.saveChangeLogs(Collections.singletonList(changelog));
    }
}
