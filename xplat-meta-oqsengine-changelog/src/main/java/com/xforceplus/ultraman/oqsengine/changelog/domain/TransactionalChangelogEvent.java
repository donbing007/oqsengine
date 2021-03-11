package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.List;

/**
 * once a transaction committed will produce multi changelogEvent
 */
public class TransactionalChangelogEvent {

    /**
     * commitId
     */
    private long commitId;

    private List<ChangedEvent> changedEventList;

    public long getCommitId() {
        return commitId;
    }

    public void setCommitId(long commitId) {
        this.commitId = commitId;
    }

    public List<ChangedEvent> getChangedEventList() {
        return changedEventList;
    }

    public void setChangedEventList(List<ChangedEvent> changedEventList) {
        this.changedEventList = changedEventList;
    }
}
