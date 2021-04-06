package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 创建的负载.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:39
 * @since 1.8
 */
public class BuildPayload implements Serializable {

    private long txId;
    private long number;
    private IEntity entity;

    public BuildPayload(long txId, long number, IEntity entity) {
        this.txId = txId;
        this.entity = entity;
        this.number = number;
    }

    public IEntity getEntity() {
        return entity;
    }

    public long getTxId() {
        return txId;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildPayload that = (BuildPayload) o;
        return txId == that.txId && number == that.number && Objects.equals(entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId, number, entity);
    }

    @Override
    public String toString() {
        return "BuildPayload{" +
                "txId=" + txId +
                ", number=" + number +
                ", entity=" + entity +
                '}';
    }

    @Override
    public String toString() {
        return "BuildPayload{" +
                "entity=" + entity +
                '}';
    }
}
