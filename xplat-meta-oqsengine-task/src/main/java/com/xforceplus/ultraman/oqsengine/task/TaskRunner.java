package com.xforceplus.ultraman.oqsengine.task;

/**
 * 任务执行者.
 * 每一个执行者只能处理一种或者几种任务.
 *
 * @author dongbin
 * @version 0.1 2021/08/12 13:55
 * @since 1.8
 */
public interface TaskRunner {

    /**
     * 运行任务.
     *
     * @param coordinator 当前任务的协调者.
     * @param task 目标任务.
     */
    public void run(TaskCoordinator coordinator, Task task);
}
