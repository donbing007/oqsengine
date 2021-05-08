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
    private long maintainid;
    private long entity;
    private long starts;
    private long ends;
    private int batchSize;
    private volatile int finishSize;
    private volatile int status;
    private long createTime;
    private long updateTime;
    private String message = "";
    private long startId;
    private IEntityClass entityClass;

    private int failedRecovers;

    public DefaultDevOpsTaskInfo() {
    }

    /**
     * 实例化.
     *
     * @param maintainid  维护id.
     * @param entityClass 元信息.
     * @param starts      开始时间.
     * @param ends        结束时间.
     */
    public DefaultDevOpsTaskInfo(long maintainid, IEntityClass entityClass, long starts, long ends) {
        this(maintainid, entityClass.id(), starts, ends, 0, 0,
            BatchStatus.PENDING.getCode(), System.currentTimeMillis(), 0);
        this.entityClass = entityClass;
    }

    /**
     * 实例化.
     *
     * @param maintainid 维护ID.
     * @param entity 实例ID.
     * @param starts 开始时间.
     * @param ends 结束时间.
     * @param batchSize 批量大小.
     * @param finishSize 已完成的大小.
     * @param status 状态.
     * @param createTime 任务创建时间.
     * @param updateTime 任务更新时间.
     */
    public DefaultDevOpsTaskInfo(long maintainid, long entity, long starts, long ends, int batchSize, int finishSize,
                                 int status, long createTime, long updateTime) {
        this.maintainid = maintainid;
        this.entity = entity;
        this.starts = starts;
        this.ends = ends;
        this.batchSize = batchSize;
        this.finishSize = finishSize;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
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

    public int getBatchSize() {
        return batchSize;
    }

    public int getFinishSize() {
        return finishSize;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public void resetMessage(String message) {
        if (null != message) {
            if (message.length() > 500) {
                this.message = message.substring(0, 500);
            } else {
                this.message = message;
            }
        }
    }

    public void resetStatus(int status) {
        this.status = status;
    }

    @Override
    public void resetEntityClass(IEntityClass entityClass) {
        this.entity = entityClass.id();
        this.entityClass = entityClass;
    }

    @Override
    public void resetStartId(long startId) {
        this.startId = startId;
    }

    @Override
    public long startId() {
        return startId;
    }

    public synchronized void setFinishSize(int finishSize) {
        this.finishSize = finishSize;
    }

    public synchronized void addFinishSize(int addSize) {
        finishSize += addSize;
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
    public boolean isCancel() {
        return status == BatchStatus.CANCEL.getCode();
    }

    @Override
    public BatchStatus status() {
        return BatchStatus.toBatchStatus(status);
    }

    @Override
    public int getProgressPercentage() {
        return 0 < batchSize ? (getFinishSize() * 100) / batchSize : 0;
    }

    @Override
    public void setBatchSize(int size) {
        this.batchSize = size;
    }

    public int failedRecovers() {
        return failedRecovers;
    }

    public void resetFailedRecovers(int recovers) {
        this.failedRecovers = recovers;
    }
}
