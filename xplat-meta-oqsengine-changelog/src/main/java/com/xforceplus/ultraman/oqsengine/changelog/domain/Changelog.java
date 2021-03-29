package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.List;

/**
 * change log pojo
 */
public class Changelog {

    /**
     * commmit id
     */
    private long version;

    private long cId;

    private long id;

    private String comment;

    private long createTime;

    private List<ChangeValue> changeValues;

    private long entityClass;

    private String username;

    private int deleteFlag;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getcId() {
        return cId;
    }

    public void setcId(long cId) {
        this.cId = cId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(long entityClass) {
        this.entityClass = entityClass;
    }

    public List<ChangeValue> getChangeValues() {
        return changeValues;
    }

    public void setChangeValues(List<ChangeValue> changeValues) {
        this.changeValues = changeValues;
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Changelog{" +
                "version=" + version +
                ", cId=" + cId +
                ", id=" + id +
                ", comment='" + comment + '\'' +
                ", createTime=" + createTime +
                ", changeValues=" + changeValues +
                ", entityClass=" + entityClass +
                ", username='" + username + '\'' +
                ", deleteFlag=" + deleteFlag +
                '}';
    }
}
