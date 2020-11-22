package com.xforceplus.ultraman.oqsengine.pojo.devops.cdc;

import static com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.DevOpsConstant.*;

/**
 * desc :
 * name : CdcErrorTask
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public class CdcErrorTask {
    private long seqNo;
    private long id;
    private long commitId;
    private long executeTime;
    private long fixedTime;
    private String message;
    private int status;

    public static CdcErrorTask buildErrorTask(long seqNo, long id, long commitId, String message) {
        CdcErrorTask cdcErrorTask = new CdcErrorTask();
        cdcErrorTask.setSeqNo(seqNo);
        cdcErrorTask.setId(id);
        cdcErrorTask.setCommitId(commitId);
        cdcErrorTask.setMessage(message.length() > MAX_ERROR_MESSAGE_LENGTH ?
                            message.substring(DEFAULT_START_POS, MAX_ERROR_MESSAGE_LENGTH) : message);
        cdcErrorTask.setExecuteTime(System.currentTimeMillis());
        cdcErrorTask.setStatus(FixedStatus.NOT_FIXED.ordinal());
        cdcErrorTask.setFixedTime(NOT_INIT_TIMESTAMP);
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
}
