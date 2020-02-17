package com.xforceplus.ultraman.oqsengine.pojo.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * 表示一个 entity 的指针.
 * @author dongbin
 * @version 0.1 2020/2/17 16:55
 * @since 1.8
 */
public final class EntityRef implements Serializable {

    private long id;
    private long pref;
    private long cref;

    public EntityRef() {
    }

    public EntityRef(long id, long pref, long cref) {
        this.id = id;
        this.pref = pref;
        this.cref = cref;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityRef)) return false;
        EntityRef entityRef = (EntityRef) o;
        return getId() == entityRef.getId() &&
            getPref() == entityRef.getPref() &&
            getCref() == entityRef.getCref();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPref(), getCref());
    }

    @Override
    public String toString() {
        return "EntityRef{" +
            "id=" + id +
            ", pref=" + pref +
            ", cref=" + cref +
            '}';
    }
}
