package com.xforceplus.ultraman.oqsengine.changelog.handler;

import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;

/**
 * listener
 */
public interface ChangelogEventHandler<T extends ChangelogEvent> {

    boolean required(ChangelogEvent changelogEvent);

    void onEvent(T changelogEvent);
}
