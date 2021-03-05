package com.xforceplus.ultraman.oqsengine.changelog.domain;

/**
 * change version and comment
 */
public class ChangeVersion {

    private Long version;

    private String comment;

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

    @Override
    public String toString() {
        return "ChangeVersion{" +
                "version='" + version + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
