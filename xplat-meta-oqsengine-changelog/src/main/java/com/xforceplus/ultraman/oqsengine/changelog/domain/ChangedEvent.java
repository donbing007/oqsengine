package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;

import java.util.Map;

/**
 * changed Event
 */
public class ChangedEvent {


    /**
     * current EntityClassId
     */
    private long entityClassId;

    /**
     * current objId
     */
    private long id;

    /**
     * TODO
     * changed value
     */
    private Map<Long, ValueWrapper> valueMap;


    /**
     * current version
     */
    private long commitId;

    /**
     * current comment
     */
    private String comment;

    /**
     * current timestamp
     */
    private long timestamp;

    /**
     * current operationType
     */
    private OperationType operationType;

    /**
     * changelog operator
     * @return
     */
    private String username;

    public Map<Long, ValueWrapper> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<Long, ValueWrapper> valueMap) {
        this.valueMap = valueMap;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "ChangedEvent{" +
                "entityClassId=" + entityClassId +
                ", id=" + id +
                ", valueMap=" + valueMap +
                ", commitId=" + commitId +
                ", comment='" + comment + '\'' +
                ", timestamp=" + timestamp +
                ", operationType=" + operationType +
                ", username='" + username + '\'' +
                '}';
    }
}
