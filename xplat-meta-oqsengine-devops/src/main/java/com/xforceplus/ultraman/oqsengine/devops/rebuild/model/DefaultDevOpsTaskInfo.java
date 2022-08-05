package com.xforceplus.ultraman.oqsengine.devops.rebuild.model;


import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * 任务描述.
 *
 * @author xujia 2020/8/24
 * @since 1.8
 */
public class DefaultDevOpsTaskInfo implements DevOpsTaskInfo {
    public static final int UN_DONE_BATCH_SIZE = 0;

    private long maintainid;
    private long entity;
    private long starts;
    private long ends;
    private volatile long batchSize;
    private volatile int finishSize;
    private volatile int errorSize;
    private volatile int status;
    private long createTime;
    private long updateTime;
    private String message = "";
    private long startId;

    private IEntityClass entityClass;

    public DefaultDevOpsTaskInfo() {
    }

    public DefaultDevOpsTaskInfo(long maintainid) {
        this.maintainid = maintainid;
    }

    /**
     * 实例化.
     *
     * @param maintainId    维护id.
     * @param entityClass   元信息.
     * @param starts        开始时间.
     * @param ends          结束时间.
     */
    public DefaultDevOpsTaskInfo(long maintainId, IEntityClass entityClass, long starts, long ends) {
        this(maintainId, entityClass.id(), starts, ends, UN_DONE_BATCH_SIZE, 0,
            BatchStatus.RUNNING.getCode(), System.currentTimeMillis(), 0);
        this.entityClass = entityClass;
        this.message = "TASK RUNNING";
    }

    /**
     * 实例化.
     *
     * @param maintainId 维护ID.
     * @param entity 实例ID.
     * @param starts 开始时间.
     * @param ends 结束时间.
     * @param batchSize 批量大小.
     * @param finishSize 已完成的大小.
     * @param status 状态.
     * @param createTime 任务创建时间.
     * @param updateTime 任务更新时间.
     */
    public DefaultDevOpsTaskInfo(long maintainId, long entity, long starts, long ends, int batchSize, int finishSize,
                                 int status, long createTime, long updateTime) {
        this.maintainid = maintainId;
        this.entity = entity;
        this.starts = starts;
        this.ends = ends;
        this.batchSize = batchSize;
        this.finishSize = finishSize;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public long updateTime() {
        return updateTime;
    }

    public long getMaintainid() {
        return maintainid;
    }

    public long getEntity() {
        return entity;
    }

    public long getStarts() {
        return starts;
    }

    public long getEnds() {
        return ends;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public int getFinishSize() {
        return finishSize;
    }

    public int getErrorSize() {
        return errorSize;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public void resetMessage(String message) {
        if (null != message) {
            this.message = message.length() > 500 ? message.substring(0, 500) : message;
        }
    }

    @Override
    public void resetStatus(int status) {
        this.status = status;
    }

    @Override
    public void setFinishSize(int finishSize) {
        this.finishSize = finishSize;
    }

    @Override
    public void addFinishSize(int addSize) {
        this.finishSize += addSize;
    }

    @Override
    public void addBatchSize(int addBatchSize) {
        this.batchSize += addBatchSize;
    }

    @Override
    public void setErrorSize(int size) {
        this.errorSize = Long.valueOf(size).intValue();
    }

    @Override
    public void resetUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String id() {
        return Long.toString(maintainid);
    }

    @Override
    public boolean isDone() {
        return status == BatchStatus.DONE.getCode();
    }

    @Override
    public boolean isError() {
        return status == BatchStatus.ERROR.getCode();
    }

    @Override
    public boolean isCancel() {
        return status == BatchStatus.CANCEL.getCode();
    }

    @Override
    public BatchStatus status() {
        return BatchStatus.toBatchStatus(status);
    }

    @Override
    public boolean isEnd() {
        return isDone() || isError() || isCancel();
    }

    @Override
    public long getProgressPercentage() {
        return 0 < batchSize ? (finishSize * 100L / batchSize) : 0;
    }

    @Override
    public void setBatchSize(long size) {
        this.batchSize = size;
    }

    @Override
    public long getStartId() {
        return startId;
    }

    @Override
    public void setStartId(long startId) {
        this.startId = startId;
    }
}
