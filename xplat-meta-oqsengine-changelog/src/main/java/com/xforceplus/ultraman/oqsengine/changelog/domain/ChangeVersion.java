package com.xforceplus.ultraman.oqsengine.changelog.domain;

/**
 * change version and comment
 */
public class ChangeVersion {

    private Long version;

    private String comment;

    private long id;

    private long timestamp;

    private String username;

    private Long source;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "ChangeVersion{" +
                "version=" + version +
                ", comment='" + comment + '\'' +
                ", id=" + id +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", source=" + source +
                '}';
    }
}
