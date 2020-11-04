package com.xforceplus.ultraman.oqsengine.storage.master.define;

import java.io.Serializable;
import java.util.Objects;

/**
 * 储存定义.
 */
public class StorageEntity implements Serializable {
    private long id;
    private long entity;
    private long tx;
    private long commitid;
    private int version;
    private int op;
    private long pref;
    private long cref;
    private boolean deleted;
    private String attribute;
    private String meta;
    private long time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEntity() {
        return entity;
    }

    public void setEntity(long entity) {
        this.entity = entity;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getCommitid() {
        return commitid;
    }

    public void setCommitid(long commitid) {
        this.commitid = commitid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getPref() {
        return pref;
    }

    public void setPref(long pref) {
        this.pref = pref;
    }

    public long getCref() {
        return cref;
    }

    public void setCref(long cref) {
        this.cref = cref;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageEntity)) return false;
        StorageEntity entity1 = (StorageEntity) o;
        return getId() == entity1.getId() &&
            getEntity() == entity1.getEntity() &&
            getTx() == entity1.getTx() &&
            getCommitid() == entity1.getCommitid() &&
            getVersion() == entity1.getVersion() &&
            getOp() == entity1.getOp() &&
            getPref() == entity1.getPref() &&
            getCref() == entity1.getCref() &&
            getTime() == entity1.getTime() &&
            Objects.equals(getDeleted(), entity1.getDeleted()) &&
            Objects.equals(getAttribute(), entity1.getAttribute()) &&
            Objects.equals(getMeta(), entity1.getMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getId(), getEntity(), getTx(), getCommitid(), getVersion(), getOp(),
            getPref(), getCref(), getDeleted(), getAttribute(), getMeta(), getTime());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StorageEntity{");
        sb.append("id=").append(id);
        sb.append(", entity=").append(entity);
        sb.append(", tx=").append(tx);
        sb.append(", commitid=").append(commitid);
        sb.append(", version=").append(version);
        sb.append(", op=").append(op);
        sb.append(", pref=").append(pref);
        sb.append(", cref=").append(cref);
        sb.append(", deleted=").append(deleted);
        sb.append(", attribute='").append(attribute).append('\'');
        sb.append(", meta='").append(meta).append('\'');
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}