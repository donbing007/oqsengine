package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.List;
import java.util.Map;

/**
 * snapshot for changelog to replay
 */
public class ChangeSnapshot {

    //agg version
    private long version;

    private long sId;

    private long id;

    private long createTime;

    private List<ChangeValue> changeValues;

    private Map<Long, List<Long>> referenceMap;

    private long entityClass;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getsId() {
        return sId;
    }

    public void setsId(long sId) {
        this.sId = sId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public List<ChangeValue> getChangeValues() {
        return changeValues;
    }

    public void setChangeValues(List<ChangeValue> changeValues) {
        this.changeValues = changeValues;
    }

    public long getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(long entityClass) {
        this.entityClass = entityClass;
    }

    public Map<Long, List<Long>> getReferenceMap() {
        return referenceMap;
    }

    public void setReferenceMap(Map<Long, List<Long>> referenceMap) {
        this.referenceMap = referenceMap;
    }

    @Override
    public String toString() {
        return "ChangeSnapshot{" +
                "version=" + version +
                ", sId=" + sId +
                ", id=" + id +
                ", createTime=" + createTime +
                ", changeValues=" + changeValues +
                ", referenceMap=" + referenceMap +
                ", entityClass=" + entityClass +
                '}';
    }
}
