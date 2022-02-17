package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 更新的事务负载.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:43
 * @since 1.8
 */
public class ReplacePayload implements Serializable {

    private long txId;
    private Map<IEntity, IValue[]> changes;

    public ReplacePayload(long txId) {
        this.txId = txId;
    }

    public long getTxId() {
        return txId;
    }

    /**
     * 增加一个实体的改变.
     *
     * @param entity 改变前的实体.
     * @param newValues 实体的新值.
     */
    public void addChange(IEntity entity, IValue[] newValues) {
        if (changes == null) {
            changes = new HashMap();
        }

        changes.put(entity, newValues);
    }

    /**
     * 获取更新造成的改变.
     *
     * @return 改变.
     */
    public Map<IEntity, IValue[]> getChanges() {
        if (changes == null) {
            return Collections.emptyMap();
        }
        return changes;
    }

    /**
     * 获取指定对象的改变.
     *
     * @param entity 目标对象.
     * @return 改变.
     */
    public Optional<Map.Entry<IEntity, IValue[]>> getChanage(IEntity entity) {
        IValue[] newValues = getChanges().get(entity);
        if (newValues == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(new AbstractMap.SimpleEntry(entity, newValues));
        }
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
        return getTxId() == that.getTxId() && Objects.equals(getChanges(), that.getChanges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTxId(), getChanges());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReplacePayload{");
        sb.append("changes=").append(changes);
        sb.append(", txId=").append(txId);
        sb.append('}');
        return sb.toString();
    }

}
