package com.xforceplus.ultraman.oqsengine.devops.handler;

import com.xforceplus.ultraman.oqsengine.devops.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.task.model.IDevOpsTaskInfo;

import java.util.Optional;

/**
 * 任务表示.
 *
 * @author dongbin
 * @version 0.1 2020/8/18 18:37
 * @since 1.8
 */
public interface TaskHandler {
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
     * 取消任务.
     */
    void cancel() throws Exception;

    /**
     * 任务状态.
     */
    Optional<BatchStatus> batchStatus();

    /**
     * 返回任务进度百分比.
     *
     * @return 百分比0-100.
     */
    int getProgressPercentage();

    IDevOpsTaskInfo devOpsTaskInfo();
}
