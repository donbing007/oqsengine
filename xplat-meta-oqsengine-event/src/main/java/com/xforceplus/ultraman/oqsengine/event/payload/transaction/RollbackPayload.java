package com.xforceplus.ultraman.oqsengine.event.payload.transaction;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author dongbin
 * @version 0.1 2021/3/24 15:27
 * @since 1.8
 */
public class RollbackPayload implements Serializable {

    private long txId;
    private String msg;

    public RollbackPayload(long txId, String msg) {
        this.txId = txId;
        this.msg = msg;
    }

    public long getTxId() {
        return txId;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RollbackPayload)) {
            return false;
        }
        RollbackPayload that = (RollbackPayload) o;
        return getTxId() == that.getTxId() &&
            Objects.equals(getMsg(), that.getMsg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTxId(), getMsg());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TransactionRollbackPayload{");
        sb.append("txId=").append(txId);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
