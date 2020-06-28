package com.xforceplus.ultraman.oqsengine.storage.master.command;

import java.io.Serializable;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/17/2020 10:20 AM
 * 功能描述:
 * 修改历史:
 */
public class StorageEntity implements Serializable {
    private long id;
    private long entity;
    private int version;
    private long pref;
    private long cref;
    private Boolean deleted;
    private String attribute;
    private long time;

    public StorageEntity() {
    }

    public StorageEntity(long id, long entity, int version, long pref, long cref, Boolean deleted, String attribute) {
        this.id = id;
        this.entity = entity;
        this.version = version;
        this.pref = pref;
        this.cref = cref;
        this.deleted = deleted;
        this.attribute = attribute;
    }

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

    @Override
    public String toString() {
        return "StorageEntity{" +
            "id=" + id +
            ", entity=" + entity +
            ", version=" + version +
            ", pref=" + pref +
            ", cref=" + cref +
            ", deleted=" + deleted +
            ", attribute=" + attribute +
            '}';
    }
}
