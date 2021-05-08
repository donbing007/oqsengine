package com.xforceplus.ultraman.oqsengine.pojo.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * 表示一个 entity 的指针.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 16:55
 * @since 1.8
 */
public final class EntityRef implements Serializable, Comparable<EntityRef> {

    private long id;
    private int op;
    private int major;
    private String orderValue;

    public EntityRef() {
    }

    public EntityRef(long id, int major) {
        this(id, major, null);
    }

    public EntityRef(long id, int major, String orderValue) {
        this(id, 0, major, orderValue);
    }

    public EntityRef(long id, int op, int major) {
        this(id, op, major, null);
    }

    /**
     * 实例化.
     *
     * @param id 实体标识.
     * @param op 操作类型.
     * @param major 大版本号.
     * @param orderValue 排序值.
     */
    public EntityRef(long id, int op, int major, String orderValue) {
        this.id = id;
        this.op = op;
        this.major = major;
        this.orderValue = orderValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
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
        return getId() == entityRef.getId()
            && getOp() == entityRef.getOp()
            && getMajor() == entityRef.getMajor()
            && Objects.equals(getOrderValue(), entityRef.getOrderValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOp(), getMajor(), getOrderValue());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityRef{");
        sb.append("id=").append(id);
        sb.append(", op=").append(op);
        sb.append(", major=").append(major);
        sb.append(", orderValue='").append(orderValue).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(EntityRef o) {
        if (this.id < o.id) {
            return -1;
        } else if (this.id > o.id) {
            return 1;
        } else {
            return 0;
        }
    }
}
