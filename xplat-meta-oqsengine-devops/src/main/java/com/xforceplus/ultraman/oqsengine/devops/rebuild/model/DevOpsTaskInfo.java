package com.xforceplus.ultraman.oqsengine.devops.rebuild.model;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * 任务描述.
 *
 * @author xujia 2020/9/9
 * @since 1.8
 */
public interface DevOpsTaskInfo {
    /**
     * 任务唯一编号.
     *
     * @return 任务唯一编号.
     */
    String id();

    /**
     * 任务是否完成了.
     *
     * @return true 完成.false没有完成.
     */
    boolean isDone();

    /**
     * 任务是否已经取消.
     *
     * @return true 取消.false没有取消.
     */
    boolean isCancel();

    /**
     * 任务状态.
     *
     * @return true 取消.false没有取消.
     */
    BatchStatus status();

    /**
     * 返回任务进度百分比.
     *
     * @return 百分比0-100.
     */
    long getProgressPercentage();

    long getMaintainid();

    long getEntity();

    long getStarts();

    long getEnds();

    int getStatus();

    IEntityClass getEntityClass();

    long getBatchSize();

    int getFinishSize();

    String message();

    void resetMessage(String message);

    void resetStatus(int status);

    void resetEntityClass(IEntityClass entityClass);

    void resetStartId(long startId);

    long startId();

    void setBatchSize(long size);

    int failedRecovers();

    void resetFailedRecovers(int recovers);

    void setFinishSize(int size);

    void addFinishSize(int addSize);
}
