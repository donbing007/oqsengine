package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityFamily;

import java.util.Objects;

/**
 * 继承家族信息实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/19 18:04
 * @since 1.8
 */
public class EntityFamily implements IEntityFamily {

    private long parent;
    private long child;

    public EntityFamily(long parent, long child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public long parent() {
        return parent;
    }

    @Override
    public long child() {
        return child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityFamily)) {
            return false;
        }
        EntityFamily that = (EntityFamily) o;
        return parent == that.parent &&
            child == that.child;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }

    @Override
    public String toString() {
        return "EntityFamily{" +
            "parent=" + parent +
            ", child=" + child +
            '}';
    }
}