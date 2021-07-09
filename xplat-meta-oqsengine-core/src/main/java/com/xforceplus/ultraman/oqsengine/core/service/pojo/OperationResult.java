package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationHint;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 表示操作结果.
 *
 * @author xujia 2021/4/8
 * @since 1.8
 */
public class OperationResult {
    private final long txId;
    private final long entityId;
    private final int version;
    private final int eventType;
    private ResultStatus resultStatus;
    private Collection<CalculationHint> hints;
    private String message;

    /**
     * 实例化.
     */
    public OperationResult(long txId, long entityId, int version, int eventType, ResultStatus resultStatus) {
        this.txId = txId;
        this.version = version;
        this.resultStatus = resultStatus;
        this.entityId = entityId;
        this.eventType = eventType;
    }

    /**
     * 实例化.
     */
    public OperationResult(long txId, long entityId, int version, int eventType, ResultStatus resultStatus,
                           String message) {
        this.txId = txId;
        this.version = version;
        this.resultStatus = resultStatus;
        this.entityId = entityId;
        this.eventType = eventType;
        this.message = message;
    }

    /**
     * 实例化.
     */
    public OperationResult(long txId, long entityId, int version, int eventType, ResultStatus resultStatus,
                           Collection<CalculationHint> hints, String message) {
        this.txId = txId;
        this.version = version;
        this.resultStatus = resultStatus;
        this.entityId = entityId;
        this.eventType = eventType;
        this.hints = hints;
        this.message = message;
    }

    /**
     * 半成功状态.
     */
    public void resetStatus(Collection<CalculationHint> hints) {
        this.resultStatus = ResultStatus.HALF_SUCCESS;
        this.hints = hints;
        this.message = ResultStatus.HALF_SUCCESS.name();
    }

    public int getVersion() {
        return version;
    }

    public long getTxId() {
        return txId;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public int getEventType() {
        return eventType;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getMessage() {
        return message;
    }

    public Collection<CalculationHint> getHints() {
        return hints;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OperationResult{");
        sb.append("txId=").append(txId);
        sb.append(", entityId=").append(entityId);
        sb.append(", version=").append(version);
        sb.append(", eventType=").append(eventType);
        sb.append(", resultStatus=").append(resultStatus);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationResult that = (OperationResult) o;
        return txId == that.txId && entityId == that.entityId && version == that.version
            && eventType == that.eventType && resultStatus == that.resultStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId, entityId, version, eventType, resultStatus);
    }


    /**
     * 工厂.
     */
    public static final class Builder {
        private long txId;
        private long entityId;
        private int version;
        private int eventType;
        private ResultStatus resultStatus;
        private String message;

        private Builder() {
        }

        public static Builder anOperationResult() {
            return new Builder();
        }

        public Builder withTxId(long txId) {
            this.txId = txId;
            return this;
        }

        public Builder withEntityId(long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withEventType(int eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withResultStatus(ResultStatus resultStatus) {
            this.resultStatus = resultStatus;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public OperationResult build() {
            return new OperationResult(txId, entityId, version, eventType, resultStatus, message);
        }
    }
}
