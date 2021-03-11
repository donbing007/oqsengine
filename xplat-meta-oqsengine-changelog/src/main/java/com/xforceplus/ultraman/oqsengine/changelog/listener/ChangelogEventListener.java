package com.xforceplus.ultraman.oqsengine.changelog.listener;

import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;

/**
 * listener
 */
public interface ChangelogEventListener<T extends ChangelogEvent> {

    boolean require(ChangelogEvent changelogEvent);

    void consume(ChangelogEvent changelogEvent);
}
