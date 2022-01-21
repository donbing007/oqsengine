package com.xforceplus.ultraman.oqsengine.devops.rebuild.handler;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;

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
    boolean isError();

    /**
     * 返回任务进度百分比.
     *
     * @return 百分比0-100.
     */
    long getProgressPercentage();

    /**
     * 获取任务信息.
     * @return
     */
    DevOpsTaskInfo devOpsTaskInfo();
}
