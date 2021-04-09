package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;

/**
 * desc :
 * name : OperationResult
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public class OperationResult {
    private long txId;
    private long entityId;
    private int version;
    private int eventType;
    private ResultStatus resultStatus;

    public OperationResult(long txId, long entityId, int version, int eventType, ResultStatus resultStatus) {
        this.txId = txId;
        this.version = version;
        this.resultStatus = resultStatus;
        this.entityId = entityId;
        this.eventType = eventType;
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
}
