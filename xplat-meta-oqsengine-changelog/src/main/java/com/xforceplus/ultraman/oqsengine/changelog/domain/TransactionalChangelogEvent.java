package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.List;

/**
 * once a transaction committed will produce multi changelogEvent
 */
public class TransactionalChangelogEvent {

    /**
     * commitId
     */
    private Long commitId;

    private List<ChangedEvent> changedEventList;

    public Long getCommitId() {
        return commitId;
    }

    public void setCommitId(Long commitId) {
        this.commitId = commitId;
    }

    public List<ChangedEvent> getChangedEventList() {
        return changedEventList;
    }

    public void setChangedEventList(List<ChangedEvent> changedEventList) {
        this.changedEventList = changedEventList;
    }

    @Override
    public String toString() {
        return "TransactionalChangelogEvent{" +
                "commitId=" + commitId +
                ", changedEventList=" + changedEventList +
                '}';
    }
}
