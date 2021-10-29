package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import java.util.Map;

/**
 * a event to store in some where.
 */
public class PersistentEvent implements ChangelogEvent {

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

    /**
     * has no context.
     */
    @Override
    public Map<String, Object> getContext() {
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("PersistentEvent{").append("changelog=").append(changelog).append('}')
            .toString();
    }
}
