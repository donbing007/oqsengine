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
    boolean isError();

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
     * 是否结束.
     *
     * @return true/false
     */
    boolean isEnd();

    /**
     * 返回任务进度百分比.
     *
     * @return 百分比0-100.
     */
    long getProgressPercentage();

    /**
     * 维护ID.
     *
     * @return 维护ID.
     */
    long getMaintainid();

    /**
     * entityClassId.
     *
     * @return entityClassId.
     */
    long getEntity();

    /**
     * startTime.
     *
     * @return 范围开始时间.
     */
    long getStarts();

    /**
     * endTime.
     *
     * @return 范围结束时间.
     */
    long getEnds();

    /**
     * 任务状态.
     *
     * @return 任务状态.
     */
    int getStatus();

    /**
     * entityClass实例.
     *
     * @return entityClass.
     */
    IEntityClass getEntityClass();

    /**
     * 总任务大小.
     *
     * @return 任务大小.
     */
    long getBatchSize();

    /**
     * 任务中已重建数量.
     *
     * @return 完成数量.
     */
    int getFinishSize();

    /**
     * 任务中重建失败数量.
     *
     * @return 失败数量.
     */
    int getErrorSize();

    /**
     * 说明.
     *
     * @return 说明.
     */
    String message();

    /**
     * 重置说明.
     *
     * @param message 重置说明.
     */
    void resetMessage(String message);

    /**
     * 重置状态.
     *
     * @param status 重置状态.
     */
    void resetStatus(int status);

    /**
     * 设置任务总数量.
     *
     * @param size 设置任务总数量.
     */
    void setBatchSize(long size);

    /**
     * 设置任务完成数量.
     *
     * @param size 设置任务完成数量.
     */
    void setFinishSize(int size);

    /**
     * 增量任务完成数量.
     *
     * @param addSize 增量任务完成数量.
     */
    void addFinishSize(int addSize);

    /**
     * 设置任务失败数量.
     *
     * @param size 任务失败数量.
     */
    void setErrorSize(long size);

    /**
     * 增量任务失败数量.
     *
     * @param errorSize 增量任务失败数量.
     */
    void addErrorSize(int errorSize);

    /**
     * 获取更新时间.
     *
     * @return 返回updateTime.
     */
    long updateTime();

    /**
     * resetUpdateTime.
     *
     * @param updateTime updateTime.
     */
    void resetUpdateTime(long updateTime);

    /**
     * 获取当前incrementSize.
     */
    int incrementSize();

    /**
     * 重置incrementSize.
     */
    void resetIncrementSize(int incrementSize);
}
