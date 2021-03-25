package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author dongbin
 * @version 0.1 2021/3/24 15:43
 * @since 1.8
 */
public class ReplacePayload implements Serializable {

    private IEntity old;
    private IEntity current;

    public ReplacePayload(IEntity old, IEntity current) {
        this.old = old;
        this.current = current;
    }

    public IEntity getOld() {
        return old;
    }

    public IEntity getCurrent() {
        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReplacePayload)) {
            return false;
        }
        ReplacePayload that = (ReplacePayload) o;
        return Objects.equals(getOld(), that.getOld()) &&
            Objects.equals(getCurrent(), that.getCurrent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOld(), getCurrent());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReplacePayload{");
        sb.append("old=").append(old);
        sb.append(", current=").append(current);
        sb.append('}');
        return sb.toString();
    }
}
