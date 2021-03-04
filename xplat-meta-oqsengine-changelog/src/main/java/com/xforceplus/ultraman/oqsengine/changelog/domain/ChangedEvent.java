package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;

/**
 * changed Event
 */
public class ChangedEvent {

    private long entityClassId;

    private long id;

    private IEntity before;

    private IEntity after;

    private long commitId;

    private String comment;

    private long timestamp;

    private OperationType operationType;

    public IEntity getBefore() {
        return before;
    }

    public void setBefore(IEntity before) {
        this.before = before;
    }

    public IEntity getAfter() {
        return after;
    }

    public void setAfter(IEntity after) {
        this.after = after;
    }

    public long getCommitId() {
        return commitId;
    }

    public void setCommitId(long commitId) {
        this.commitId = commitId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
