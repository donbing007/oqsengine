package com.xforceplus.ultraman.oqsengine.event.payload.transaction;

import java.io.Serializable;
import java.util.Objects;

/**
 * 事务回滚事件的负载.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:27
 * @since 1.8
 */
public class RollbackPayload implements Serializable {

    private long txId;
    private long maxOpNumber;
    private String msg;

    /**
     * 实例.
     *
     * @param txId        事务ID.
     * @param maxOpNumber 事务中的操作号.
     * @param msg         消息.
     */
    public RollbackPayload(long txId, long maxOpNumber, String msg) {
        this.txId = txId;
        this.msg = msg;
        this.maxOpNumber = maxOpNumber;
    }

    public long getTxId() {
        return txId;
    }

    public String getMsg() {
        return msg;
    }

    public long getMaxOpNumber() {
        return maxOpNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RollbackPayload that = (RollbackPayload) o;
        return txId == that.txId && maxOpNumber == that.maxOpNumber && Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId, maxOpNumber, msg);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RollbackPayload{");
        sb.append("txId=").append(txId);
        sb.append(", maxOpNumber=").append(maxOpNumber);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
