package com.xforceplus.ultraman.oqsengine.pojo.devops;


/**
 * desc :.
 * name : CdcErrorTask
 *
 * @author : xujia 2020/11/21
 * @since : 1.8
 */
public class CdcErrorTask {
    private long seqNo;
    private long batchId;
    private long id;
    private long entity;
    private int version;
    private int op;
    private long commitId;
    private int errorType;
    private int status;
    private String operationObject;
    private long executeTime;
    private long fixedTime;
    private String message;


    /**
     * 创建记录CDC错误任务.
     */
    public static CdcErrorTask buildErrorTask(
        long seqNo, long batchId, long id, long entity, int version, int op, long commitId, int errorType,
        String operationObject, String message) {
        CdcErrorTask cdcErrorTask = new CdcErrorTask();
        cdcErrorTask.setSeqNo(seqNo);
        cdcErrorTask.setBatchId(batchId);
        cdcErrorTask.setId(id);
        cdcErrorTask.setEntity(entity);
        cdcErrorTask.setVersion(version);
        cdcErrorTask.setOp(op);
        cdcErrorTask.setCommitId(commitId);
        cdcErrorTask.setErrorType(errorType);
        cdcErrorTask.setOperationObject(operationObject);
        cdcErrorTask.setStatus(FixedStatus.NOT_FIXED.getStatus());
        cdcErrorTask.setExecuteTime(System.currentTimeMillis());
        cdcErrorTask.setFixedTime(DevOpsConstant.NOT_INIT_TIMESTAMP);
        cdcErrorTask.setMessage(message.length() > DevOpsConstant.MAX_ERROR_MESSAGE_LENGTH
            ? message.substring(DevOpsConstant.DEFAULT_START_POS, DevOpsConstant.MAX_ERROR_MESSAGE_LENGTH) : message);
        return cdcErrorTask;
    }

    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    public void setFixedTime(long fixedTime) {
        this.fixedTime = fixedTime;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCommitId() {
        return commitId;
    }

    public void setCommitId(long commitId) {
        this.commitId = commitId;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public long getFixedTime() {
        return fixedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }


    public String getOperationObject() {
        return operationObject;
    }

    public void setOperationObject(String operationObject) {
        this.operationObject = operationObject;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public long getEntity() {
        return entity;
    }

    public void setEntity(long entity) {
        this.entity = entity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }
}
