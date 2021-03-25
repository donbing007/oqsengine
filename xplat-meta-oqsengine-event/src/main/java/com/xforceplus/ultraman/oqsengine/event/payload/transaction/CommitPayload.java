package com.xforceplus.ultraman.oqsengine.event.payload.transaction;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 事务事件的负载.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:03
 * @since 1.8
 */
public class CommitPayload implements Serializable {
    private long txId;
    private long commitId;
    private String msg;
    private boolean readonly;

    public CommitPayload(long txId, long commitId, String msg, boolean readonly) {
        this.txId = txId;
        this.commitId = commitId;
        this.msg = msg;
        this.readonly = readonly;
    }

    public long getTxId() {
        return txId;
    }

    public long getCommitId() {
        return commitId;
    }

    public Optional<String> getMsg() {
        return Optional.ofNullable(msg);
    }

    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommitPayload)) {
            return false;
        }
        CommitPayload that = (CommitPayload) o;
        return getTxId() == that.getTxId() &&
            getCommitId() == that.getCommitId() &&
            Objects.equals(getMsg(), that.getMsg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTxId(), getCommitId(), getMsg());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TransactionPayload{");
        sb.append("txId=").append(txId);
        sb.append(", commitId=").append(commitId);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
