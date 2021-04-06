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
    private long maxOpNumber;

    public CommitPayload(long txId, long commitId, String msg, boolean readonly, long maxOpNumber) {
        this.txId = txId;
        this.commitId = commitId;
        this.msg = msg;
        this.readonly = readonly;
        this.maxOpNumber = maxOpNumber;
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

    public long getMaxOpNumber() {
        return maxOpNumber;
    }

    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitPayload that = (CommitPayload) o;
        return txId == that.txId
                && commitId == that.commitId
                && readonly == that.readonly
                && maxOpNumber == that.maxOpNumber
                && Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId, commitId, msg, readonly, maxOpNumber);
    }

    @Override
    public String toString() {
        return "CommitPayload{" +
                "txId=" + txId +
                ", commitId=" + commitId +
                ", msg='" + msg + '\'' +
                ", readonly=" + readonly +
                ", maxOpNumber=" + maxOpNumber +
                '}';
    }
}
