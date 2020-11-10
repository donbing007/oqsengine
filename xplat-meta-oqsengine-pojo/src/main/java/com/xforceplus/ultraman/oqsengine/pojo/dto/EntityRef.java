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
    private String orderValue;
    private int op;

    public EntityRef() {
    }

    public EntityRef(long id, long pref, long cref) {
        this(id, pref, cref, null);
    }

    public EntityRef(long id, long pref, long cref, String orderValue) {
        this.id = id;
        this.pref = pref;
        this.cref = cref;
        this.orderValue = orderValue;
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

    public String getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(String orderValue) {
        this.orderValue = orderValue;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityRef)) {
            return false;
        }
        EntityRef entityRef = (EntityRef) o;
        return getId() == entityRef.getId() &&
            getPref() == entityRef.getPref() &&
            getCref() == entityRef.getCref() &&
            Objects.equals(getOrderValue(), entityRef.getOrderValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPref(), getCref(), getOrderValue());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityRef{");
        sb.append("id=").append(id);
        sb.append(", pref=").append(pref);
        sb.append(", cref=").append(cref);
        sb.append(", orderValue='").append(orderValue).append('\'');
        sb.append(", op=").append(op);
        sb.append('}');
        return sb.toString();
    }
}
