package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.io.Serializable;
import java.util.Arrays;
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
    private IEntity[] entities;

    /**
     * 实例化.
     *
     * @param txId 事务id.
     * @param number 创建的数量.
     * @param entities 创建的目标实体.
     */
    public BuildPayload(long txId, long number, IEntity ...entities) {
        this.txId = txId;
        this.entities = entities;
        this.number = number;
    }

    public IEntity getEntity() {
        return entities[0];
    }

    public IEntity[] getEntities() {
        return entities;
    }

    public long getTxId() {
        return txId;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BuildPayload)) {
            return false;
        }
        BuildPayload that = (BuildPayload) o;
        return getTxId() == that.getTxId() && getNumber() == that.getNumber() && Arrays.equals(getEntities(),
            that.getEntities());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getTxId(), getNumber());
        result = 31 * result + Arrays.hashCode(getEntities());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuildPayload{");
        sb.append("entities=").append(Arrays.toString(entities));
        sb.append(", number=").append(number);
        sb.append(", txId=").append(txId);
        sb.append('}');
        return sb.toString();
    }
}
